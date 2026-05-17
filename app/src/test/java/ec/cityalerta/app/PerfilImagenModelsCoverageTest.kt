package ec.cityalerta.app

import ec.cityalerta.app.model.data.perfil.PerfilResumen
import ec.cityalerta.app.model.data.perfilimagen.PerfilImagen
import ec.cityalerta.app.model.data.perfilimagen.PerfilImagenCreateDto
import ec.cityalerta.app.model.data.perfilimagen.PerfilImagenUpdateDto
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class PerfilImagenModelsCoverageTest {

    @Test
    fun perfilImagen_holdsAllFields() {
        val imagen = PerfilImagen(
            id = "img-1",
            perfil_id = "perfil-1",
            storage_uuid = "storage-uuid",
            url_path = "perfil_imagen/storage-uuid",
            created_at = "2026-05-17T10:00:00Z",
            updated_at = "2026-05-17T11:00:00Z"
        )

        assertEquals("img-1", imagen.id)
        assertEquals("perfil-1", imagen.perfil_id)
        assertEquals("storage-uuid", imagen.storage_uuid)
        assertEquals("perfil_imagen/storage-uuid", imagen.url_path)
        assertEquals("2026-05-17T10:00:00Z", imagen.created_at)
        assertEquals("2026-05-17T11:00:00Z", imagen.updated_at)
    }

    @Test
    fun perfilImagen_usesNullTimestampsByDefault() {
        val imagen = PerfilImagen(
            id = "img-1",
            perfil_id = "perfil-1",
            storage_uuid = "storage-uuid",
            url_path = "perfil_imagen/storage-uuid"
        )

        assertNull(imagen.created_at)
        assertNull(imagen.updated_at)
    }

    @Test
    fun perfilImagen_copyAndEquality() {
        val original = PerfilImagen(
            id = "img-1",
            perfil_id = "perfil-1",
            storage_uuid = "storage-uuid",
            url_path = "perfil_imagen/storage-uuid"
        )
        val updated = original.copy(storage_uuid = "new-uuid")

        assertNotEquals(original, updated)
        assertEquals("new-uuid", updated.storage_uuid)
        assertEquals(original, original.copy())
    }

    @Test
    fun perfilImagenCreateDto_holdsAllFields() {
        val dto = PerfilImagenCreateDto(
            perfil_id = "perfil-1",
            storage_uuid = "storage-uuid",
            url_path = "perfil_imagen/storage-uuid"
        )

        assertEquals("perfil-1", dto.perfil_id)
        assertEquals("storage-uuid", dto.storage_uuid)
        assertEquals("perfil_imagen/storage-uuid", dto.url_path)
    }

    @Test
    fun perfilImagenCreateDto_equality() {
        val first = PerfilImagenCreateDto("p1", "s1", "path1")
        val second = PerfilImagenCreateDto("p1", "s1", "path1")
        val different = PerfilImagenCreateDto("p2", "s1", "path1")

        assertEquals(first, second)
        assertNotEquals(first, different)
    }

    @Test
    fun perfilImagenUpdateDto_allowsPartialUpdates() {
        val emptyUpdate = PerfilImagenUpdateDto()
        assertNull(emptyUpdate.perfil_id)
        assertNull(emptyUpdate.storage_uuid)
        assertNull(emptyUpdate.url_path)

        val fullUpdate = PerfilImagenUpdateDto(
            perfil_id = "perfil-1",
            storage_uuid = "storage-uuid",
            url_path = "perfil_imagen/storage-uuid"
        )
        assertEquals("perfil-1", fullUpdate.perfil_id)
        assertEquals("storage-uuid", fullUpdate.storage_uuid)
        assertEquals("perfil_imagen/storage-uuid", fullUpdate.url_path)
    }

    @Test
    fun perfilImagenUpdateDto_copyPreservesValues() {
        val update = PerfilImagenUpdateDto(storage_uuid = "uuid")
        val copied = update.copy(url_path = "new-path")

        assertEquals("uuid", copied.storage_uuid)
        assertEquals("new-path", copied.url_path)
        assertNull(copied.perfil_id)
    }

    @Test
    fun perfilResumen_holdsDashboardMetrics() {
        val resumen = PerfilResumen(
            id = "user-1",
            nombreCompleto = "Ana Torres",
            rolSlug = "ciudadano",
            activo = true,
            ciudadId = "ciudad-1",
            totalReportes = 12,
            reportesResueltos = 5
        )

        assertEquals("user-1", resumen.id)
        assertEquals("Ana Torres", resumen.nombreCompleto)
        assertEquals("ciudadano", resumen.rolSlug)
        assertEquals(true, resumen.activo)
        assertEquals("ciudad-1", resumen.ciudadId)
        assertEquals(12, resumen.totalReportes)
        assertEquals(5, resumen.reportesResueltos)
    }

    @Test
    fun perfilResumen_copyAndEquality() {
        val resumen = PerfilResumen(
            id = "user-1",
            nombreCompleto = "Ana Torres",
            rolSlug = "ciudadano",
            activo = true,
            ciudadId = "ciudad-1",
            totalReportes = 1,
            reportesResueltos = 0
        )

        val updated = resumen.copy(totalReportes = 2, reportesResueltos = 1)
        assertEquals(2, updated.totalReportes)
        assertEquals(1, updated.reportesResueltos)
        assertEquals(resumen, resumen.copy())
        assertNotEquals(resumen, updated)
    }
}
