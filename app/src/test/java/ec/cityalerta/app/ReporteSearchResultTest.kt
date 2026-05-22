package ec.cityalerta.app

import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.data.reporte.ReporteSearchResult
import ec.cityalerta.app.testdoubles.SearchReportTestFixtures
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ReporteSearchResultTest {

    @Test
    fun reporteSearchResult_holdsAllFields() {
        val reporte = SearchReportTestFixtures.sampleReporte()
        val result = ReporteSearchResult(
            reporte = reporte,
            barrioNombre = "Barrio Centro",
            direccionAproximada = "Calle 10 y Av. Principal"
        )

        assertEquals(reporte, result.reporte)
        assertEquals("Barrio Centro", result.barrioNombre)
        assertEquals("Calle 10 y Av. Principal", result.direccionAproximada)
    }

    @Test
    fun reporteSearchResult_allowsNullDireccion() {
        val result = SearchReportTestFixtures.sampleSearchResult(direccionAproximada = null)

        assertNull(result.direccionAproximada)
        assertNotNull(result.reporte)
    }

    @Test
    fun reporteSearchResult_copyPreservesReporte() {
        val original = SearchReportTestFixtures.sampleSearchResult()
        val modified = original.copy(barrioNombre = "Nuevo barrio")

        assertEquals("Nuevo barrio", modified.barrioNombre)
        assertEquals(original.reporte.id, modified.reporte.id)
        assertEquals("Centro", original.barrioNombre)
    }

    @Test
    fun reporteSearchResult_equality() {
        val reporte = SearchReportTestFixtures.sampleReporte(
            id = "r1",
            categoria = ReportType.LUZ
        )
        val a = ReporteSearchResult(reporte, "Centro", "Dir 1")
        val b = ReporteSearchResult(reporte, "Centro", "Dir 1")

        assertEquals(a, b)
        assertEquals(ReporteEstado.PENDIENTE, a.reporte.estado)
    }
}
