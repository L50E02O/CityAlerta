package ec.cityalerta.app

import ec.cityalerta.app.model.data.barrio.Barrio
import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.data.perfil.Perfil
import ec.cityalerta.app.model.data.geoJson.Geometry
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EntityDataClassesCoverageTest {

    @Test
    fun testBarrio() {
        val geometry = Geometry(
            type = "Polygon",
            coordinates = listOf(listOf(listOf(0.0, 0.0)))
        )
        val barrio = Barrio(
            id = "barrio-1",
            ciudadId = "ciudad-1",
            nombre = "Test Barrio",
            perimetro = geometry,
            createdAt = "2024-01-01",
            updatedAt = "2024-01-02"
        )
        assertEquals("barrio-1", barrio.id)
        assertEquals("ciudad-1", barrio.ciudadId)
        assertEquals("Test Barrio", barrio.nombre)
        assertEquals(geometry, barrio.perimetro)
        assertEquals("2024-01-01", barrio.createdAt)
        assertEquals("2024-01-02", barrio.updatedAt)
    }

    @Test
    fun testBarrioWithoutDates() {
        val geometry = Geometry(
            type = "Polygon",
            coordinates = listOf(listOf(listOf(0.0, 0.0)))
        )
        val barrio = Barrio(
            id = "barrio-1",
            ciudadId = "ciudad-1",
            nombre = "Test Barrio",
            perimetro = geometry
        )
        assertEquals("barrio-1", barrio.id)
        assertNull(barrio.createdAt)
        assertNull(barrio.updatedAt)
    }

    @Test
    fun testCiudad() {
        val geometry = Geometry(
            type = "Polygon",
            coordinates = listOf(listOf(listOf(0.0, 0.0)))
        )
        val ciudad = Ciudad(
            id = "ciudad-1",
            nombre = "Quito",
            pais = "Ecuador",
            geojson = geometry,
            centroLat = -0.1807,
            centroLng = -78.4678,
            createdAt = "2024-01-01",
            updatedAt = "2024-01-02"
        )
        assertEquals("ciudad-1", ciudad.id)
        assertEquals("Quito", ciudad.nombre)
        assertEquals("Ecuador", ciudad.pais)
        assertEquals(geometry, ciudad.geojson)
        assertEquals(-0.1807, ciudad.centroLat)
        assertEquals(-78.4678, ciudad.centroLng)
        assertEquals("2024-01-01", ciudad.createdAt)
        assertEquals("2024-01-02", ciudad.updatedAt)
    }

    @Test
    fun testCiudadWithoutDates() {
        val geometry = Geometry(
            type = "Polygon",
            coordinates = listOf(listOf(listOf(0.0, 0.0)))
        )
        val ciudad = Ciudad(
            id = "ciudad-1",
            nombre = "Quito",
            pais = "Ecuador",
            geojson = geometry,
            centroLat = -0.1807,
            centroLng = -78.4678
        )
        assertEquals("ciudad-1", ciudad.id)
        assertNull(ciudad.createdAt)
        assertNull(ciudad.updatedAt)
    }

    @Test
    fun testPerfil() {
        val perfil = Perfil(
            id = "perfil-1",
            nombreCompleto = "John Doe",
            rolSlug = "ciudadano",
            activo = true,
            createdAt = "2024-01-01",
            updatedAt = "2024-01-02",
            ciudadId = "ciudad-1"
        )
        assertEquals("perfil-1", perfil.id)
        assertEquals("John Doe", perfil.nombreCompleto)
        assertEquals("ciudadano", perfil.rolSlug)
        assertEquals(true, perfil.activo)
        assertEquals("2024-01-01", perfil.createdAt)
        assertEquals("2024-01-02", perfil.updatedAt)
        assertEquals("ciudad-1", perfil.ciudadId)
    }

    @Test
    fun testPerfilWithoutDates() {
        val perfil = Perfil(
            id = "perfil-1",
            nombreCompleto = "John Doe",
            rolSlug = "ciudadano",
            activo = true,
            ciudadId = "ciudad-1"
        )
        assertEquals("perfil-1", perfil.id)
        assertNull(perfil.createdAt)
        assertNull(perfil.updatedAt)
    }
}
