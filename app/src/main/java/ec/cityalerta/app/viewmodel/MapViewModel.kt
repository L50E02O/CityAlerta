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
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteUbicacionRepository
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.data.contracts.map.MapRepositoryContract
import ec.cityalerta.app.model.utils.GeoJsonConverter
import kotlinx.coroutines.launch

data class MapUiState(
    val ciudad: Ciudad? = null,
    val reportMarkers: List<MapMarker> = emptyList(),
    val reports: List<Reporte> = emptyList(),
    val selectedCategory: ReportType? = null,
    val selectedReport: Reporte? = null,
    val categories: List<ReportType> = ReportType.entries,
    val cameraZoom: Float = 15f,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)

class MapViewModel(
    private val repository: MapRepositoryContract,
    private val reporteRepository: ReporteRepository,
    private val ubicacionRepository: ReporteUbicacionRepository,
    private val authRepository: AuthRepositoryContract
) : ViewModel() {

    var uiState by mutableStateOf(MapUiState())
        private set

    private var polygonPoints: List<LatLng> = emptyList()

    fun loadCiudad(ciudadId: String) {
        uiState = uiState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val realCiudadId = if (ciudadId.length != 36) {
                    authRepository.buscarCiudadPorNombre(ciudadId).getOrNull() ?: ciudadId
                } else {
                    ciudadId
                }
                
                val ciudad = repository.getCiudadById(realCiudadId)
                if (ciudad != null) {
                    val geometry = ciudad.geojson
                    polygonPoints = GeoJsonConverter.extractPolygonPoints(geometry)

                    val reportesResult = reporteRepository.getAll()
                    val ubicacionesResult = ubicacionRepository.getAll()

                    if (reportesResult.isSuccess && ubicacionesResult.isSuccess) {
                        val allReportes = reportesResult.getOrThrow()
                        val allUbicaciones = ubicacionesResult.getOrThrow().associateBy { it.id }

                        val filteredReports = allReportes.filter { it.ciudad_id == realCiudadId }

                        // Convertir a marcadores de mapa
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
                        }

                        uiState = uiState.copy(
                            ciudad = ciudad,
                            isLoading = false,
                            reportMarkers = markers,
                            reports = filteredReports
                        )
                    } else {
                        uiState = uiState.copy(
                            ciudad = ciudad,
                            isLoading = false,
                            errorMessage = "Error al obtener reportes de la base de datos"
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
        uiState = uiState.copy(selectedCategory = newCategory)
    }

    fun onReportClicked(reportId: String) {
        val report = uiState.reports.firstOrNull { it.id == reportId }
        uiState = uiState.copy(selectedReport = report)
    }

    fun onDismissReport(){
        uiState = uiState.copy(selectedReport = null)
    }
}

