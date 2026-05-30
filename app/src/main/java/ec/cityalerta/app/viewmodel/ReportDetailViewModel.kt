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
import ec.cityalerta.app.model.repository.BarrioRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

data class ReportDetailState(
    val isLoading: Boolean = false,
    val report: ReporteUI? = null,
    val error: String? = null
)

class ReportDetailViewModel(
    private val reporteRepository: ReporteRepository = ReporteRepository(),
    private val reporteImagenRepository: ReporteImagenRepository = ReporteImagenRepository(),
    private val reporteUbicacionRepository: ReporteUbicacionRepository = ReporteUbicacionRepository(),
    private val reporteStorageRepository: ReporteStorageRepository = ReporteStorageRepository(),
    private val barrioRepository: BarrioRepository = BarrioRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ReportDetailState())
    val state: StateFlow<ReportDetailState> = _state

    fun loadReportDetail(reportId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val reporte = reporteRepository.getById(reportId).getOrNull()
                if (reporte != null) {
                    val reportUi = mapReporteToUi(reporte)
                    _state.value = _state.value.copy(isLoading = false, report = reportUi)
                } else {
                    _state.value = _state.value.copy(isLoading = false, error = "Reporte no encontrado")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
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
            categoryType = reporte.categoria,
            imageUrl = imageUrl,
            barrio = barrio?.nombre ?: "Barrio desconocido",
            direccion = ubicacion?.direccion_aproximada ?: "Direccion no disponible",
            descripcion = reporte.descripcion,
            estado = mapEstadoLabel(reporte.estado),
            fecha = reporte.fecha_reporte,
            timeAgo = calculateTimeAgo(reporte.created_at),
            lat = ubicacion?.lat ?: 0.0,
            lng = ubicacion?.lng ?: 0.0,
            ciudadId = reporte.ciudad_id
        )
    }

    private fun calculateTimeAgo(createdAt: String?): String {
        if (createdAt.isNullOrBlank()) return "Hace poco"
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
                else -> "Hace poco"
            }
        } catch (_: Exception) {
            "Hace poco"
        }
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
