package ec.cityalerta.app

import ec.cityalerta.app.model.data.MockData
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MockDataTest {

    @Test
    fun getMantaCity_returnsMantaEcuador() {
        val ciudad = MockData.getMantaCity()

        assertEquals("manta", ciudad.id)
        assertEquals("Manta", ciudad.nombre)
        assertEquals("Ecuador", ciudad.pais)
        assertEquals(-0.95, ciudad.centroLat)
        assertEquals(-80.73, ciudad.centroLng)
        assertEquals("Polygon", ciudad.geojson.type)
        assertTrue(ciudad.geojson.coordinates.isNotEmpty())
    }

    @Test
    fun getMockReportMarkers_returnsFourMarkers() {
        val markers = MockData.getMockReportMarkers()

        assertEquals(4, markers.size)
        markers.forEach { marker ->
            assertNotNull(marker.id)
            assertTrue(marker.title.isNotBlank())
            assertNotNull(marker.description)
        }
    }
}
