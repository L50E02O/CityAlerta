package ec.cityalerta.app

import ec.cityalerta.app.model.data.contracts.crud.CrudRepositoryContract
import ec.cityalerta.app.model.data.reporteimagen.ReporteImagen
import ec.cityalerta.app.model.data.reporteimagen.ReporteImagenCreateDto
import ec.cityalerta.app.model.data.reporteimagen.ReporteImagenUpdateDto
import ec.cityalerta.app.model.repository.ReporteImagenRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests unitarios para ReporteImagenRepository.
 * Valida la creacion y manipulacion de datos de imagenes de reportes.
 */
class ReporteImagenRepositoryTest {

    private lateinit var repository: ReporteImagenRepository

    @Before
    fun setUp() {
        repository = ReporteImagenRepository()
    }

    @Test
    fun testReporteImagenCreateDtoCreation() {
        val createDto = ReporteImagenCreateDto(
            reporte_id = "reporte-1",
            storage_uuid = "uuid-storage-123",
            url_path = "/storage/reportes/imagen1.jpg"
        )

        assertNotNull(createDto)
        assertEquals("reporte-1", createDto.reporte_id)
        assertEquals("uuid-storage-123", createDto.storage_uuid)
        assertEquals("/storage/reportes/imagen1.jpg", createDto.url_path)
    }

    @Test
    fun testReporteImagenUpdateDtoCreation() {
        val updateDto = ReporteImagenUpdateDto(
            reporte_id = "reporte-2",
            storage_uuid = "uuid-nuevo",
            url_path = "/storage/reportes/imagen2.png"
        )

        assertNotNull(updateDto)
        assertEquals("reporte-2", updateDto.reporte_id)
        assertEquals("uuid-nuevo", updateDto.storage_uuid)
        assertEquals("/storage/reportes/imagen2.png", updateDto.url_path)
    }

    @Test
    fun testReporteImagenUpdateDtoConCamposOpcionales() {
        val updateDto = ReporteImagenUpdateDto(
            reporte_id = null,
            storage_uuid = "solo-uuid",
            url_path = null
        )

        assertNotNull(updateDto)
        assertNull(updateDto.reporte_id)
        assertEquals("solo-uuid", updateDto.storage_uuid)
        assertNull(updateDto.url_path)
    }

    @Test
    fun testReporteImagenDataClass() {
        val imagen = ReporteImagen(
            id = "imagen-1",
            reporte_id = "reporte-1",
            storage_uuid = "uuid-abc",
            url_path = "/path/imagen.jpg",
            created_at = "2024-01-01T10:00:00Z",
            updated_at = "2024-01-05T15:30:00Z"
        )

        assertNotNull(imagen)
        assertEquals("imagen-1", imagen.id)
        assertEquals("reporte-1", imagen.reporte_id)
        assertEquals("uuid-abc", imagen.storage_uuid)
        assertEquals("/path/imagen.jpg", imagen.url_path)
        assertEquals("2024-01-01T10:00:00Z", imagen.created_at)
        assertEquals("2024-01-05T15:30:00Z", imagen.updated_at)
    }

    @Test
    fun testReporteImagenSinTimestampsOpcionales() {
        val imagen = ReporteImagen(
            id = "imagen-2",
            reporte_id = "reporte-1",
            storage_uuid = "uuid-def",
            url_path = "/path/foto.png"
        )

        assertNull(imagen.created_at)
        assertNull(imagen.updated_at)
    }

    @Test
    fun testMultiplesImagenesMismoReporte() {
        val reporteId = "reporte-123"
        val imagenes = (1..3).map { index ->
            ReporteImagenCreateDto(
                reporte_id = reporteId,
                storage_uuid = "uuid-$index",
                url_path = "/storage/reporte-$index.jpg"
            )
        }

        assertEquals(3, imagenes.size)
        assertTrue(imagenes.all { it.reporte_id == reporteId })
        assertEquals(3, imagenes.map { it.storage_uuid }.distinct().size)
    }

    @Test
    fun testReporteImagenEquality() {
        val imagen1 = ReporteImagen(
            id = "imagen-1",
            reporte_id = "reporte-1",
            storage_uuid = "uuid-1",
            url_path = "/path/a.jpg"
        )
        val imagen2 = imagen1.copy()

        assertEquals(imagen1, imagen2)
        assertEquals(imagen1.id, imagen2.id)
        assertEquals(imagen1.url_path, imagen2.url_path)
    }

    @Test
    fun testReporteImagenCopyWithModifications() {
        val original = ReporteImagen(
            id = "imagen-1",
            reporte_id = "reporte-1",
            storage_uuid = "uuid-original",
            url_path = "/path/original.jpg"
        )

        val modificado = original.copy(
            url_path = "/path/actualizado.jpg",
            storage_uuid = "uuid-nuevo"
        )

        assertEquals("imagen-1", modificado.id)
        assertEquals("/path/actualizado.jpg", modificado.url_path)
        assertEquals("uuid-nuevo", modificado.storage_uuid)
        assertEquals("/path/original.jpg", original.url_path)
    }

    @Test
    fun testReporteImagenConExtensionesDiferentes() {
        val extensiones = listOf(".jpg", ".png", ".webp")
        val imagenes = extensiones.map { ext ->
            ReporteImagenCreateDto(
                reporte_id = "reporte-1",
                storage_uuid = "uuid$ext",
                url_path = "/storage/imagen$ext"
            )
        }

        assertEquals(3, imagenes.size)
        extensiones.forEachIndexed { index, ext ->
            assertTrue(imagenes[index].url_path.endsWith(ext))
        }
    }

    @Test
    fun testReporteImagenRepositoryInitialization() {
        assertNotNull(repository)
        assertTrue(repository is ReporteImagenRepository)
    }

    @Test
    fun testReporteImagenRepositoryImplementaContratoCrud() {
        assertTrue(repository is CrudRepositoryContract<*, *, *>)
    }
}
