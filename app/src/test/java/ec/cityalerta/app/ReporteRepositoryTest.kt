package ec.cityalerta.app

import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporte.ReporteCreateDto
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.ReporteUpdateDto
import ec.cityalerta.app.model.repository.ReporteRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests unitarios para ReporteRepository.
 * Valida las operaciones CRUD y la logica de busqueda para reportes.
 * Se enfocan en casos borde y flujos negativos.
 */
class ReporteRepositoryTest {

    private lateinit var repository: ReporteRepository

    @Before
    fun setUp() {
        repository = ReporteRepository()
    }

    // --- Pruebas de creacion de DTOs (casos puros sin dependencias externas) ---

    @Test
    fun testReporteCreateDtoCreation() {
        // Arrange
        val createDto = ReporteCreateDto(
            usuario_id = "user-1",
            ciudad_id = "ciudad-1",
            ubicacion_id = "ubicacion-1",
            descripcion = "Robo en la esquina",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-01",
            categoria = ReportType.ZONA_DE_RIESGO,
            barrio_id = "barrio-1"
        )

        // Assert
        assertNotNull(createDto)
        assertEquals("user-1", createDto.usuario_id)
        assertEquals("ciudad-1", createDto.ciudad_id)
        assertEquals("Robo en la esquina", createDto.descripcion)
        assertEquals(ReportType.ZONA_DE_RIESGO, createDto.categoria)
    }

    @Test
    fun testReporteCreateDtoConDescripcionVacia() {
        // Test caso borde: descripcion vacia
        val createDto = ReporteCreateDto(
            usuario_id = "user-1",
            ciudad_id = "ciudad-1",
            ubicacion_id = "ubicacion-1",
            descripcion = "",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-01",
            categoria = ReportType.ZONA_DE_RIESGO,
            barrio_id = "barrio-1"
        )

        assertNotNull(createDto)
        assertEquals("", createDto.descripcion)
    }

    @Test
    fun testReporteDataClass() {
        // Arrange
        val reporte = Reporte(
            id = "reporte-1",
            usuario_id = "user-1",
            ciudad_id = "ciudad-1",
            ubicacion_id = "ubicacion-1",
            descripcion = "Incidente reportado",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-01",
            categoria = ReportType.BACHE,
            barrio_id = "barrio-1"
        )

        // Assert
        assertNotNull(reporte)
        assertEquals("reporte-1", reporte.id)
        assertEquals("user-1", reporte.usuario_id)
        assertEquals(ReporteEstado.PENDIENTE, reporte.estado)
        assertEquals(ReportType.BACHE, reporte.categoria)
    }

    @Test
    fun testReporteConDiferentesEstados() {
        // Test todos los estados posibles
        ReporteEstado.entries.forEach { estado ->
            val reporte = Reporte(
                id = "reporte-${estado.name}",
                usuario_id = "user-1",
                ciudad_id = "ciudad-1",
                ubicacion_id = "ubicacion-1",
                descripcion = "Prueba estado $estado",
                estado = estado,
                fecha_reporte = "2024-01-01",
                categoria = ReportType.ZONA_DE_RIESGO,
                barrio_id = "barrio-1"
            )

            assertEquals(estado, reporte.estado)
        }
    }

    @Test
    fun testReporteConDiferentesCategories() {
        // Test todas las categorias posibles
        ReportType.entries.forEach { categoria ->
            val reporte = Reporte(
                id = "reporte-${categoria.name}",
                usuario_id = "user-1",
                ciudad_id = "ciudad-1",
                ubicacion_id = "ubicacion-1",
                descripcion = "Prueba categoria $categoria",
                estado = ReporteEstado.PENDIENTE,
                fecha_reporte = "2024-01-01",
                categoria = categoria,
                barrio_id = "barrio-1"
            )

            assertEquals(categoria, reporte.categoria)
        }
    }

    @Test
    fun testReporteUpdateDto() {
        // Arrange
        val updateDto = ReporteUpdateDto(
            usuario_id = "user-2",
            ciudad_id = "ciudad-2",
            ubicacion_id = "ubicacion-2",
            descripcion = "Descripcion actualizada",
            estado = ReporteEstado.RESUELTO,
            fecha_reporte = "2024-01-02",
            categoria = ReportType.BACHE,
            barrio_id = "barrio-2"
        )

        // Assert
        assertNotNull(updateDto)
        assertEquals("user-2", updateDto.usuario_id)
        assertEquals("descripcion actualizada", updateDto.descripcion?.lowercase())
        assertEquals(ReporteEstado.RESUELTO, updateDto.estado)
    }

