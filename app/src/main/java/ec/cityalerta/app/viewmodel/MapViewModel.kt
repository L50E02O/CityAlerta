package ec.cityalerta.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.data.MapMarker
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.repository.interfaces.IMapRepository
import ec.cityalerta.app.model.utils.GeoJsonConverter
import java.util.Locale

data class MapUiState(
    val ciudad: Ciudad? = null,
    val marcadores: List<MapMarker> = emptyList(),
    val reportMarkers: List<MapMarker> = emptyList(),
    val reports: List<Reporte> = emptyList(),
    val selectedCategory: ReportType? = null,
    val selectedReport: Reporte? = null,
    val categories: List<ReportType> = ReportType.entries,
    val cameraZoom: Float = 15f,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isPointValid: Boolean? = null
)

class MapViewModel(private val repository: IMapRepository) : ViewModel() {

    var uiState by mutableStateOf(MapUiState())
        private set

    private var polygonPoints: List<LatLng> = emptyList()

    fun loadCiudad(ciudadId: String) {
        uiState = uiState.copy(isLoading = true, errorMessage = null)

        try {
            val ciudad = repository.getCiudadById(ciudadId)
            if (ciudad != null) {
                val geometry = ciudad.geojson
                if (geometry.coordinates.isEmpty()) {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "Geometria de ciudad no disponible"
                    )
                    return
                }

                // Extraer puntos del poligono para validacion
                polygonPoints = GeoJsonConverter.extractPolygonPoints(geometry)

                uiState = uiState.copy(
                    ciudad = ciudad,
                    isLoading = false,
                    marcadores = emptyList(),
                    reportMarkers = ec.cityalerta.app.model.data.MockData.getMockReportMarkers(),
                    reports = ec.cityalerta.app.model.data.MockData.getMockReports()
                )
            } else {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Ciudad no encontrada: $ciudadId"
                )
            }
        } catch (e: Exception) {
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "Error cargando ciudad: ${e.message}"
            )
        }
    }

    fun onMapClicked(latLng: LatLng) {
        // Validar si el punto esta dentro del poligono
        val isPointInside = GeoJsonConverter.pointInPolygon(latLng, polygonPoints)
        uiState = uiState.copy(isPointValid = isPointInside)

        if (isPointInside) {
            // Crear marcador con validacion
            val marker = MapMarker(
                id = "marker_${System.currentTimeMillis()}",
                latitude = latLng.latitude,
                longitude = latLng.longitude,
                title = "Marcador ${repository.getMarkers().size + 1}",
                description = "Lat: ${String.format(Locale.US, "%.4f", latLng.latitude)}, " +
                    "Lng: ${String.format(Locale.US, "%.4f", latLng.longitude)}"
            )

            // Agregar marcador validado
            repository.addMarker(marker)
            uiState = uiState.copy(marcadores = repository.getMarkers())
        }
    }

    fun removeMarker(markerId: String) {
        repository.removeMarker(markerId)
        uiState = uiState.copy(marcadores = repository.getMarkers())
    }

    fun updateCameraZoom(zoom: Float) {
        uiState = uiState.copy(cameraZoom = zoom)
    }

    fun clearMarkers() {
        repository.clearMarkers()
        uiState = uiState.copy(marcadores = emptyList())
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

