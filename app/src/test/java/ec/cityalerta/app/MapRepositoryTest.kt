package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.MapMarker
import ec.cityalerta.app.model.data.MockData
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests unitarios para MapRepository.
 * Verifica operaciones de obtener ciudades, agregar y remover marcadores.
 */
class MapRepositoryTest {

    private lateinit var repository: MapRepository

    @Before
    fun setUp() {
        repository = MapRepository()
    }

    @Test
    fun testGetCiudadById_Manta() {
        // Act
        val ciudad = repository.getCiudadById("manta")

        // Assert
        assertNotNull(ciudad)
        assertEquals("Manta", ciudad?.nombre)
        assertEquals("Ecuador", ciudad?.pais)
    }

    @Test
    fun testGetCiudadById_NotFound() {
        // Act
        val ciudad = repository.getCiudadById("inexistente")

        // Assert
        assertEquals(null, ciudad)
    }

    @Test
    fun testAddMarker() {
        // Arrange
        val marker = MapMarker(
            id = "marker_123",
            latitude = -0.95,
            longitude = -80.73,
            title = "Test Marker"
        )

        // Act
        repository.addMarker(marker)
        val markers = repository.getMarkers()

        // Assert
        assertEquals(1, markers.size)
        assertEquals("marker_123", markers[0].id)
    }

    @Test
    fun testRemoveMarker() {
        // Arrange
        val marker1 = MapMarker("marker_1", -0.95, -80.73, "Marker 1")
        val marker2 = MapMarker("marker_2", -0.94, -80.72, "Marker 2")
        repository.addMarker(marker1)
        repository.addMarker(marker2)

        // Act
        repository.removeMarker("marker_1")
        val markers = repository.getMarkers()

        // Assert
        assertEquals(1, markers.size)
        assertEquals("marker_2", markers[0].id)
    }

    @Test
    fun testClearMarkers() {
        // Arrange
        repository.addMarker(MapMarker("marker_1", -0.95, -80.73, "Marker 1"))
        repository.addMarker(MapMarker("marker_2", -0.94, -80.72, "Marker 2"))

        // Act
        repository.clearMarkers()
        val markers = repository.getMarkers()

        // Assert
        assertTrue(markers.isEmpty())
    }

    @Test
    fun testGetMarkers_Empty() {
        // Act
        val markers = repository.getMarkers()

        // Assert
        assertTrue(markers.isEmpty())
    }

    @Test
    fun testGetMarkers_Multiple() {
        // Arrange
        repeat(3) { index ->
            repository.addMarker(
                MapMarker("marker_$index", -0.95 - index * 0.01, -80.73, "Marker $index")
            )
        }

        // Act
        val markers = repository.getMarkers()

        // Assert
        assertEquals(3, markers.size)
    }
}

