package ec.cityalerta.app

import ec.cityalerta.app.model.data.barrio.BarrioCreateDto
import ec.cityalerta.app.model.data.barrio.BarrioUpdateDto
import ec.cityalerta.app.model.data.ciudad.CiudadCreateDto
import ec.cityalerta.app.model.data.ciudad.CiudadUpdateDto
import ec.cityalerta.app.model.data.perfil.PerfilCreateDto
import ec.cityalerta.app.model.data.geoJson.Geometry
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DtoCoverageTest {

    @Test
    fun testBarrioCreateDto() {
        val geometry = Geometry(
            type = "Polygon",
            coordinates = listOf(listOf(listOf(0.0, 0.0)))
        )
        val dto = BarrioCreateDto(
            ciudadId = "ciudad-1",
            nombre = "Test Barrio",
            perimetro = geometry
        )
        assertEquals("ciudad-1", dto.ciudadId)
        assertEquals("Test Barrio", dto.nombre)
        assertEquals(geometry, dto.perimetro)
    }

    @Test
    fun testBarrioUpdateDto() {
        val geometry = Geometry(
            type = "Polygon",
            coordinates = listOf(listOf(listOf(0.0, 0.0)))
        )
        val dto = BarrioUpdateDto(
            ciudadId = "ciudad-1",
            nombre = "Updated Barrio",
            perimetro = geometry
        )
        assertEquals("ciudad-1", dto.ciudadId)
        assertEquals("Updated Barrio", dto.nombre)
        assertEquals(geometry, dto.perimetro)
    }

    @Test
    fun testBarrioUpdateDtoWithNulls() {
        val dto = BarrioUpdateDto()
        assertNull(dto.ciudadId)
        assertNull(dto.nombre)
        assertNull(dto.perimetro)
    }

    @Test
    fun testCiudadCreateDto() {
        val geometry = Geometry(
            type = "Polygon",
            coordinates = listOf(listOf(listOf(0.0, 0.0)))
        )
        val dto = CiudadCreateDto(
            nombre = "Quito",
            pais = "Ecuador",
            geojson = geometry,
            centroLat = -0.1807,
            centroLng = -78.4678
        )
        assertEquals("Quito", dto.nombre)
        assertEquals("Ecuador", dto.pais)
        assertEquals(geometry, dto.geojson)
        assertEquals(-0.1807, dto.centroLat)
        assertEquals(-78.4678, dto.centroLng)
    }

    @Test
    fun testCiudadUpdateDto() {
        val geometry = Geometry(
            type = "Polygon",
            coordinates = listOf(listOf(listOf(0.0, 0.0)))
        )
        val dto = CiudadUpdateDto(
            nombre = "Updated Quito",
            pais = "Ecuador",
            geojson = geometry,
            centroLat = -0.1807,
            centroLng = -78.4678
        )
        assertEquals("Updated Quito", dto.nombre)
        assertEquals("Ecuador", dto.pais)
        assertEquals(geometry, dto.geojson)
        assertEquals(-0.1807, dto.centroLat)
        assertEquals(-78.4678, dto.centroLng)
    }

    @Test
    fun testCiudadUpdateDtoWithNulls() {
        val dto = CiudadUpdateDto()
        assertNull(dto.nombre)
        assertNull(dto.pais)
        assertNull(dto.geojson)
        assertNull(dto.centroLat)
        assertNull(dto.centroLng)
    }

    @Test
    fun testPerfilCreateDto() {
        val dto = PerfilCreateDto(
            nombreCompleto = "John Doe",
            rolSlug = "ciudadano",
            activo = true,
            ciudadId = "ciudad-1"
        )
        assertEquals("John Doe", dto.nombreCompleto)
        assertEquals("ciudadano", dto.rolSlug)
        assertEquals(true, dto.activo)
        assertEquals("ciudad-1", dto.ciudadId)
    }
}
