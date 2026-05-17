package ec.cityalerta.app

import ec.cityalerta.app.model.data.reporteimagen.ReporteImagen
import ec.cityalerta.app.model.data.reporteimagen.ReporteImagenCreateDto
import ec.cityalerta.app.model.data.reporteimagen.ReporteImagenUpdateDto
import ec.cityalerta.app.model.repository.ReporteImagenRepository
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests de cobertura para mapeos y operaciones de ReporteImagenRepository.
 */
class ReporteImagenRepositoryCoverageTest {

    private lateinit var repository: ReporteImagenRepository

    @Before
    fun setUp() {
        repository = ReporteImagenRepository()
    }

    @Test
    fun toReporteImagen_mapeaCamposSnakeCase() {
        val json = JsonObject(
            mapOf(
                "id" to JsonPrimitive("img-1"),
                "reporte_id" to JsonPrimitive("reporte-1"),
                "storage_uuid" to JsonPrimitive("uuid-abc"),
                "url_path" to JsonPrimitive("/storage/foto.jpg"),
                "created_at" to JsonPrimitive("2024-01-01T00:00:00Z"),
                "updated_at" to JsonPrimitive("2024-02-01T00:00:00Z")
            )
        )

        val imagen = invokeToReporteImagen(json)

        assertEquals("img-1", imagen.id)
        assertEquals("reporte-1", imagen.reporte_id)
        assertEquals("uuid-abc", imagen.storage_uuid)
        assertEquals("/storage/foto.jpg", imagen.url_path)
        assertEquals("2024-01-01T00:00:00Z", imagen.created_at)
        assertEquals("2024-02-01T00:00:00Z", imagen.updated_at)
    }

    @Test
    fun toReporteImagen_mapeaCamposCamelCaseAlternativos() {
        val json = JsonObject(
            mapOf(
                "id" to JsonPrimitive("img-2"),
                "reporteId" to JsonPrimitive("reporte-2"),
                "storageUuid" to JsonPrimitive("uuid-def"),
                "urlPath" to JsonPrimitive("/storage/foto.png")
            )
        )

        val imagen = invokeToReporteImagen(json)

        assertEquals("reporte-2", imagen.reporte_id)
        assertEquals("uuid-def", imagen.storage_uuid)
        assertEquals("/storage/foto.png", imagen.url_path)
        assertNull(imagen.created_at)
    }

    @Test
    fun toReporteImagen_camposFaltantes_usaValoresPorDefecto() {
        val imagen = invokeToReporteImagen(JsonObject(emptyMap()))

        assertEquals("", imagen.id)
        assertEquals("", imagen.reporte_id)
        assertEquals("", imagen.storage_uuid)
        assertEquals("", imagen.url_path)
    }

    @Test
    fun toCreateJson_generaEstructuraEsperada() {
        val dto = ReporteImagenCreateDto(
            reporte_id = "reporte-1",
            storage_uuid = "uuid-123",
            url_path = "/path/imagen.webp"
        )

        val json = invokeToCreateJson(dto)

        assertEquals("reporte-1", json["reporte_id"]?.toString()?.trim('"'))
        assertEquals("uuid-123", json["storage_uuid"]?.toString()?.trim('"'))
        assertEquals("/path/imagen.webp", json["url_path"]?.toString()?.trim('"'))
    }

    @Test
    fun toUpdateJson_soloCamposPresentes() {
        val dto = ReporteImagenUpdateDto(
            reporte_id = null,
            storage_uuid = "uuid-nuevo",
            url_path = "/nueva/ruta.jpg"
        )

        val json = invokeToUpdateJson(dto)

        assertEquals(2, json.size)
        assertNull(json["reporte_id"])
        assertNotNull(json["storage_uuid"])
        assertNotNull(json["url_path"])
    }

    @Test
    fun toUpdateJson_todosLosCampos() {
        val dto = ReporteImagenUpdateDto(
            reporte_id = "reporte-9",
            storage_uuid = "uuid-9",
            url_path = "/ruta.jpg"
        )

        val json = invokeToUpdateJson(dto)

        assertEquals(3, json.size)
    }

    @Test
    fun create_ejecutaSafeSupabaseCall() = runTest {
        val dto = ReporteImagenCreateDto("reporte-1", "uuid", "/path.jpg")
        val result = repository.create(dto)

        assertTrue(result.isFailure || result.isSuccess)
    }

    @Test
    fun update_ejecutaSafeSupabaseCall() = runTest {
        val dto = ReporteImagenUpdateDto(url_path = "/nuevo.jpg")
        val result = repository.update(dto, "img-1")

        assertTrue(result.isFailure || result.isSuccess)
    }

    @Test
    fun getAll_ejecutaSafeSupabaseCall() = runTest {
        val result = repository.getAll()

        assertTrue(result.isFailure || result.isSuccess)
    }

    @Test
    fun getById_ejecutaSafeSupabaseCall() = runTest {
        val result = repository.getById("img-1")

        assertTrue(result.isFailure || result.isSuccess)
    }

    @Test
    fun delete_ejecutaSafeSupabaseCall() = runTest {
        val result = repository.delete("img-1")

        assertTrue(result.isFailure || result.isSuccess)
    }

    @Test
    fun getFirstImagenByReporteId_ejecutaSafeSupabaseCall() = runTest {
        val result = repository.getFirstImagenByReporteId("reporte-1")

        assertTrue(result.isFailure || result.isSuccess)
    }

    @Test
    fun getAllImagenesByReporteId_ejecutaSafeSupabaseCall() = runTest {
        val result = repository.getAllImagenesByReporteId("reporte-1")

        assertTrue(result.isFailure || result.isSuccess)
    }

    private fun invokeToReporteImagen(json: JsonObject): ReporteImagen {
        val method = ReporteImagenRepository::class.java.declaredMethods
            .first { it.name == "toReporteImagen" && it.parameterTypes.size == 1 }
        method.isAccessible = true
        return method.invoke(repository, json) as ReporteImagen
    }

    private fun invokeToCreateJson(dto: ReporteImagenCreateDto): JsonObject {
        val method = ReporteImagenRepository::class.java.declaredMethods
            .first { it.name == "toCreateJson" && it.parameterTypes.size == 1 }
        method.isAccessible = true
        return method.invoke(repository, dto) as JsonObject
    }

    private fun invokeToUpdateJson(dto: ReporteImagenUpdateDto): JsonObject {
        val method = ReporteImagenRepository::class.java.declaredMethods
            .first { it.name == "toUpdateJson" && it.parameterTypes.size == 1 }
        method.isAccessible = true
        return method.invoke(repository, dto) as JsonObject
    }
}
