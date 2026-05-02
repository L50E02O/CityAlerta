package ec.cityalerta.app

import com.google.android.gms.maps.model.LatLng
import ec.cityalerta.app.model.data.Geometry
import ec.cityalerta.app.model.utils.GeoJsonConverter
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests unitarios para GeoJsonConverter.
 * Verifica la conversion de polígonos GeoJSON y la validacion de puntos dentro de polígonos.
 */
class GeoJsonConverterTest {

    @Test
    fun testExtractPolygonPoints_ValidGeometry() {
        // Arrange
        val geometry = Geometry(
            type = "Polygon",
            coordinates = listOf(
                listOf(
                    listOf(-80.0, -1.0),
                    listOf(-80.0, 0.0),
                    listOf(-79.0, 0.0),
                    listOf(-79.0, -1.0),
                    listOf(-80.0, -1.0)
                )
            )
        )

        // Act
        val points = GeoJsonConverter.extractPolygonPoints(geometry)

        // Assert
        assertEquals(5, points.size)
        assertEquals(LatLng(-1.0, -80.0), points[0])
    }

    @Test
    fun testExtractPolygonPoints_EmptyGeometry() {
        // Arrange
        val geometry = Geometry(type = "Polygon", coordinates = emptyList())

        // Act
        val points = GeoJsonConverter.extractPolygonPoints(geometry)

        // Assert
        assertTrue(points.isEmpty())
    }

    @Test
    fun testPointInPolygon_InsideSquare() {
        // Arrange
        val polygon = listOf(
            LatLng(-1.0, -80.0),
            LatLng(-1.0, -79.0),
            LatLng(0.0, -79.0),
            LatLng(0.0, -80.0),
            LatLng(-1.0, -80.0)
        )
        val point = LatLng(-0.5, -79.5)

        // Act
        val result = GeoJsonConverter.pointInPolygon(point, polygon)

        // Assert
        assertTrue(result)
    }

    @Test
    fun testPointInPolygon_OutsideSquare() {
        // Arrange
        val polygon = listOf(
            LatLng(-1.0, -80.0),
            LatLng(-1.0, -79.0),
            LatLng(0.0, -79.0),
            LatLng(0.0, -80.0),
            LatLng(-1.0, -80.0)
        )
        val point = LatLng(1.0, -80.0)

        // Act
        val result = GeoJsonConverter.pointInPolygon(point, polygon)

        // Assert
        assertFalse(result)
    }

    @Test
    fun testPointInPolygon_OnBoundary() {
        // Arrange
        val polygon = listOf(
            LatLng(-1.0, -80.0),
            LatLng(-1.0, -79.0),
            LatLng(0.0, -79.0),
            LatLng(0.0, -80.0),
            LatLng(-1.0, -80.0)
        )
        val pointOnEdge = LatLng(-1.0, -79.5)

        // Act
        val result = GeoJsonConverter.pointInPolygon(pointOnEdge, polygon)

        // Assert - El algoritmo ray casting trata puntos en el borde como fuera del poligono
        assertFalse(result)
    }

    @Test
    fun testPointInPolygon_TriangleTooSmall() {
        // Arrange - Polígono muy pequeño (menos de 3 puntos)
        val polygon = listOf(
            LatLng(-1.0, -80.0),
            LatLng(-1.0, -79.0)
        )
        val point = LatLng(-0.5, -79.5)

        // Act
        val result = GeoJsonConverter.pointInPolygon(point, polygon)

        // Assert
        assertFalse(result)
    }

    @Test
    fun testPointInPolygon_ComplexPolygon() {
        // Arrange - Polígono más complejo
        val polygon = listOf(
            LatLng(0.0, 0.0),
            LatLng(0.0, 2.0),
            LatLng(1.0, 3.0),
            LatLng(2.0, 2.0),
            LatLng(2.0, 0.0),
            LatLng(0.0, 0.0)
        )
        val pointInside = LatLng(1.0, 1.0)
        val pointOutside = LatLng(3.0, 1.0)

        // Act
        val resultInside = GeoJsonConverter.pointInPolygon(pointInside, polygon)
        val resultOutside = GeoJsonConverter.pointInPolygon(pointOutside, polygon)

        // Assert
        assertTrue(resultInside)
        assertFalse(resultOutside)
    }

    @Test
    fun testGeoJsonPolygonToGoogleMapsPolygon_ValidGeometry() {
        // Arrange
        val geometry = Geometry(
            type = "Polygon",
            coordinates = listOf(
                listOf(
                    listOf(-80.0, -1.0),
                    listOf(-80.0, 0.0),
                    listOf(-79.0, 0.0),
                    listOf(-79.0, -1.0),
                    listOf(-80.0, -1.0)
                )
            )
        )

        // Act
        val polygonOptions = GeoJsonConverter.geoJsonPolygonToGoogleMapsPolygon(geometry)

        // Assert
        assertNotNull(polygonOptions)
        assertEquals(0x4287F5CC, polygonOptions.fillColor)
        assertEquals(0xFF287FCC.toInt(), polygonOptions.strokeColor)
    }

    private fun assertNotNull(value: Any?) {
        assertTrue(value != null)
    }
}

