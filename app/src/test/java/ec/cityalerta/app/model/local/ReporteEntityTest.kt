package ec.cityalerta.app.model.local

import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReporteEntityTest {

    @Test
    fun testReporteEntityCreation() {
        val reporteEntity = ReporteEntity(
            id = "reporte-123",
            usuario_id = "user-456",
            ciudad_id = "ciudad-789",
            ubicacion_id = "ubicacion-001",
            descripcion = "Bache en la calle principal",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-15",
            categoria = ReportType.BACHE,
            created_at = "2024-01-15T10:00:00Z",
            updated_at = "2024-01-15T10:00:00Z",
            barrio_id = "barrio-123",
            barrio_nombre = "Centro",
            direccion_aproximada = "Calle 10 y Av. Principal",
            image_url = "https://example.com/image.jpg"
        )

        assertEquals("reporte-123", reporteEntity.id)
        assertEquals("user-456", reporteEntity.usuario_id)
        assertEquals("ciudad-789", reporteEntity.ciudad_id)
        assertEquals("ubicacion-001", reporteEntity.ubicacion_id)
        assertEquals("Bache en la calle principal", reporteEntity.descripcion)
        assertEquals(ReporteEstado.PENDIENTE, reporteEntity.estado)
        assertEquals("2024-01-15", reporteEntity.fecha_reporte)
        assertEquals(ReportType.BACHE, reporteEntity.categoria)
        assertEquals("2024-01-15T10:00:00Z", reporteEntity.created_at)
        assertEquals("2024-01-15T10:00:00Z", reporteEntity.updated_at)
        assertEquals("barrio-123", reporteEntity.barrio_id)
        assertEquals("Centro", reporteEntity.barrio_nombre)
        assertEquals("Calle 10 y Av. Principal", reporteEntity.direccion_aproximada)
        assertEquals("https://example.com/image.jpg", reporteEntity.image_url)
    }

    @Test
    fun testReporteEntityWithOptionalFieldsNull() {
        val reporteEntity = ReporteEntity(
            id = "reporte-456",
            usuario_id = "user-789",
            ciudad_id = "ciudad-123",
            ubicacion_id = "ubicacion-002",
            descripcion = "Alumbrado público dañado",
            estado = ReporteEstado.RESUELTO,
            fecha_reporte = "2024-02-20",
            categoria = ReportType.LUZ,
            created_at = null,
            updated_at = null,
            barrio_id = "barrio-456"
        )

        assertEquals("reporte-456", reporteEntity.id)
        assertEquals(ReporteEstado.RESUELTO, reporteEntity.estado)
        assertEquals(ReportType.LUZ, reporteEntity.categoria)
        assertNull(reporteEntity.created_at)
        assertNull(reporteEntity.updated_at)
        assertNull(reporteEntity.barrio_nombre)
        assertNull(reporteEntity.direccion_aproximada)
        assertNull(reporteEntity.image_url)
    }

    @Test
    fun testReporteEntityWithDifferentStates() {
        val pendiente = ReporteEntity(
            id = "reporte-1",
            usuario_id = "user-1",
            ciudad_id = "ciudad-1",
            ubicacion_id = "ubicacion-1",
            descripcion = "Reporte pendiente",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-03-01",
            categoria = ReportType.ZONA_DE_RIESGO,
            created_at = null,
            updated_at = null,
            barrio_id = "barrio-1"
        )

        val enProceso = ReporteEntity(
            id = "reporte-2",
            usuario_id = "user-2",
            ciudad_id = "ciudad-2",
            ubicacion_id = "ubicacion-2",
            descripcion = "Reporte en proceso",
            estado = ReporteEstado.EN_PROCESO,
            fecha_reporte = "2024-03-02",
            categoria = ReportType.AGUA,
            created_at = null,
            updated_at = null,
            barrio_id = "barrio-2"
        )

        val resuelto = ReporteEntity(
            id = "reporte-3",
            usuario_id = "user-3",
            ciudad_id = "ciudad-3",
            ubicacion_id = "ubicacion-3",
            descripcion = "Reporte resuelto",
            estado = ReporteEstado.RESUELTO,
            fecha_reporte = "2024-03-03",
            categoria = ReportType.ZONA_DE_RIESGO,
            created_at = null,
            updated_at = null,
            barrio_id = "barrio-3"
        )

        assertEquals(ReporteEstado.PENDIENTE, pendiente.estado)
        assertEquals(ReporteEstado.EN_PROCESO, enProceso.estado)
        assertEquals(ReporteEstado.RESUELTO, resuelto.estado)
    }

    @Test
    fun testReporteEntityWithDifferentCategories() {
        val bache = ReporteEntity(
            id = "reporte-1",
            usuario_id = "user-1",
            ciudad_id = "ciudad-1",
            ubicacion_id = "ubicacion-1",
            descripcion = "Bache",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-04-01",
            categoria = ReportType.BACHE,
            created_at = null,
            updated_at = null,
            barrio_id = "barrio-1"
        )

        val basura = ReporteEntity(
            id = "reporte-2",
            usuario_id = "user-2",
            ciudad_id = "ciudad-2",
            ubicacion_id = "ubicacion-2",
            descripcion = "Basura",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-04-02",
            categoria = ReportType.AGUA,
            created_at = null,
            updated_at = null,
            barrio_id = "barrio-2"
        )

        val alumbrado = ReporteEntity(
            id = "reporte-3",
            usuario_id = "user-3",
            ciudad_id = "ciudad-3",
            ubicacion_id = "ubicacion-3",
            descripcion = "Alumbrado",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-04-03",
            categoria = ReportType.LUZ,
            created_at = null,
            updated_at = null,
            barrio_id = "barrio-3"
        )

        assertEquals(ReportType.BACHE, bache.categoria)
        assertEquals(ReportType.AGUA, basura.categoria)
        assertEquals(ReportType.LUZ, alumbrado.categoria)
    }

    @Test
    fun testReporteEntityWithEmptyOptionalFields() {
        val reporteEntity = ReporteEntity(
            id = "reporte-999",
            usuario_id = "user-999",
            ciudad_id = "ciudad-999",
            ubicacion_id = "ubicacion-999",
            descripcion = "Test reporte",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-05-01",
            categoria = ReportType.ZONA_DE_RIESGO,
            created_at = null,
            updated_at = null,
            barrio_id = "barrio-999",
            barrio_nombre = "",
            direccion_aproximada = "",
            image_url = ""
        )

        assertEquals("", reporteEntity.barrio_nombre)
        assertEquals("", reporteEntity.direccion_aproximada)
        assertEquals("", reporteEntity.image_url)
    }
}
