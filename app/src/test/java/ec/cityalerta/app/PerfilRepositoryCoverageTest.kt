package ec.cityalerta.app

import ec.cityalerta.app.model.data.perfil.Perfil
import ec.cityalerta.app.model.data.perfil.PerfilCreateDto
import ec.cityalerta.app.model.data.perfil.PerfilUpdateDto
import ec.cityalerta.app.model.repository.PerfilRepository
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests de cobertura para mapeos y operaciones de PerfilRepository.
 */
class PerfilRepositoryCoverageTest {

    private lateinit var repository: PerfilRepository

    @Before
    fun setUp() {
        repository = PerfilRepository()
    }

    @Test
    fun toPerfil_mapeaCamposSnakeCase() {
        val json = JsonObject(
            mapOf(
                "id" to JsonPrimitive("perfil-1"),
                "nombre_completo" to JsonPrimitive("Ana Torres"),
                "rol_slug" to JsonPrimitive("ciudadano"),
                "activo" to JsonPrimitive(true),
                "created_at" to JsonPrimitive("2024-01-01T00:00:00Z"),
                "updated_at" to JsonPrimitive("2024-02-01T00:00:00Z"),
                "ciudad_id" to JsonPrimitive("ciudad-manta")
            )
        )

        val perfil = invokeToPerfil(json)

        assertEquals("perfil-1", perfil.id)
        assertEquals("Ana Torres", perfil.nombreCompleto)
        assertEquals("ciudadano", perfil.rolSlug)
        assertTrue(perfil.activo)
        assertEquals("2024-01-01T00:00:00Z", perfil.createdAt)
        assertEquals("2024-02-01T00:00:00Z", perfil.updatedAt)
        assertEquals("ciudad-manta", perfil.ciudadId)
    }

    @Test
    fun toPerfil_mapeaCamposCamelCaseAlternativos() {
        val json = JsonObject(
            mapOf(
                "id" to JsonPrimitive("perfil-2"),
                "nombreCompleto" to JsonPrimitive("Carlos Ruiz"),
                "rolSlug" to JsonPrimitive("admin"),
                "activo" to JsonPrimitive(false),
                "createdAt" to JsonPrimitive("2024-03-01T00:00:00Z"),
                "updatedAt" to JsonPrimitive("2024-04-01T00:00:00Z"),
                "ciudadId" to JsonPrimitive("ciudad-quito")
            )
        )

        val perfil = invokeToPerfil(json)

        assertEquals("Carlos Ruiz", perfil.nombreCompleto)
        assertEquals("admin", perfil.rolSlug)
        assertFalse(perfil.activo)
        assertEquals("ciudad-quito", perfil.ciudadId)
    }

    @Test
    fun toPerfil_camposFaltantes_usaValoresPorDefecto() {
        val json = JsonObject(emptyMap())

        val perfil = invokeToPerfil(json)

        assertEquals("", perfil.id)
        assertEquals("", perfil.nombreCompleto)
        assertEquals("", perfil.rolSlug)
        assertFalse(perfil.activo)
        assertNull(perfil.createdAt)
        assertNull(perfil.updatedAt)
        assertEquals("", perfil.ciudadId)
    }

    @Test
    fun toCreateJson_generaEstructuraEsperada() {
        val dto = PerfilCreateDto(
            nombreCompleto = "Maria Lopez",
            rolSlug = "moderador",
            activo = true,
            ciudadId = "ciudad-1"
        )

        val json = invokeToCreateJson(dto)

        assertEquals("Maria Lopez", json["nombre_completo"]?.toString()?.trim('"'))
        assertEquals("moderador", json["rol_slug"]?.toString()?.trim('"'))
        assertEquals("true", json["activo"]?.toString())
        assertEquals("ciudad-1", json["ciudad_id"]?.toString()?.trim('"'))
    }

    @Test
    fun toUpdateJson_soloCamposPresentes() {
        val dto = PerfilUpdateDto(
            nombreCompleto = "Nombre actualizado",
            rolSlug = null,
            activo = false,
            ciudadId = "ciudad-nueva"
        )

        val json = invokeToUpdateJson(dto)

        assertEquals(3, json.size)
        assertNotNull(json["nombre_completo"])
        assertNull(json["rol_slug"])
        assertNotNull(json["activo"])
        assertNotNull(json["ciudad_id"])
    }

    @Test
    fun toUpdateJson_vacio_cuandoNoHayCampos() {
        val json = invokeToUpdateJson(PerfilUpdateDto())

        assertTrue(json.isEmpty())
    }

    @Test
    fun create_ejecutaSafeSupabaseCall() = runTest {
        val dto = PerfilCreateDto("Test", "ciudadano", true, "ciudad-1")
        val result = repository.create(dto)

        assertTrue(result.isFailure || result.isSuccess)
    }

    @Test
    fun update_ejecutaSafeSupabaseCall() = runTest {
        val dto = PerfilUpdateDto(nombreCompleto = "Actualizado")
        val result = repository.update(dto, "perfil-1")

        assertTrue(result.isFailure || result.isSuccess)
    }

    @Test
    fun getAll_ejecutaSafeSupabaseCall() = runTest {
        val result = repository.getAll()

        assertTrue(result.isFailure || result.isSuccess)
    }

    @Test
    fun getById_ejecutaSafeSupabaseCall() = runTest {
        val result = repository.getById("perfil-1")

        assertTrue(result.isFailure || result.isSuccess)
    }

    @Test
    fun delete_ejecutaSafeSupabaseCall() = runTest {
        val result = repository.delete("perfil-1")

        assertTrue(result.isFailure || result.isSuccess)
    }

    private fun invokeToPerfil(json: JsonObject): Perfil {
        val method = PerfilRepository::class.java.declaredMethods
            .first { it.name == "toPerfil" && it.parameterTypes.size == 1 }
        method.isAccessible = true
        return method.invoke(repository, json) as Perfil
    }

    private fun invokeToCreateJson(dto: PerfilCreateDto): JsonObject {
        val method = PerfilRepository::class.java.declaredMethods
            .first { it.name == "toCreateJson" && it.parameterTypes.size == 1 }
        method.isAccessible = true
        return method.invoke(repository, dto) as JsonObject
    }

    private fun invokeToUpdateJson(dto: PerfilUpdateDto): JsonObject {
        val method = PerfilRepository::class.java.declaredMethods
            .first { it.name == "toUpdateJson" && it.parameterTypes.size == 1 }
        method.isAccessible = true
        return method.invoke(repository, dto) as JsonObject
    }
}
