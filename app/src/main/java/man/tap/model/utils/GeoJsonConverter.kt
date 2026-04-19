package man.tap.model.utils

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import man.tap.model.data.Geometry

object GeoJsonConverter {

    fun geoJsonPolygonToGoogleMapsPolygon(geometry: Geometry): PolygonOptions {
        val polygonOptions = PolygonOptions()

        if (geometry.coordinates.isNotEmpty()) {
            val ring = geometry.coordinates[0]

            val points = ring.map { coord ->
                if (coord.size >= 2) {
                    LatLng(coord[1], coord[0])
                } else {
                    LatLng(0.0, 0.0)
                }
            }

            polygonOptions.addAll(points)
        }

        polygonOptions.fillColor(0x4287F5CC.toInt())
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

            if (point.longitude > minOf(p1.longitude, p2.longitude)) {
                if (point.longitude <= maxOf(p1.longitude, p2.longitude)) {
                    if (point.latitude <= maxOf(p1.latitude, p2.latitude)) {
                        if (p1.longitude != p2.longitude) {
                            val xinters =
                                (point.longitude - p1.longitude) * (p2.latitude - p1.latitude) / (p2.longitude - p1.longitude) + p1.latitude
                            if (p1.latitude == p2.latitude || point.latitude <= xinters) {
                                inside = !inside
                            }
                        }
                    }
                }
            }
            p1 = p2
        }

        return inside
    }

    fun extractPolygonPoints(geometry: Geometry): List<LatLng> {
        val points = mutableListOf<LatLng>()

        if (geometry.coordinates.isNotEmpty()) {
            val ring = geometry.coordinates[0]
            points.addAll(ring.map { coord ->
                if (coord.size >= 2) {
                    LatLng(coord[1], coord[0])
                } else {
                    LatLng(0.0, 0.0)
                }
            })
        }

        return points
    }
}
