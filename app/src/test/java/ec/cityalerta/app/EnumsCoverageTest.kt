package ec.cityalerta.app

import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import org.junit.Test
import kotlin.test.assertEquals

class EnumsCoverageTest {

    @Test
    fun testReportTypeToDisplayName() {
        assertEquals("Zona de riesgo", ReportType.ZONA_DE_RIESGO.toDisplayName())
        assertEquals("Bache", ReportType.BACHE.toDisplayName())
        assertEquals("Agua", ReportType.AGUA.toDisplayName())
        assertEquals("Luz", ReportType.LUZ.toDisplayName())
    }

    @Test
    fun testReporteEstadoEntries() {
        val estados = ReporteEstado.entries
        assertEquals(3, estados.size)
        assertEquals(ReporteEstado.PENDIENTE, estados[0])
        assertEquals(ReporteEstado.EN_PROCESO, estados[1])
        assertEquals(ReporteEstado.RESUELTO, estados[2])
    }

    @Test
    fun testReportTypeEntries() {
        val tipos = ReportType.entries
        assertEquals(4, tipos.size)
        assertEquals(ReportType.ZONA_DE_RIESGO, tipos[0])
        assertEquals(ReportType.BACHE, tipos[1])
        assertEquals(ReportType.AGUA, tipos[2])
        assertEquals(ReportType.LUZ, tipos[3])
    }
}
