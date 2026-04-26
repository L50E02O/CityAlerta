package man.tap.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import man.tap.model.data.Ciudad
import man.tap.model.data.Feature
import man.tap.model.data.GeoJson
import man.tap.model.data.Geometry
import man.tap.model.data.MapMarker
import man.tap.model.data.Properties
import man.tap.model.repository.IMapRepository
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests unitarios para MapViewModel.
 * Verifica la logica de carga de ciudades, validacion de puntos y manejo de marcadores.
 */
class MapViewModelTest {

    @Mock
    private lateinit var mapRepository: IMapRepository

    private lateinit var viewModel: MapViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = MapViewModel(mapRepository)
    }

    @Test
    fun testLoadCiudad_Success() {
        // Arrange
        val testCiudad = createTestCiudad()
        whenever(mapRepository.getCiudadById("manta")).thenReturn(testCiudad)

        // Act
        viewModel.loadCiudad("manta")

        // Assert
        assertFalse(viewModel.uiState.isLoading)
        assertNotNull(viewModel.uiState.ciudad)
        assertEquals("Manta", viewModel.uiState.ciudad?.nombre)
        assertNull(viewModel.uiState.errorMessage)
    }

    @Test
    fun testLoadCiudad_NotFound() {
        // Arrange
        whenever(mapRepository.getCiudadById("inexistente")).thenReturn(null)

        // Act
        viewModel.loadCiudad("inexistente")

        // Assert
        assertFalse(viewModel.uiState.isLoading)
        assertNull(viewModel.uiState.ciudad)
        assertNotNull(viewModel.uiState.errorMessage)
        assertTrue(viewModel.uiState.errorMessage!!.contains("Ciudad no encontrada"))
    }

    @Test
    fun testLoadCiudad_Exception() {
        // Arrange - Simular excepcion lanzada por el repositorio
        whenever(mapRepository.getCiudadById("error")).thenThrow(RuntimeException("connection failed"))

        // Act
        viewModel.loadCiudad("error")

        // Assert
        assertFalse(viewModel.uiState.isLoading)
        assertNull(viewModel.uiState.ciudad)
        assertNotNull(viewModel.uiState.errorMessage)
        assertTrue(viewModel.uiState.errorMessage!!.contains("Error cargando ciudad:"))
    }

    @Test
    fun testOnMapClicked_InsidePolygon() {
        // Arrange
        val testCiudad = createTestCiudad()
        whenever(mapRepository.getCiudadById("manta")).thenReturn(testCiudad)
        whenever(mapRepository.getMarkers()).thenReturn(emptyList())
        viewModel.loadCiudad("manta")

        // Act - Click dentro del polígono (Manta)
        val pointInside = LatLng(-0.95, -80.73)
        viewModel.onMapClicked(pointInside)

        // Assert
        assertTrue(viewModel.uiState.isPointValid ?: false)
    }

    @Test
    fun testOnMapClicked_OutsidePolygon() {
        // Arrange
        val testCiudad = createTestCiudad()
        whenever(mapRepository.getCiudadById("manta")).thenReturn(testCiudad)
        viewModel.loadCiudad("manta")

        // Act - Click fuera del polígono
        val pointOutside = LatLng(0.0, 0.0)
        viewModel.onMapClicked(pointOutside)

        // Assert
        assertFalse(viewModel.uiState.isPointValid ?: true)
    }

    @Test
    fun testClearMarkers() {
        // Arrange
        whenever(mapRepository.getMarkers()).thenReturn(emptyList())

        // Act
        viewModel.clearMarkers()

        // Assert
        assertTrue(viewModel.uiState.marcadores.isEmpty())
    }

    @Test
    fun testUpdateCameraZoom() {
        // Arrange
        val newZoom = 18f

        // Act
        viewModel.updateCameraZoom(newZoom)

        // Assert
        assertEquals(newZoom, viewModel.uiState.cameraZoom)
    }

    @Test
    fun testRemoveMarker() {
        // Arrange
        val markerId = "marker_123"

        // Act
        viewModel.removeMarker(markerId)

        // Assert - Verify repository was called
        assertEquals(emptyList(), viewModel.uiState.marcadores)
    }

    /**
     * Crea una ciudad de prueba con geometría simple para testing.
     */
    private fun createTestCiudad(): Ciudad {
        val geometry = Geometry(
            type = "Polygon",
            coordinates = listOf(
                listOf(
                    listOf(-80.8, -1.0),
                    listOf(-80.8, -0.9),
                    listOf(-80.7, -0.9),
                    listOf(-80.7, -1.0),
                    listOf(-80.8, -1.0)
                )
            )
        )

        val feature = Feature(
            type = "Feature",
            properties = Properties(name = "Manta", country = "Ecuador"),
            geometry = geometry
        )

        val geoJson = GeoJson(
            type = "FeatureCollection",
            features = listOf(feature)
        )

        return Ciudad(
            id = "manta",
            nombre = "Manta",
            pais = "Ecuador",
            geojson = geoJson,
            centroLat = -0.95,
            centroLng = -80.73
        )
    }

    /**
     * Companion para evitar cambios de estado entre tests.
     */
    private companion object {
        val initialState = MapUiState()
    }
}



