package ec.cityalerta.app

import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacion
import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacionCreateDto
import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacionUpdateDto
import ec.cityalerta.app.model.repository.ReporteUbicacionRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests unitarios para ReporteUbicacionRepository.
 * Valida la creacion y manipulacion de datos de ubicaciones de reportes.
 */
class ReporteUbicacionRepositoryTest {

    private lateinit var repository: ReporteUbicacionRepository

    @Before
    fun setUp() {
        repository = ReporteUbicacionRepository()
    }

    @Test
    fun testReporteUbicacionCreateDtoCreation() {
        val createDto = ReporteUbicacionCreateDto(
            lat = -0.9542,
            lng = -80.7314,
            direccion_aproximada = "Avenida principal, Manta"
        )

        assertNotNull(createDto)
        assertEquals(-0.9542, createDto.lat)
        assertEquals(-80.7314, createDto.lng)
        assertEquals("Avenida principal, Manta", createDto.direccion_aproximada)
    }

    @Test
    fun testReporteUbicacionUpdateDtoCreation() {
        val updateDto = ReporteUbicacionUpdateDto(
            lat = -1.0,
            lng = -79.0,
            direccion_aproximada = "Nueva direccion, Quito"
        )

        assertNotNull(updateDto)
        assertEquals(-1.0, updateDto.lat)
        assertEquals(-79.0, updateDto.lng)
        assertEquals("Nueva direccion, Quito", updateDto.direccion_aproximada)
    }

    @Test
    fun testReporteUbicacionUpdateDtoConCamposOpcionales() {
        val updateDto = ReporteUbicacionUpdateDto(
            lat = null,
            lng = null,
            direccion_aproximada = "Solo actualizar direccion"
        )

        assertNotNull(updateDto)
        assertNull(updateDto.lat)
        assertNull(updateDto.lng)
        assertEquals("Solo actualizar direccion", updateDto.direccion_aproximada)
    }

    @Test
    fun testReporteUbicacionDataClass() {
        val ubicacion = ReporteUbicacion(
            id = "ubicacion-1",
            lat = -0.9542,
            lng = -80.7314,
            direccion_aproximada = "Manta Ecuador",
            created_at = "2024-01-01T10:00:00Z",
            updated_at = "2024-01-05T15:30:00Z"
        )

        assertNotNull(ubicacion)
        assertEquals("ubicacion-1", ubicacion.id)
        assertEquals(-0.9542, ubicacion.lat)
        assertEquals(-80.7314, ubicacion.lng)
        assertEquals("Manta Ecuador", ubicacion.direccion_aproximada)
        assertEquals("2024-01-01T10:00:00Z", ubicacion.created_at)
        assertEquals("2024-01-05T15:30:00Z", ubicacion.updated_at)
    }

    @Test
    fun testReporteUbicacionSinTimestampsOpcionales() {
        val ubicacion = ReporteUbicacion(
            id = "ubicacion-2",
            lat = -0.22,
            lng = -78.51,
            direccion_aproximada = "Quito"
        )

        assertNull(ubicacion.created_at)
        assertNull(ubicacion.updated_at)
    }

    @Test
    fun testReporteUbicacionEquality() {
        val ubicacion1 = ReporteUbicacion(
            id = "ubicacion-1",
            lat = -0.9542,
            lng = -80.7314,
            direccion_aproximada = "Manta"
        )
        val ubicacion2 = ubicacion1.copy()

        assertEquals(ubicacion1, ubicacion2)
        assertEquals(ubicacion1.lat, ubicacion2.lat)
        assertEquals(ubicacion1.lng, ubicacion2.lng)
    }

    @Test
    fun testReporteUbicacionCopyWithModifications() {
        val original = ReporteUbicacion(
            id = "ubicacion-1",
            lat = -0.9542,
            lng = -80.7314,
            direccion_aproximada = "Direccion original"
        )

        val modificado = original.copy(
            lat = -1.0,
            lng = -79.0,
            direccion_aproximada = "Direccion actualizada"
        )

        assertEquals("ubicacion-1", modificado.id)
        assertEquals(-1.0, modificado.lat)
        assertEquals(-79.0, modificado.lng)
        assertEquals("Direccion actualizada", modificado.direccion_aproximada)
        assertEquals(-0.9542, original.lat)
        assertEquals("Direccion original", original.direccion_aproximada)
    }

    @Test
    fun testCoordenadasCiudadesEcuador() {
        val ubicaciones = listOf(
            ReporteUbicacionCreateDto(-0.9542, -80.7314, "Manta"),
            ReporteUbicacionCreateDto(-0.22, -78.51, "Quito"),
            ReporteUbicacionCreateDto(-2.19, -79.88, "Guayaquil")
        )

        assertEquals(3, ubicaciones.size)
        assertTrue(ubicaciones.any { it.direccion_aproximada == "Manta" })
        assertTrue(ubicaciones.any { it.direccion_aproximada == "Quito" })
        assertTrue(ubicaciones.all { it.lat in -90.0..90.0 })
        assertTrue(ubicaciones.all { it.lng in -180.0..180.0 })
    }

    @Test
    fun testReporteUbicacionConDireccionVacia() {
        val createDto = ReporteUbicacionCreateDto(
            lat = 0.0,
            lng = 0.0,
            direccion_aproximada = ""
        )

        assertEquals("", createDto.direccion_aproximada)
    }

    @Test
    fun testReporteUbicacionRepositoryInitialization() {
        assertNotNull(repository)
        assertTrue(repository is ReporteUbicacionRepository)
    }
}
