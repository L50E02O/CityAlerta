package ec.cityalerta.app

import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacion
import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacionCreateDto
import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacionUpdateDto
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReporteUbicacionDataClassesCoverageTest {

    @Test
    fun testReporteUbicacion() {
        val ubicacion = ReporteUbicacion(
            id = "ubicacion-1",
            lat = -0.1807,
            lng = -78.4678,
            direccion_aproximada = "Calle 123",
            created_at = "2024-01-01",
            updated_at = "2024-01-02"
        )
        assertEquals("ubicacion-1", ubicacion.id)
        assertEquals(-0.1807, ubicacion.lat)
        assertEquals(-78.4678, ubicacion.lng)
        assertEquals("Calle 123", ubicacion.direccion_aproximada)
        assertEquals("2024-01-01", ubicacion.created_at)
        assertEquals("2024-01-02", ubicacion.updated_at)
    }

    @Test
    fun testReporteUbicacionWithoutDates() {
        val ubicacion = ReporteUbicacion(
            id = "ubicacion-1",
            lat = -0.1807,
            lng = -78.4678,
            direccion_aproximada = "Calle 123"
        )
        assertNull(ubicacion.created_at)
        assertNull(ubicacion.updated_at)
    }

    @Test
    fun testReporteUbicacionCreateDto() {
        val dto = ReporteUbicacionCreateDto(
            lat = -0.1807,
            lng = -78.4678,
            direccion_aproximada = "Calle 123"
        )
        assertEquals(-0.1807, dto.lat)
        assertEquals(-78.4678, dto.lng)
        assertEquals("Calle 123", dto.direccion_aproximada)
    }

    @Test
    fun testReporteUbicacionUpdateDto() {
        val dto = ReporteUbicacionUpdateDto(
            lat = -0.1807,
            lng = -78.4678,
            direccion_aproximada = "Calle 123"
        )
        assertEquals(-0.1807, dto.lat)
        assertEquals(-78.4678, dto.lng)
        assertEquals("Calle 123", dto.direccion_aproximada)
    }

    @Test
    fun testReporteUbicacionUpdateDtoWithNulls() {
        val dto = ReporteUbicacionUpdateDto()
        assertNull(dto.lat)
        assertNull(dto.lng)
        assertNull(dto.direccion_aproximada)
    }
}
