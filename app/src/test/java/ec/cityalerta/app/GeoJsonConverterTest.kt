package ec.cityalerta.app

import com.google.android.gms.maps.model.LatLng
import ec.cityalerta.app.model.data.geoJson.Geometry
import ec.cityalerta.app.model.utils.GeoJsonConverter
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GeoJsonConverterTest {

    @Test
    fun extractPolygonPoints_validGeometry() {
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

        val points = GeoJsonConverter.extractPolygonPoints(geometry)

        assertEquals(5, points.size)
        assertEquals(LatLng(-1.0, -80.0), points[0])
    }

    @Test
    fun extractPolygonPoints_emptyGeometry() {
        val geometry = Geometry(type = "Polygon", coordinates = emptyList())

        val points = GeoJsonConverter.extractPolygonPoints(geometry)

        assertTrue(points.isEmpty())
    }

    @Test
    fun pointInPolygon_insideSquare() {
        val polygon = listOf(
            LatLng(-1.0, -80.0),
            LatLng(-1.0, -79.0),
            LatLng(0.0, -79.0),
            LatLng(0.0, -80.0),
            LatLng(-1.0, -80.0)
        )
        val point = LatLng(-0.5, -79.5)

        assertTrue(GeoJsonConverter.pointInPolygon(point, polygon))
    }

    @Test
    fun pointInPolygon_outsideSquare() {
        val polygon = listOf(
            LatLng(-1.0, -80.0),
            LatLng(-1.0, -79.0),
            LatLng(0.0, -79.0),
            LatLng(0.0, -80.0),
            LatLng(-1.0, -80.0)
        )
        val point = LatLng(1.0, -80.0)

        assertFalse(GeoJsonConverter.pointInPolygon(point, polygon))
    }

    @Test
    fun pointInPolygon_onBoundary() {
        val polygon = listOf(
            LatLng(-1.0, -80.0),
            LatLng(-1.0, -79.0),
            LatLng(0.0, -79.0),
            LatLng(0.0, -80.0),
            LatLng(-1.0, -80.0)
        )
        val pointOnEdge = LatLng(-1.0, -79.5)

        assertFalse(GeoJsonConverter.pointInPolygon(pointOnEdge, polygon))
    }

    @Test
    fun pointInPolygon_triangleTooSmall() {
        val polygon = listOf(
            LatLng(-1.0, -80.0),
            LatLng(-1.0, -79.0)
        )
        val point = LatLng(-0.5, -79.5)

        assertFalse(GeoJsonConverter.pointInPolygon(point, polygon))
    }

    @Test
    fun pointInPolygon_complexPolygon() {
        val polygon = listOf(
            LatLng(0.0, 0.0),
            LatLng(0.0, 2.0),
            LatLng(1.0, 3.0),
            LatLng(2.0, 2.0),
            LatLng(2.0, 0.0),
            LatLng(0.0, 0.0)
        )

        assertTrue(GeoJsonConverter.pointInPolygon(LatLng(1.0, 1.0), polygon))
        assertFalse(GeoJsonConverter.pointInPolygon(LatLng(3.0, 1.0), polygon))
    }

    @Test
    fun extractPolygonPoints_skipsInvalidCoordinates() {
        val geometry = Geometry(
            type = "Polygon",
            coordinates = listOf(
                listOf(
                    listOf(-80.0),
                    listOf(-80.0, -1.0),
                    listOf(-79.0, -1.0)
                )
            )
        )

        val points = GeoJsonConverter.extractPolygonPoints(geometry)

        assertEquals(2, points.size)
    }

    @Test
    fun geoJsonPolygonToGoogleMapsPolygon_validGeometry() {
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

        val polygonOptions = GeoJsonConverter.geoJsonPolygonToGoogleMapsPolygon(geometry)

        assertNotNull(polygonOptions)
        assertEquals(0x4287F5CC, polygonOptions.fillColor)
        assertEquals(0xFF287FCC.toInt(), polygonOptions.strokeColor)
    }
}
