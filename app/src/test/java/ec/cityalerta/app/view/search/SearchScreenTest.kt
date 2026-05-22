package ec.cityalerta.app.view.search

import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.viewmodel.SearchReportState
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests unitarios para la pantalla de busqueda y su estado asociado.
 */
class SearchScreenTest {

    @Test
    fun searchReportState_defaults() {
        val state = SearchReportState()

        assertFalse(state.isLoading)
        assertEquals("", state.searchQuery)
        assertNull(state.selectedCategory)
        assertTrue(state.reportes.isEmpty())
        assertNull(state.error)
        assertEquals("", state.ciudadId)
    }

    @Test
    fun categoryFilterRow_includesAllReportTypesPlusTodos() {
        val categories = listOf(null) + ReportType.entries.toList()

        assertEquals(ReportType.entries.size + 1, categories.size)
        assertNull(categories.first())
        assertTrue(categories.contains(ReportType.ZONA_DE_RIESGO))
        assertTrue(categories.contains(ReportType.BACHE))
        assertTrue(categories.contains(ReportType.AGUA))
        assertTrue(categories.contains(ReportType.LUZ))
    }

    @Test
    fun categoryFilterRow_labelsMatchViewModelMapping() {
        val labels = mapOf(
            null to "Todos",
            ReportType.ZONA_DE_RIESGO to "Seguridad",
            ReportType.BACHE to "Bache",
            ReportType.AGUA to "Agua",
            ReportType.LUZ to "Luz"
        )

        assertEquals("Todos", labels[null])
        assertEquals("Seguridad", labels[ReportType.ZONA_DE_RIESGO])
        assertEquals("Bache", labels[ReportType.BACHE])
        assertEquals("Agua", labels[ReportType.AGUA])
        assertEquals("Luz", labels[ReportType.LUZ])
    }

    @Test
    fun searchReportState_withFilters() {
        val state = SearchReportState(
            searchQuery = "centro",
            selectedCategory = ReportType.AGUA,
            ciudadId = "ciudad-uuid"
        )

        assertEquals("centro", state.searchQuery)
        assertEquals(ReportType.AGUA, state.selectedCategory)
        assertEquals("ciudad-uuid", state.ciudadId)
    }
}
