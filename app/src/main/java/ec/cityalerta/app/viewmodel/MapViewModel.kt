package ec.cityalerta.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.data.MapMarker
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.repository.BarrioRepository
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteUbicacionRepository
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.data.contracts.map.MapRepositoryContract
import ec.cityalerta.app.model.utils.GeoJsonConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.floor

data class BarrioRiskState(
    val barrioId: String,
    val nombre: String,
    val center: LatLng,
    val radius: Double,
    val reportCount: Int,
    val fillColor: Long,
    val strokeColor: Long = 0x88287FCCL
)

interface IRiskColorProvider {
    fun getColorForReportCount(count: Int): Long
}

class DefaultRiskColorProvider(
    private val thresholds: List<Pair<Int, Long>> = listOf(
        5 to 0x44FF0000L,
        10 to 0x66FF0000L,
        15 to 0x88CC0000L,
        20 to 0xAA990000L
    ),
    private val safeColor: Long = 0x00000000L
) : IRiskColorProvider {
    override fun getColorForReportCount(count: Int): Long {
        if (count < thresholds.first().first) return safeColor
        return thresholds.lastOrNull { count >= it.first }?.second ?: thresholds.last().second
    }
}

data class MapUiState(
    val ciudad: Ciudad? = null,
    val reportMarkers: List<MapMarker> = emptyList(),
    val barrioRisks: List<BarrioRiskState> = emptyList(),
    val reports: List<Reporte> = emptyList(),
    val selectedCategory: ReportType? = null,
    val selectedReport: Reporte? = null,
    val isMultiReport: Boolean = false,
    val reportCount: Int = 0,
    val categories: List<ReportType> = ReportType.entries,
    val cameraZoom: Float = 15f,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)

class MapViewModel(
    private val repository: MapRepositoryContract,
    private val reporteRepository: ReporteRepository,
    private val ubicacionRepository: ReporteUbicacionRepository,
    private val authRepository: AuthRepositoryContract,
    private val barrioRepository: BarrioRepository,
    private val colorProvider: IRiskColorProvider = DefaultRiskColorProvider()
) : ViewModel() {

    var uiState by mutableStateOf(MapUiState())
        private set

    private var polygonPoints: List<LatLng> = emptyList()

    fun loadCiudad(ciudadId: String) {
        uiState = uiState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val realCiudadId = when {
                    ciudadId == "current" || ciudadId == "Sin ciudad" || ciudadId.isBlank() -> {
                        authRepository.getCiudadId().getOrNull() ?: ""
                    }
                    ciudadId.length != 36 -> {
                        authRepository.buscarCiudadPorNombre(ciudadId).getOrNull() ?: ciudadId
                    }
                    else -> ciudadId
                }
                
                if (realCiudadId.isEmpty()) {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "No se pudo determinar la ciudad actual"
                    )
                    return@launch
                }

                val ciudad = repository.getCiudadById(realCiudadId)
                if (ciudad != null) {
                    val geometry = ciudad.geojson
                    polygonPoints = GeoJsonConverter.extractPolygonPoints(geometry)

                    val reportesResult = reporteRepository.getAll()
                    val ubicacionesResult = ubicacionRepository.getAll()

                    if (reportesResult.isSuccess && ubicacionesResult.isSuccess) {
                        val allUbicaciones = ubicacionesResult.getOrThrow().associateBy { it.id }
                        val allReportes = reportesResult.getOrThrow()
                        val filteredReports = allReportes
                            .filter { it.ciudad_id == realCiudadId }
                            .distinctBy { it.id }

                        val riskZones = withContext(Dispatchers.Default) {
                            val cellSize = 0.0015
                            val reportLocs = filteredReports.mapNotNull { allUbicaciones[it.ubicacion_id] }

                            if (reportLocs.isEmpty()) return@withContext emptyList<BarrioRiskState>()

                            val grid = mutableMapOf<Pair<Int, Int>, MutableList<LatLng>>()
                            reportLocs.forEach { loc ->
                                val cellX = floor(loc.lat / cellSize).toInt()
                                val cellY = floor(loc.lng / cellSize).toInt()
                                val key = Pair(cellX, cellY)
                                grid.getOrPut(key) { mutableListOf() }.add(LatLng(loc.lat, loc.lng))
                            }

                            grid.mapNotNull { (key, points) ->
                                val count = points.size
                                val color = colorProvider.getColorForReportCount(count)
                                if (color != 0x00000000L) {
                                    val avgLat = points.map { it.latitude }.average()
                                    val avgLng = points.map { it.longitude }.average()

                                    BarrioRiskState(
                                        barrioId = "zone_${key.first}_${key.second}",
                                        nombre = "Zona de Riesgo",
                                        center = LatLng(avgLat, avgLng),
                                        radius = 120.0,
                                        reportCount = count,
                                        fillColor = color
                                    )
                                } else null
                            }
                        }

                        val markers = filteredReports.mapNotNull { report ->
                            allUbicaciones[report.ubicacion_id]?.let { loc ->
                                MapMarker(
                                    id = report.id,
                                    latitude = loc.lat,
                                    longitude = loc.lng,
                                    title = report.categoria.name.replace("_", " "),
                                    description = report.descripcion
                                )
                            }
                        }.distinctBy { it.id }

                        uiState = uiState.copy(
                            ciudad = ciudad,
                            isLoading = false,
                            reportMarkers = markers,
                            barrioRisks = riskZones,
                            reports = filteredReports
                        )
                    } else {
                        uiState = uiState.copy(
                            ciudad = ciudad,
                            isLoading = false,
                            errorMessage = "Error al obtener datos de la base de datos"
                        )
                    }
                } else {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "Ciudad no encontrada en el sistema"
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Error cargando ciudad: ${e.message}"
                )
            }
        }
    }

    fun updateCameraZoom(zoom: Float) {
        uiState = uiState.copy(cameraZoom = zoom)
    }

    fun onCategorySelected(category: ReportType) {
        val newCategory = if (uiState.selectedCategory == category) null else category
        uiState = uiState.copy(
            selectedCategory = newCategory,
            selectedReport = null,
            isMultiReport = false,
            reportCount = 0
        )
    }

    fun onReportClicked(reportId: String) {
        val report = uiState.reports.firstOrNull { it.id == reportId }
        uiState = uiState.copy(
            selectedReport = report,
            isMultiReport = false,
            reportCount = 1
        )
    }

    fun onClusterClicked(reportType: ReportType, count: Int) {
        val dummyReport = Reporte(
            id = "cluster",
            usuario_id = "",
            ciudad_id = "",
            ubicacion_id = "",
            descripcion = "",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "",
            categoria = reportType,
            barrio_id = ""
        )
        uiState = uiState.copy(
            selectedReport = dummyReport,
            isMultiReport = true,
            reportCount = count
        )
    }

    fun onDismissReport(){
        uiState = uiState.copy(selectedReport = null, isMultiReport = false, reportCount = 0)
    }
}
