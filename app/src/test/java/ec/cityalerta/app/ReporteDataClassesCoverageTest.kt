package ec.cityalerta.app

import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporte.ReporteCreateDto
import ec.cityalerta.app.model.data.reporte.ReporteUpdateDto
import ec.cityalerta.app.model.data.reporte.ReporteSearchResult
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.data.reporte.ReportType
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReporteDataClassesCoverageTest {

    @Test
    fun testReporte() {
        val reporte = Reporte(
            id = "reporte-1",
            usuario_id = "user-1",
            ciudad_id = "ciudad-1",
            ubicacion_id = "ubicacion-1",
            descripcion = "Test reporte",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-01",
            categoria = ReportType.BACHE,
            created_at = "2024-01-01",
            updated_at = "2024-01-02",
            barrio_id = "barrio-1"
        )
        assertEquals("reporte-1", reporte.id)
        assertEquals("user-1", reporte.usuario_id)
        assertEquals("ciudad-1", reporte.ciudad_id)
        assertEquals("ubicacion-1", reporte.ubicacion_id)
        assertEquals("Test reporte", reporte.descripcion)
        assertEquals(ReporteEstado.PENDIENTE, reporte.estado)
        assertEquals("2024-01-01", reporte.fecha_reporte)
        assertEquals(ReportType.BACHE, reporte.categoria)
        assertEquals("2024-01-01", reporte.created_at)
        assertEquals("2024-01-02", reporte.updated_at)
        assertEquals("barrio-1", reporte.barrio_id)
    }

    @Test
    fun testReporteWithoutDates() {
        val reporte = Reporte(
            id = "reporte-1",
            usuario_id = "user-1",
            ciudad_id = "ciudad-1",
            ubicacion_id = "ubicacion-1",
            descripcion = "Test reporte",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-01",
            categoria = ReportType.BACHE,
            barrio_id = "barrio-1"
        )
        assertNull(reporte.created_at)
        assertNull(reporte.updated_at)
    }

    @Test
    fun testReporteCreateDto() {
        val dto = ReporteCreateDto(
            usuario_id = "user-1",
            ciudad_id = "ciudad-1",
            ubicacion_id = "ubicacion-1",
            descripcion = "Test reporte",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-01",
            categoria = ReportType.BACHE,
            barrio_id = "barrio-1"
        )
        assertEquals("user-1", dto.usuario_id)
        assertEquals("ciudad-1", dto.ciudad_id)
        assertEquals("ubicacion-1", dto.ubicacion_id)
        assertEquals("Test reporte", dto.descripcion)
        assertEquals(ReporteEstado.PENDIENTE, dto.estado)
        assertEquals("2024-01-01", dto.fecha_reporte)
        assertEquals(ReportType.BACHE, dto.categoria)
        assertEquals("barrio-1", dto.barrio_id)
    }

    @Test
    fun testReporteUpdateDto() {
        val dto = ReporteUpdateDto(
            usuario_id = "user-1",
            ciudad_id = "ciudad-1",
            ubicacion_id = "ubicacion-1",
            descripcion = "Updated reporte",
            estado = ReporteEstado.EN_PROCESO,
            fecha_reporte = "2024-01-02",
            categoria = ReportType.AGUA,
            barrio_id = "barrio-1"
        )
        assertEquals("user-1", dto.usuario_id)
        assertEquals("ciudad-1", dto.ciudad_id)
        assertEquals("ubicacion-1", dto.ubicacion_id)
        assertEquals("Updated reporte", dto.descripcion)
        assertEquals(ReporteEstado.EN_PROCESO, dto.estado)
        assertEquals("2024-01-02", dto.fecha_reporte)
        assertEquals(ReportType.AGUA, dto.categoria)
        assertEquals("barrio-1", dto.barrio_id)
    }

    @Test
    fun testReporteUpdateDtoWithNulls() {
        val dto = ReporteUpdateDto()
        assertNull(dto.usuario_id)
        assertNull(dto.ciudad_id)
        assertNull(dto.ubicacion_id)
        assertNull(dto.descripcion)
        assertNull(dto.estado)
        assertNull(dto.fecha_reporte)
        assertNull(dto.categoria)
        assertNull(dto.barrio_id)
    }

    @Test
    fun testReporteSearchResult() {
        val reporte = Reporte(
            id = "reporte-1",
            usuario_id = "user-1",
            ciudad_id = "ciudad-1",
            ubicacion_id = "ubicacion-1",
            descripcion = "Test reporte",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-01",
            categoria = ReportType.BACHE,
            barrio_id = "barrio-1"
        )
        val result = ReporteSearchResult(
            reporte = reporte,
            barrioNombre = "Test Barrio",
            direccionAproximada = "Calle 123"
        )
        assertEquals(reporte, result.reporte)
        assertEquals("Test Barrio", result.barrioNombre)
        assertEquals("Calle 123", result.direccionAproximada)
    }

    @Test
    fun testReporteSearchResultWithNullDireccion() {
        val reporte = Reporte(
            id = "reporte-1",
            usuario_id = "user-1",
            ciudad_id = "ciudad-1",
            ubicacion_id = "ubicacion-1",
            descripcion = "Test reporte",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-01",
            categoria = ReportType.BACHE,
            barrio_id = "barrio-1"
        )
        val result = ReporteSearchResult(
            reporte = reporte,
            barrioNombre = "Test Barrio",
            direccionAproximada = null
        )
        assertEquals(reporte, result.reporte)
        assertEquals("Test Barrio", result.barrioNombre)
        assertNull(result.direccionAproximada)
    }
}
