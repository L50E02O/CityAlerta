package ec.cityalerta.app

import ec.cityalerta.app.model.data.perfilimagen.PerfilImagen
import ec.cityalerta.app.model.data.perfilimagen.PerfilImagenCreateDto
import ec.cityalerta.app.model.data.perfilimagen.PerfilImagenUpdateDto
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PerfilImagenDataClassesCoverageTest {

    @Test
    fun testPerfilImagen() {
        val imagen = PerfilImagen(
            id = "imagen-1",
            perfil_id = "perfil-1",
            storage_uuid = "uuid-123",
            url_path = "https://example.com/image.jpg",
            created_at = "2024-01-01",
            updated_at = "2024-01-02"
        )
        assertEquals("imagen-1", imagen.id)
        assertEquals("perfil-1", imagen.perfil_id)
        assertEquals("uuid-123", imagen.storage_uuid)
        assertEquals("https://example.com/image.jpg", imagen.url_path)
        assertEquals("2024-01-01", imagen.created_at)
        assertEquals("2024-01-02", imagen.updated_at)
    }

    @Test
    fun testPerfilImagenWithoutDates() {
        val imagen = PerfilImagen(
            id = "imagen-1",
            perfil_id = "perfil-1",
            storage_uuid = "uuid-123",
            url_path = "https://example.com/image.jpg"
        )
        assertNull(imagen.created_at)
        assertNull(imagen.updated_at)
    }

    @Test
    fun testPerfilImagenCreateDto() {
        val dto = PerfilImagenCreateDto(
            perfil_id = "perfil-1",
            storage_uuid = "uuid-123",
            url_path = "https://example.com/image.jpg"
        )
        assertEquals("perfil-1", dto.perfil_id)
        assertEquals("uuid-123", dto.storage_uuid)
        assertEquals("https://example.com/image.jpg", dto.url_path)
    }

    @Test
    fun testPerfilImagenUpdateDto() {
        val dto = PerfilImagenUpdateDto(
            perfil_id = "perfil-1",
            storage_uuid = "uuid-123",
            url_path = "https://example.com/image.jpg"
        )
        assertEquals("perfil-1", dto.perfil_id)
        assertEquals("uuid-123", dto.storage_uuid)
        assertEquals("https://example.com/image.jpg", dto.url_path)
    }

    @Test
    fun testPerfilImagenUpdateDtoWithNulls() {
        val dto = PerfilImagenUpdateDto()
        assertNull(dto.perfil_id)
        assertNull(dto.storage_uuid)
        assertNull(dto.url_path)
    }
}
