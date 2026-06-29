package ec.cityalerta.app

import ec.cityalerta.app.model.data.Usuario
import ec.cityalerta.app.model.data.location.UserLocation
import ec.cityalerta.app.model.data.map.BarrioRiskState
import ec.cityalerta.app.model.data.geoJson.Feature
import ec.cityalerta.app.model.data.geoJson.GeoJson
import ec.cityalerta.app.model.data.geoJson.Geometry
import ec.cityalerta.app.model.data.geoJson.Properties
import com.google.android.gms.maps.model.LatLng
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DataClassesCoverageTest {

    @Test
    fun testUsuario() {
        val usuario = Usuario(
            id = "user-1",
            email = "test@example.com"
        )
        assertEquals("user-1", usuario.id)
        assertEquals("test@example.com", usuario.email)
    }

    @Test
    fun testUserLocation() {
        val location = UserLocation(
            latitude = -0.1807,
            longitude = -78.4678
        )
        assertEquals(-0.1807, location.latitude)
        assertEquals(-78.4678, location.longitude)
    }

    @Test
    fun testBarrioRiskState() {
        val center = LatLng(-0.1807, -78.4678)
        val riskState = BarrioRiskState(
            barrioId = "barrio-1",
            nombre = "Test Barrio",
            center = center,
            radius = 500.0,
            reportCount = 10,
            fillColor = 0x77FF0000.toInt(),
            strokeColor = 0xBB770000.toInt()
        )
        assertEquals("barrio-1", riskState.barrioId)
        assertEquals("Test Barrio", riskState.nombre)
        assertEquals(center, riskState.center)
        assertEquals(500.0, riskState.radius)
        assertEquals(10, riskState.reportCount)
        assertEquals(0x77FF0000.toInt(), riskState.fillColor)
        assertEquals(0xBB770000.toInt(), riskState.strokeColor)
    }

    @Test
    fun testGeoJson() {
        val geoJson = GeoJson(
            type = "FeatureCollection",
            features = emptyList()
        )
        assertEquals("FeatureCollection", geoJson.type)
        assertEquals(emptyList(), geoJson.features)
    }

    @Test
    fun testFeature() {
        val properties = Properties(
            name = "Test Feature",
            country = "Ecuador"
        )
        val geometry = Geometry(
            type = "Polygon",
            coordinates = listOf(listOf(listOf(0.0, 0.0)))
        )
        val feature = Feature(
            type = "Feature",
            properties = properties,
            geometry = geometry
        )
        assertEquals("Feature", feature.type)
        assertEquals(properties, feature.properties)
        assertEquals(geometry, feature.geometry)
    }

    @Test
    fun testProperties() {
        val properties = Properties(
            name = "Test",
            country = "Ecuador"
        )
        assertEquals("Test", properties.name)
        assertEquals("Ecuador", properties.country)
    }

    @Test
    fun testGeometry() {
        val coordinates = listOf(listOf(listOf(0.0, 0.0), listOf(1.0, 1.0)))
        val geometry = Geometry(
            type = "Polygon",
            coordinates = coordinates
        )
        assertEquals("Polygon", geometry.type)
        assertEquals(coordinates, geometry.coordinates)
    }
}
