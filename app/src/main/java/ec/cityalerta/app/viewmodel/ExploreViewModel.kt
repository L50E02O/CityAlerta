package ec.cityalerta.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteStorageRepository
import ec.cityalerta.app.model.repository.CiudadRepository
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.local.ReporteEntity
import ec.cityalerta.app.model.remote.SupabaseProvider
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

private const val TIME_AGO_RECENT = "Hace poco"

data class ReporteUI(
    val id: String,
    val categoria: String,
    val categoryType: ReportType,
    val imageUrl: String?,
    val barrio: String,
    val direccion: String,
    val descripcion: String,
    val estado: String,
    val fecha: String,
    val timeAgo: String,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val ciudadId: String = ""
)

data class ExploreState(
    val isLoading: Boolean = false,
    val ciudadNombre: String = "Cargando...",
    val reportes: List<ReporteUI> = emptyList(),
    val error: String? = null,
    val currentPage: Int = 1,
    val hasNextPage: Boolean = false,
    val itemsPerPage: Int = 10,
    val isSyncing: Boolean = false
)

class ExploreViewModel(
    private val authRepository: AuthRepositoryContract,
    private val reporteRepository: ReporteRepository,
    private val reporteStorageRepository: ReporteStorageRepository,
    private val ciudadRepository: CiudadRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExploreState())
    val state: StateFlow<ExploreState> = _state

    private val _ciudadId = MutableStateFlow<String?>(null)
    private var observationJob: Job? = null
    private var realtimeJob: Job? = null

    init {
        setupReactiveObservation()
        ensureCiudadIdIsReady()
    }

    private fun ensureCiudadIdIsReady() {
        viewModelScope.launch {
            while (_ciudadId.value == null) {
                authRepository.getCiudadId().onSuccess { id ->
                    _ciudadId.value = id
                }
                if (_ciudadId.value == null) delay(1500)
            }
        }
    }

    private fun setupReactiveObservation() {
        viewModelScope.launch {
            combine(
                _ciudadId.filterNotNull(),
                _state.map { it.currentPage }.distinctUntilChanged()
            ) { id, page -> id to page }
            .collectLatest { (id, page) ->
                startRoomObservation(id, page)
                setupRealtimeSync(id)
            }
        }
    }

    private suspend fun setupRealtimeSync(ciudadId: String) {
        realtimeJob?.cancel()
        realtimeJob = viewModelScope.launch {
            val channel = SupabaseProvider.client.realtime.channel("reportes_$ciudadId")
            
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "reporte"
                filter = "ciudad_id=eq.$ciudadId"
            }

            launch {
                channel.subscribe()
                changeFlow.collect { action ->
                    // Cuando algo cambia en la ciudad del usuario, disparamos una sincronización de la página actual
                    loadData(forceRefresh = true)
                }
            }
        }
    }

    private suspend fun startRoomObservation(ciudadId: String, page: Int) {
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            val offset = (page - 1) * _state.value.itemsPerPage

            // Mantener el conteo actualizado (esto puede ser por polling ligero o room ya lo hace si observamos el total)
            launch {
                while (true) {
                    val total = reporteRepository.getTotalLocalReportesCount(ciudadId)
                    val hasNext = total > (page * _state.value.itemsPerPage)
                    if (_state.value.hasNextPage != hasNext) {
                        _state.update { it.copy(hasNextPage = hasNext) }
                    }
                    delay(5000)
                }
            }

            reporteRepository.getLocalReportesFlow(ciudadId, _state.value.itemsPerPage, offset)
                .collect { entities ->
                    val uiList = entities.map { entity ->
                        ReporteUI(
                            id = entity.id,
                            categoria = mapCategoriaLabel(entity.categoria),
                            categoryType = entity.categoria,
                            imageUrl = null,
                            barrio = entity.barrio_nombre ?: "Barrio desconocido",
                            direccion = entity.direccion_aproximada ?: "Dirección no disponible",
                            descripcion = entity.descripcion,
                            estado = mapEstadoLabel(entity.estado),
                            fecha = entity.fecha_reporte,
                            timeAgo = calculateTimeAgo(entity.created_at),
                            ciudadId = entity.ciudad_id
                        )
                    }
                    _state.update { it.copy(reportes = uiList) }

                    val uuids = entities.mapNotNull { it.image_url }.distinct()
                    if (uuids.isNotEmpty()) {
                        loadImages(uuids, entities)
                    }
                }
        }
    }

    private fun loadImages(uuids: List<String>, entities: List<ReporteEntity>) {
        viewModelScope.launch {
            reporteStorageRepository.generateSignedImageUrls(uuids).onSuccess { signedUrlsMap ->
                _state.update { currentState ->
                    currentState.copy(
                        reportes = currentState.reportes.map { ui ->
                            val originalEntity = entities.find { it.id == ui.id }
                            ui.copy(imageUrl = signedUrlsMap[originalEntity?.image_url])
                        }
                    )
                }
            }
        }
    }

    fun nextPage() {
        if (_state.value.hasNextPage) {
            _state.update { it.copy(currentPage = it.currentPage + 1) }
            loadData() 
        }
    }

    fun previousPage() {
        if (_state.value.currentPage > 1) {
            _state.update { it.copy(currentPage = it.currentPage - 1) }
            loadData()
        }
    }

    private var lastSyncPage: Int = -1
    private var lastSyncTime: Long = 0
    private val SYNC_COOLDOWN_MS = 60_000 // Aumentamos a 1 minuto porque tenemos Realtime

    fun loadData(forceRefresh: Boolean = false) {
        val currentState = _state.value
        val now = System.currentTimeMillis()

        if (currentState.isSyncing) return
        
        // Si no es forzado y la página está fresca, omitimos
        if (!forceRefresh && lastSyncPage == currentState.currentPage && 
            (now - lastSyncTime) < SYNC_COOLDOWN_MS && currentState.reportes.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true, error = null) }
            try {
                val ciudadId = _ciudadId.value ?: authRepository.getCiudadId().getOrNull()
                if (ciudadId != null) {
                    val nombre = loadCiudadNombre(ciudadId)
                    _state.update { it.copy(ciudadNombre = nombre) }

                    val offset = (currentState.currentPage - 1) * currentState.itemsPerPage
                    reporteRepository.getReporteByCiudadId(ciudadId, currentState.itemsPerPage, offset).onSuccess {
                        lastSyncPage = currentState.currentPage
                        lastSyncTime = System.currentTimeMillis()
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            } finally {
                _state.update { it.copy(isSyncing = false, isLoading = false) }
            }
        }
    }

    private fun calculateTimeAgo(createdAt: String?): String {
        if (createdAt.isNullOrBlank()) return TIME_AGO_RECENT
        return try {
            val createdAtInstant = Instant.parse(createdAt)
            val now = Instant.now()
            val minutes = ChronoUnit.MINUTES.between(createdAtInstant, now)
            val hours = ChronoUnit.HOURS.between(createdAtInstant, now)
            val days = ChronoUnit.DAYS.between(createdAtInstant, now)
            when {
                days > 0 -> "Hace $days dia${if (days > 1) "s" else ""}"
                hours > 0 -> "Hace $hours hora${if (hours > 1) "s" else ""}"
                minutes > 0 -> "Hace $minutes minuto${if (minutes > 1) "s" else ""}"
                else -> TIME_AGO_RECENT
            }
        } catch (_: Exception) { TIME_AGO_RECENT }
    }

    private suspend fun loadCiudadNombre(ciudadId: String): String {
        return ciudadRepository.getById(ciudadId).getOrNull()?.nombre ?: "Ubicacion desconocida"
    }

    private fun mapCategoriaLabel(categoria: ReportType): String = when (categoria) {
        ReportType.ZONA_DE_RIESGO -> "Seguridad"
        ReportType.BACHE -> "Bache"
        ReportType.AGUA -> "Agua"
        ReportType.LUZ -> "Luz"
    }

    private fun mapEstadoLabel(estado: ReporteEstado): String = when (estado) {
        ReporteEstado.PENDIENTE -> "Pendiente"
        ReporteEstado.EN_PROCESO -> "En Proceso"
        ReporteEstado.RESUELTO -> "Resuelto"
    }
}
