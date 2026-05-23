package ec.cityalerta.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ec.cityalerta.app.model.data.perfil.Perfil
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteImagenRepository
import ec.cityalerta.app.model.repository.ReporteUbicacionRepository
import ec.cityalerta.app.model.repository.ReporteStorageRepository
import ec.cityalerta.app.model.repository.PerfilRepository
import ec.cityalerta.app.model.repository.CiudadRepository
import ec.cityalerta.app.model.repository.BarrioRepository
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.remote.SupabaseProvider
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

private const val TIME_AGO_RECENT = "Hace poco"

data class ReporteUI(
    val id: String,
    val categoria: String,
    val imageUrl: String?,
    val barrio: String,
    val direccion: String,
    val descripcion: String,
    val estado: String,
    val fecha: String,
    val timeAgo: String
)

data class ExploreState(
    val isLoading: Boolean = false,
    val ciudadNombre: String = "Cargando...",
    val reportes: List<ReporteUI> = emptyList(),
    val error: String? = null
)

class ExploreViewModel(
    private val authRepository: AuthRepositoryContract,
    private val reporteRepository: ReporteRepository = ReporteRepository(),
    private val reporteImagenRepository: ReporteImagenRepository = ReporteImagenRepository(),
    private val reporteUbicacionRepository: ReporteUbicacionRepository = ReporteUbicacionRepository(),
    private val reporteStorageRepository: ReporteStorageRepository = ReporteStorageRepository(),
    private val perfilRepository: PerfilRepository = PerfilRepository(),
    private val ciudadRepository: CiudadRepository = CiudadRepository(),
    private val barrioRepository: BarrioRepository = BarrioRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ExploreState())
    val state: StateFlow<ExploreState> = _state

    private var dataLoaded = false

    fun loadData(force: Boolean = false) {
        if (!force && dataLoaded && _state.value.reportes.isNotEmpty()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                // Obtenemos la ciudad directamente de la fuente de verdad (Perfil en DB)
                val ciudadId = authRepository.getCiudadId().getOrNull()
                
                if (ciudadId == null) {
                    _state.value = _state.value.copy(isLoading = false, error = "No se pudo determinar la ciudad")
                    return@launch
                }

                val ciudadNombreDeferred = coroutineScope {
                    async { loadCiudadNombre(ciudadId) }
                }
                val reportesUIDeferred = coroutineScope {
                    async { loadReportesForCiudad(ciudadId) }
                }
                
                val nombre = ciudadNombreDeferred.await()
                val listaReportes = reportesUIDeferred.await()

                _state.value = _state.value.copy(
                    isLoading = false,
                    ciudadNombre = nombre,
                    reportes = listaReportes
                )
                dataLoaded = true
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
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
        } catch (_: Exception) {
            TIME_AGO_RECENT
        }
    }

    private suspend fun loadCiudadNombre(ciudadId: String): String {
        return ciudadRepository.getById(ciudadId).getOrNull()?.nombre ?: "Ubicacion desconocida"
    }

    private suspend fun loadReportesForCiudad(ciudadId: String): List<ReporteUI> = coroutineScope {
        val reportesCiudad = reporteRepository.getReporteByCiudadId(ciudadId).getOrNull().orEmpty()
        reportesCiudad.map { reporte ->
            async { mapReporteToUi(reporte) }
        }.awaitAll()
    }

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
