package ec.cityalerta.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteImagenRepository
import ec.cityalerta.app.model.repository.ReporteUbicacionRepository
import ec.cityalerta.app.model.repository.ReporteStorageRepository
import ec.cityalerta.app.model.repository.PerfilRepository
import ec.cityalerta.app.model.repository.BarrioRepository
import ec.cityalerta.app.model.remote.SupabaseProvider
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

private const val TIME_AGO_RECENT = "Hace poco"

// Estado de la pantalla de busqueda de reportes
data class SearchReportState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: ReportType? = null,
    val reportes: List<ReporteUI> = emptyList(),
    val error: String? = null,
    val ciudadId: String = ""
)

// ViewModel que maneja la busqueda y filtrado de reportes
class SearchReportViewModel(
    private val reporteRepository: ReporteRepository = ReporteRepository(),
    private val reporteImagenRepository: ReporteImagenRepository = ReporteImagenRepository(),
    private val reporteUbicacionRepository: ReporteUbicacionRepository = ReporteUbicacionRepository(),
    private val reporteStorageRepository: ReporteStorageRepository = ReporteStorageRepository(),
    private val perfilRepository: PerfilRepository = PerfilRepository(),
    private val barrioRepository: BarrioRepository = BarrioRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(SearchReportState())
    val state: StateFlow<SearchReportState> = _state

    private var dataLoaded = false

    // Carga los datos cuando el usuario entra a la pantalla de busqueda
    fun loadData(force: Boolean = false) {
        if (!force && dataLoaded && _state.value.reportes.isNotEmpty()) return

        initializeCiudadId()
    }

    // Inicializa el ID de la ciudad desde el perfil del usuario autenticado
    private fun initializeCiudadId() {
        viewModelScope.launch {
            try {
                val userId = SupabaseProvider.client.auth.currentUserOrNull()?.id
                if (userId != null) {
                    val perfil = perfilRepository.getById(userId).getOrNull()
                    perfil?.let {
                        _state.value = _state.value.copy(ciudadId = it.ciudadId)
                        search("")
                        dataLoaded = true
                    }
                }
            } catch (_: Exception) {
                _state.value = _state.value.copy(error = "Error al cargar ciudad")
            }
        }
    }

    // Actualiza el query de busqueda por nombre de barrio
    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        search(query)
    }

    // Selecciona o deselecciona una categoria de filtro
    fun selectCategory(category: ReportType?) {
        _state.value = _state.value.copy(selectedCategory = category)
        search(_state.value.searchQuery)
    }

    // Realiza la busqueda con los filtros actuales
    private fun search(query: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val ciudadId = _state.value.ciudadId
                if (ciudadId.isEmpty()) {
                    _state.value = _state.value.copy(isLoading = false, error = "No se pudo cargar la ciudad")
                    return@launch
                }

                val allReportes = reporteRepository.getReporteByCiudadId(ciudadId).getOrNull().orEmpty()

                // Filtra reportes por categoria y nombre de barrio
                val filteredReportes = allReportes.filter { reporte ->
                    val matchesCategory = _state.value.selectedCategory == null || reporte.categoria == _state.value.selectedCategory
                    val matchesBarrio = if (query.isBlank()) {
                        true
                    } else {
                        val barrioInfo = barrioRepository.getById(reporte.barrio_id).getOrNull()
                        barrioInfo?.nombre?.contains(query, ignoreCase = true) ?: false
                    }
                    matchesCategory && matchesBarrio
                }

                val reportesUI = filteredReportes.map { reporte ->
                    async { mapReporteToUi(reporte) }
                }.awaitAll()

                _state.value = _state.value.copy(
                    isLoading = false,
                    reportes = reportesUI
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error en la busqueda"
                )
            }
        }
    }

    // Convierte un reporte a su representacion visual con imagenes y datos procesados
    private suspend fun mapReporteToUi(reporte: Reporte): ReporteUI = coroutineScope {
        val primerImagen = reporteImagenRepository.getFirstImagenByReporteId(reporte.id).getOrNull()
        val imageUrl = primerImagen?.storage_uuid?.takeIf { it.isNotBlank() }?.let { objectPath ->
            reporteStorageRepository.generateSignedImageUrl(objectPath).getOrNull()
        }

        val ubicacionDeferred = async { reporteUbicacionRepository.getById(reporte.ubicacion_id).getOrNull() }
        val barrioDeferred = async { barrioRepository.getById(reporte.barrio_id).getOrNull() }
        val ubicacion = ubicacionDeferred.await()
        val barrio = barrioDeferred.await()

        ReporteUI(
            id = reporte.id,
            categoria = mapCategoriaLabel(reporte.categoria),
            imageUrl = imageUrl,
            barrio = barrio?.nombre ?: "Barrio desconocido",
            direccion = ubicacion?.direccion_aproximada ?: "Direccion no disponible",
            descripcion = reporte.descripcion,
            estado = mapEstadoLabel(reporte.estado),
            fecha = reporte.fecha_reporte,
            timeAgo = calculateTimeAgo(reporte.created_at)
        )
    }

    // Calcula el tiempo transcurrido desde la creacion del reporte
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
        } catch (_: Exception) {
            TIME_AGO_RECENT
        }
    }

    // Convierte el tipo de categoria a etiqueta legible
    private fun mapCategoriaLabel(categoria: ReportType): String = when (categoria) {
        ReportType.ZONA_DE_RIESGO -> "Seguridad"
        ReportType.BACHE -> "Bache"
        ReportType.AGUA -> "Agua"
        ReportType.LUZ -> "Luz"
    }

    // Convierte el estado del reporte a etiqueta legible
    private fun mapEstadoLabel(estado: ReporteEstado): String = when (estado) {
        ReporteEstado.PENDIENTE -> "Pendiente"
        ReporteEstado.EN_PROCESO -> "En Proceso"
        ReporteEstado.RESUELTO -> "Resuelto"
    }
}






