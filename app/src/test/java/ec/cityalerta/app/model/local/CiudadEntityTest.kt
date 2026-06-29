package ec.cityalerta.app.model.local

import ec.cityalerta.app.model.data.geoJson.Geometry
import org.junit.Test
import kotlin.test.assertEquals

class CiudadEntityTest {

    @Test
    fun testCiudadEntityCreation() {
        val geometry = Geometry(
            type = "Polygon",
            coordinates = listOf(listOf(listOf(-80.0, -1.0), listOf(-81.0, -2.0)))
        )
        val ciudadEntity = CiudadEntity(
            id = "ciudad-123",
            nombre = "Quito",
            pais = "Ecuador",
            geojson = geometry,
            centroLat = -0.1807,
            centroLng = -78.4678
        )

        assertEquals("ciudad-123", ciudadEntity.id)
        assertEquals("Quito", ciudadEntity.nombre)
        assertEquals("Ecuador", ciudadEntity.pais)
        assertEquals(geometry, ciudadEntity.geojson)
        assertEquals(-0.1807, ciudadEntity.centroLat)
        assertEquals(-78.4678, ciudadEntity.centroLng)
    }

    @Test
    fun testCiudadEntityWithDifferentCoordinates() {
        val geometry = Geometry(
            type = "Polygon",
            coordinates = listOf(
                listOf(listOf(-2.0, 0.0), listOf(-2.1, 0.1), listOf(-2.2, 0.2))
            )
        )
        val ciudadEntity = CiudadEntity(
            id = "ciudad-456",
            nombre = "Guayaquil",
            pais = "Ecuador",
            geojson = geometry,
            centroLat = -2.1897,
            centroLng = -79.8847
        )

        assertEquals("ciudad-456", ciudadEntity.id)
        assertEquals("Guayaquil", ciudadEntity.nombre)
        assertEquals(-2.1897, ciudadEntity.centroLat)
        assertEquals(-79.8847, ciudadEntity.centroLng)
    }

    @Test
    fun testCiudadEntityWithEmptyGeometry() {
        val geometry = Geometry(
            type = "Polygon",
            coordinates = emptyList()
        )
        val ciudadEntity = CiudadEntity(
            id = "ciudad-789",
            nombre = "Manta",
            pais = "Ecuador",
            geojson = geometry,
            centroLat = -0.9554,
            centroLng = -80.7294
        )

        assertEquals("ciudad-789", ciudadEntity.id)
        assertEquals("Manta", ciudadEntity.nombre)
        assertEquals(emptyList<List<List<Double>>>(), ciudadEntity.geojson.coordinates)
    }

    @Test
    fun testCiudadEntityWithZeroCoordinates() {
        val geometry = Geometry(
            type = "Polygon",
            coordinates = listOf(listOf(listOf(0.0, 0.0)))
        )
        val ciudadEntity = CiudadEntity(
            id = "ciudad-000",
            nombre = "Test City",
            pais = "Test Country",
            geojson = geometry,
            centroLat = 0.0,
            centroLng = 0.0
        )

        assertEquals(0.0, ciudadEntity.centroLat)
        assertEquals(0.0, ciudadEntity.centroLng)
    }
}
