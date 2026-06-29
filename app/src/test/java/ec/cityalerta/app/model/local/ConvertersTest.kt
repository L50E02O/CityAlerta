package ec.cityalerta.app.model.local

import ec.cityalerta.app.model.data.geoJson.Geometry
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun testFromGeometry_withNull() {
        val result = converters.fromGeometry(null)

        assertNull(result)
    }

    @Test
    fun testToGeometry_withNull() {
        val result = converters.toGeometry(null)

        assertNull(result)
    }

    @Test
    fun testFromReportType_withValidType() {
        val result = converters.fromReportType(ReportType.BACHE)

        assertEquals("BACHE", result)
    }

    @Test
    fun testFromReportType_withNull() {
        val result = converters.fromReportType(null)

        assertNull(result)
    }

    @Test
    fun testToReportType_withValidString() {
        val result = converters.toReportType("BACHE")

        assertEquals(ReportType.BACHE, result)
    }

    @Test
    fun testToReportType_withNull() {
        val result = converters.toReportType(null)

        assertNull(result)
    }

    @Test
    fun testToReportType_withAllTypes() {
        assertEquals(ReportType.BACHE, converters.toReportType("BACHE"))
        assertEquals(ReportType.AGUA, converters.toReportType("AGUA"))
        assertEquals(ReportType.LUZ, converters.toReportType("LUZ"))
        assertEquals(ReportType.ZONA_DE_RIESGO, converters.toReportType("ZONA_DE_RIESGO"))
    }

    @Test
    fun testFromReporteEstado_withValidEstado() {
        val result = converters.fromReporteEstado(ReporteEstado.PENDIENTE)

        assertEquals("PENDIENTE", result)
    }

    @Test
    fun testFromReporteEstado_withNull() {
        val result = converters.fromReporteEstado(null)

        assertNull(result)
    }

    @Test
    fun testToReporteEstado_withValidString() {
        val result = converters.toReporteEstado("PENDIENTE")

        assertEquals(ReporteEstado.PENDIENTE, result)
    }

    @Test
    fun testToReporteEstado_withNull() {
        val result = converters.toReporteEstado(null)

        assertNull(result)
    }

    @Test
    fun testToReporteEstado_withAllEstados() {
        assertEquals(ReporteEstado.PENDIENTE, converters.toReporteEstado("PENDIENTE"))
        assertEquals(ReporteEstado.EN_PROCESO, converters.toReporteEstado("EN_PROCESO"))
        assertEquals(ReporteEstado.RESUELTO, converters.toReporteEstado("RESUELTO"))
    }


    @Test
    fun testReportTypeRoundTrip() {
        val originalType = ReportType.BACHE

        val string = converters.fromReportType(originalType)
        val decodedType = converters.toReportType(string)

        assertEquals(originalType, decodedType)
    }

    @Test
    fun testReporteEstadoRoundTrip() {
        val originalEstado = ReporteEstado.EN_PROCESO

        val string = converters.fromReporteEstado(originalEstado)
        val decodedEstado = converters.toReporteEstado(string)

        assertEquals(originalEstado, decodedEstado)
    }
}
