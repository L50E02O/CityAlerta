package man.tap.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import man.tap.model.data.Ciudad
import man.tap.model.data.MapMarker
import man.tap.model.repository.IMapRepository
import man.tap.model.utils.GeoJsonConverter
import java.util.Locale

data class MapUiState(
    val ciudad: Ciudad? = null,
    val marcadores: List<MapMarker> = emptyList(),
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
                val geometry = ciudad.geojson.features.firstOrNull()?.geometry
                if (geometry == null) {
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
                    marcadores = emptyList()
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
}

