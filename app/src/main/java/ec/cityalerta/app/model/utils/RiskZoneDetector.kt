package ec.cityalerta.app.model.utils

import com.google.android.gms.maps.model.LatLng
import ec.cityalerta.app.model.data.map.BarrioRiskState
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacion
import kotlin.math.*

interface IRiskColorProvider {
    fun getColorForReportCount(count: Int): Int
}

class ExponentialRiskColorProvider(
    private val baseCount: Int = 5,
    private val baseColor: Int = 0x77FF0000.toInt(),
    private val maxColor: Int = 0xBB770000.toInt()
) : IRiskColorProvider {
    override fun getColorForReportCount(count: Int): Int {
        if (count < baseCount) return 0x00000000
        val doublings = floor(log2(count.toDouble() / baseCount)).toInt()
        return when {
            doublings <= 0 -> baseColor
            doublings == 1 -> 0x99CC0000.toInt()
            doublings == 2 -> 0xAA990000.toInt()
            else -> maxColor
        }
    }
}

interface IRiskZoneDetector {
    fun detectRiskZones(
        reports: List<Reporte>,
        ubicaciones: Map<String, ReporteUbicacion>
    ): List<BarrioRiskState>
}

class RadialRiskZoneDetector(
    private val colorProvider: IRiskColorProvider,
    private val radiusMeters: Double = 500.0,
    private val minReports: Int = 5
) : IRiskZoneDetector {
    
    override fun detectRiskZones(
        reports: List<Reporte>,
        ubicaciones: Map<String, ReporteUbicacion>
    ): List<BarrioRiskState> {
        val riskReports = reports.filter { it.categoria == ReportType.ZONA_DE_RIESGO }
        val locationsWithReports = riskReports.mapNotNull { report ->
            ubicaciones[report.ubicacion_id]?.let { it to report.id }
        }

        if (locationsWithReports.size < minReports) return emptyList()

        val result = mutableListOf<BarrioRiskState>()
        val processedReportIds = mutableSetOf<String>()

        for ((loc, reportId) in locationsWithReports) {
            if (reportId in processedReportIds) continue

            // Buscar todos los reportes cercanos
            val nearby = locationsWithReports.filter { (otherLoc, _) ->
                haversine(loc.lat, loc.lng, otherLoc.lat, otherLoc.lng) <= radiusMeters
            }

            if (nearby.size >= minReports) {
                val count = nearby.size
                val avgLat = nearby.map { it.first.lat }.average()
                val avgLng = nearby.map { it.first.lng }.average()
                val fillColor = colorProvider.getColorForReportCount(count)
                val strokeColor = (fillColor and 0x00FFFFFF) or 0xFF000000.toInt()

                result.add(BarrioRiskState(
                    barrioId = "zone_${reportId}",
                    nombre = "Zona de Riesgo",
                    center = LatLng(avgLat, avgLng),
                    radius = radiusMeters,
                    reportCount = count,
                    fillColor = fillColor,
                    strokeColor = strokeColor
                ))

                processedReportIds.addAll(nearby.map { it.second })
            }
        }

        return result
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
