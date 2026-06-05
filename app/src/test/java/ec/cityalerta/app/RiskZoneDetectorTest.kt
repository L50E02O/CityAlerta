package ec.cityalerta.app

import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacion
import ec.cityalerta.app.model.utils.ExponentialRiskColorProvider
import ec.cityalerta.app.model.utils.IRiskColorProvider
import ec.cityalerta.app.model.utils.RadialRiskZoneDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RiskZoneDetectorTest {

    @Test
    fun testExponentialRiskColorProvider() {
        val provider = ExponentialRiskColorProvider(baseCount = 5)
        
        // Less than base count -> Transparent
        assertEquals(0x00000000, provider.getColorForReportCount(4))
        
        // Exactly base count -> base color
        assertEquals(0x77FF0000.toInt(), provider.getColorForReportCount(5))
        
        // Doublings
        // 5 * 2 = 10 -> doublings = 1
        assertEquals(0x99CC0000.toInt(), provider.getColorForReportCount(10))
        
        // 5 * 4 = 20 -> doublings = 2
        assertEquals(0xAA990000.toInt(), provider.getColorForReportCount(20))
        
        // More than 2 doublings
        assertEquals(0xBB770000.toInt(), provider.getColorForReportCount(40))
    }

    @Test
    fun testRadialRiskZoneDetector_NoReports() {
        val mockColorProvider = object : IRiskColorProvider {
            override fun getColorForReportCount(count: Int): Int = 0x77FF0000.toInt()
        }
        val detector = RadialRiskZoneDetector(mockColorProvider, minReports = 2)
        
        val zones = detector.detectRiskZones(emptyList(), emptyMap())
        assertTrue(zones.isEmpty())
    }

    @Test
    fun testRadialRiskZoneDetector_Detection() {
        val mockColorProvider = object : IRiskColorProvider {
            override fun getColorForReportCount(count: Int): Int = 0x77FF0000.toInt()
        }
        val detector = RadialRiskZoneDetector(mockColorProvider, radiusMeters = 1000.0, minReports = 2)

        val report1 = createRiskReport("r1", "u1")
        val report2 = createRiskReport("r2", "u2")
        val report3 = createRiskReport("r3", "u3") // Far away
        
        val ubicaciones = mapOf(
            "u1" to ReporteUbicacion("u1", -1.0, -80.0, "Direccion 1"),
            "u2" to ReporteUbicacion("u2", -1.001, -80.001, "Direccion 2"), // ~150m away
            "u3" to ReporteUbicacion("u3", 0.0, 0.0, "Direccion 3") // Very far
        )

        val zones = detector.detectRiskZones(listOf(report1, report2, report3), ubicaciones)
        
        assertEquals(1, zones.size)
        assertEquals(2, zones[0].reportCount)
        assertEquals("Zona de Riesgo", zones[0].nombre)
        assertEquals(0x77FF0000.toInt(), zones[0].fillColor)
        // Stroke color calculation: (fillColor and 0x00FFFFFF) or 0xFF000000.toInt()
        assertEquals(0xFFFF0000.toInt(), zones[0].strokeColor)
    }

    @Test
    fun testRadialRiskZoneDetector_SkipNonRiskReports() {
        val detector = RadialRiskZoneDetector(ExponentialRiskColorProvider(), minReports = 1)
        
        val report = Reporte(
            id = "r1",
            usuario_id = "user1",
            ciudad_id = "c1",
            ubicacion_id = "u1",
            descripcion = "Bache",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-01",
            categoria = ReportType.BACHE, // NOT ZONA_DE_RIESGO
            barrio_id = "b1"
        )
        val ubicaciones = mapOf("u1" to ReporteUbicacion("u1", -1.0, -80.0, "Direccion 1"))
        
        val zones = detector.detectRiskZones(listOf(report), ubicaciones)
        assertTrue(zones.isEmpty())
    }

    private fun createRiskReport(id: String, ubicacionId: String): Reporte {
        return Reporte(
            id = id,
            usuario_id = "user1",
            ciudad_id = "c1",
            ubicacion_id = ubicacionId,
            descripcion = "Riesgo",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-01",
            categoria = ReportType.ZONA_DE_RIESGO,
            barrio_id = "b1"
        )
    }
}