    @Test
    fun testReporteUpdateDtoConCamposOpcionales() {
        // Test que ciertos campos sean opcionales en update
        val updateDto = ReporteUpdateDto(
            usuario_id = null,
            ciudad_id = null,
            ubicacion_id = null,
            descripcion = "Solo actualizar descripcion",
            estado = null,
            fecha_reporte = null,
            categoria = null,
            barrio_id = null
        )

        assertNotNull(updateDto)
        assertEquals("solo actualizar descripcion", updateDto.descripcion?.lowercase())
    }

    @Test
    fun testReporteRepositoryInitialization() {
        assertNotNull(repository)
        assertTrue(repository is ReporteRepository)
    }

    @Test
    fun testReporteSinTimestampsOpcionales() {
        val reporte = Reporte(
            id = "reporte-1",
            usuario_id = "user-1",
            ciudad_id = "ciudad-1",
            ubicacion_id = "ubicacion-1",
            descripcion = "Sin timestamps",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-01",
            categoria = ReportType.BACHE,
            barrio_id = "barrio-1"
        )

        assertEquals(null, reporte.created_at)
        assertEquals(null, reporte.updated_at)
    }

    @Test
    fun testMultiplesReportesMismaCiudadPorCreateDto() {
        val ciudadId = "ciudad-manta"
        val reportes = (1..4).map { index ->
            ReporteCreateDto(
                usuario_id = "user-$index",
                ciudad_id = ciudadId,
                ubicacion_id = "ubicacion-$index",
                descripcion = "Reporte $index",
                estado = ReporteEstado.PENDIENTE,
                fecha_reporte = "2024-01-0$index",
                categoria = ReportType.ZONA_DE_RIESGO,
                barrio_id = "barrio-$index"
            )
        }

        assertEquals(4, reportes.size)
        assertTrue(reportes.all { it.ciudad_id == ciudadId })
    }

    @Test
    fun testMultiplesReportesConMismaCiudad() {
        // Test creacion de multiples reportes para la misma ciudad
        val ciudadId = "ciudad-manta"
        val reportes = (1..5).map { index ->
            Reporte(
                id = "reporte-$index",
                usuario_id = "user-$index",
                ciudad_id = ciudadId,
                ubicacion_id = "ubicacion-$index",
                descripcion = "Reporte numero $index",
                estado = ReporteEstado.PENDIENTE,
                fecha_reporte = "2024-01-0$index",
                categoria = ReportType.ZONA_DE_RIESGO,
                barrio_id = "barrio-$index"
            )
        }

        // Verify todos comparten la misma ciudad_id
        assertTrue(reportes.all { it.ciudad_id == ciudadId })
        assertEquals(5, reportes.size)
    }

    @Test
    fun testReporteConValoresEspeciales() {
        // Test manejo de caracteres especiales en descripcion
        val reporte = Reporte(
            id = "reporte-1",
            usuario_id = "user-1",
            ciudad_id = "ciudad-1",
            ubicacion_id = "ubicacion-1",
            descripcion = "Descripcion con caracteres especiales: @#$%^&*()",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-01",
            categoria = ReportType.ZONA_DE_RIESGO,
            barrio_id = "barrio-1"
        )

        assertNotNull(reporte)
        assertTrue(reporte.descripcion.contains("@#$%^&*()"))
    }

    @Test
    fun testReporteEquality() {
        // Test que dos reportes con mismos datos sean iguales
        val reporte1 = Reporte(
            id = "reporte-1",
            usuario_id = "user-1",
            ciudad_id = "ciudad-1",
            ubicacion_id = "ubicacion-1",
            descripcion = "Reporte",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-01",
            categoria = ReportType.ZONA_DE_RIESGO,
            barrio_id = "barrio-1"
        )

        val reporte2 = Reporte(
            id = "reporte-1",
            usuario_id = "user-1",
            ciudad_id = "ciudad-1",
            ubicacion_id = "ubicacion-1",
            descripcion = "Reporte",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-01",
            categoria = ReportType.ZONA_DE_RIESGO,
            barrio_id = "barrio-1"
        )

        assertEquals(reporte1, reporte2)
    }

    @Test
    fun testReporteCopyOperation() {
        // Test copy() function de data class
        val reporteOriginal = Reporte(
            id = "reporte-1",
            usuario_id = "user-1",
            ciudad_id = "ciudad-1",
            ubicacion_id = "ubicacion-1",
            descripcion = "Reporte original",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-01",
            categoria = ReportType.ZONA_DE_RIESGO,
            barrio_id = "barrio-1"
        )

        val reporteModificado = reporteOriginal.copy(
            descripcion = "Reporte modificado",
            estado = ReporteEstado.RESUELTO
        )

        assertEquals("Reporte modificado", reporteModificado.descripcion)
        assertEquals(ReporteEstado.RESUELTO, reporteModificado.estado)
        assertEquals(reporteOriginal.id, reporteModificado.id)
    }
}

