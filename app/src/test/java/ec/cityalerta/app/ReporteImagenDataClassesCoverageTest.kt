package ec.cityalerta.app

import ec.cityalerta.app.model.data.reporteimagen.ReporteImagen
import ec.cityalerta.app.model.data.reporteimagen.ReporteImagenCreateDto
import ec.cityalerta.app.model.data.reporteimagen.ReporteImagenUpdateDto
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReporteImagenDataClassesCoverageTest {

    @Test
    fun testReporteImagen() {
        val imagen = ReporteImagen(
            id = "imagen-1",
            reporte_id = "reporte-1",
            storage_uuid = "uuid-123",
            url_path = "https://example.com/image.jpg",
            created_at = "2024-01-01",
            updated_at = "2024-01-02"
        )
        assertEquals("imagen-1", imagen.id)
        assertEquals("reporte-1", imagen.reporte_id)
        assertEquals("uuid-123", imagen.storage_uuid)
        assertEquals("https://example.com/image.jpg", imagen.url_path)
        assertEquals("2024-01-01", imagen.created_at)
        assertEquals("2024-01-02", imagen.updated_at)
    }

    @Test
    fun testReporteImagenWithoutDates() {
        val imagen = ReporteImagen(
            id = "imagen-1",
            reporte_id = "reporte-1",
            storage_uuid = "uuid-123",
            url_path = "https://example.com/image.jpg"
        )
        assertNull(imagen.created_at)
        assertNull(imagen.updated_at)
    }

    @Test
    fun testReporteImagenCreateDto() {
        val dto = ReporteImagenCreateDto(
            reporte_id = "reporte-1",
            storage_uuid = "uuid-123",
            url_path = "https://example.com/image.jpg"
        )
        assertEquals("reporte-1", dto.reporte_id)
        assertEquals("uuid-123", dto.storage_uuid)
        assertEquals("https://example.com/image.jpg", dto.url_path)
    }

    @Test
    fun testReporteImagenUpdateDto() {
        val dto = ReporteImagenUpdateDto(
            reporte_id = "reporte-1",
            storage_uuid = "uuid-123",
            url_path = "https://example.com/image.jpg"
        )
        assertEquals("reporte-1", dto.reporte_id)
        assertEquals("uuid-123", dto.storage_uuid)
        assertEquals("https://example.com/image.jpg", dto.url_path)
    }

    @Test
    fun testReporteImagenUpdateDtoWithNulls() {
        val dto = ReporteImagenUpdateDto()
        assertNull(dto.reporte_id)
        assertNull(dto.storage_uuid)
        assertNull(dto.url_path)
    }
}
