package ec.cityalerta.app.model.utils

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import ec.cityalerta.app.model.data.Geometry

object GeoJsonConverter {

    fun geoJsonPolygonToGoogleMapsPolygon(geometry: Geometry): PolygonOptions {
        val polygonOptions = PolygonOptions()

        if (geometry.coordinates.isNotEmpty()) {
            val ring = geometry.coordinates[0]

            val points = ring.mapNotNull { coord ->
                if (coord.size >= 2) LatLng(coord[1], coord[0]) else null
            }

            polygonOptions.addAll(points)
        }

        polygonOptions.fillColor(0x4287F5CC)
        polygonOptions.strokeColor(0xFF287FCC.toInt())
        polygonOptions.strokeWidth(2f)

        return polygonOptions
    }

    fun pointInPolygon(point: LatLng, polygon: List<LatLng>): Boolean {
        if (polygon.size < 3) return false

        var inside = false
        var p1 = polygon[0]

        for (i in 1..polygon.size) {
            val p2 = polygon[i % polygon.size]

            if (shouldCheckIntersection(point, p1, p2) && hasRayIntersection(point, p1, p2)) {
                inside = !inside
            }
            p1 = p2
        }

        return inside
    }

    private fun shouldCheckIntersection(point: LatLng, p1: LatLng, p2: LatLng): Boolean {
        return point.longitude > minOf(p1.longitude, p2.longitude) &&
                point.longitude <= maxOf(p1.longitude, p2.longitude) &&
                point.latitude <= maxOf(p1.latitude, p2.latitude) &&
                p1.longitude != p2.longitude
    }

    private fun hasRayIntersection(point: LatLng, p1: LatLng, p2: LatLng): Boolean {
        val xinters = (point.longitude - p1.longitude) * (p2.latitude - p1.latitude) /
                      (p2.longitude - p1.longitude) + p1.latitude
        return p1.latitude == p2.latitude || point.latitude <= xinters
    }

    fun extractPolygonPoints(geometry: Geometry): List<LatLng> {
        val points = mutableListOf<LatLng>()

        if (geometry.coordinates.isNotEmpty()) {
            val ring = geometry.coordinates[0]
            points.addAll(ring.mapNotNull { coord ->
                if (coord.size >= 2) LatLng(coord[1], coord[0]) else null
            })
        }

        return points
    }
}
