package ec.cityalerta.app

import ec.cityalerta.app.model.data.perfil.Perfil
import ec.cityalerta.app.model.data.perfil.PerfilCreateDto
import ec.cityalerta.app.model.data.perfil.PerfilUpdateDto
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.repository.PerfilRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder
import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder
import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder
import io.github.jan.supabase.postgrest.query.decodeList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.mockStatic
import org.mockito.kotlin.whenever
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests unitarios para PerfilRepository. Valida las operaciones CRUD basicas
 * para perfiles utilizando mocks de Supabase.
 */
@RunWith(MockitoJUnitRunner::class)
class PerfilRepositoryTest {

    @Mock
    private lateinit var mockSupabaseClient: SupabaseClient

    @Mock
    private lateinit var mockPostgrest: Postgrest

    private lateinit var repository: PerfilRepository

    @Before
    fun setUp() {
        repository = PerfilRepository()
    }

    private fun mockSupabaseProvider(block: suspend () -> Unit) = runTest {
        mockStatic(SupabaseProvider::class.java).use { mockedStatic ->
            whenever(SupabaseProvider.client).thenReturn(mockSupabaseClient)
            block()
        }
    }

    @Test
    fun testCreatePerfilSuccessfully() = runTest {
        val createDto = PerfilCreateDto(
            nombreCompleto = "Juan Perez",
            rolSlug = "admin",
            activo = true
        )

        val mockResponse = JsonObject(
            mapOf(
                "id" to JsonPrimitive("123"),
                "nombre_completo" to JsonPrimitive("Juan Perez"),
                "rol_slug" to JsonPrimitive("admin"),
                "activo" to JsonPrimitive(true),
                "created_at" to JsonPrimitive("2024-01-01T00:00:00Z"),
                "updated_at" to JsonPrimitive("2024-01-01T00:00:00Z")
            )
        )

        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            val mockRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockSupabaseClient.from("perfil")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.insert(any<JsonObject>(), any())).thenReturn(mockRequestBuilder)
            whenever(mockRequestBuilder.decodeList<JsonObject>()).thenReturn(listOf(mockResponse))

            val result = repository.create(createDto)

            assertTrue(result.isSuccess)
            assertEquals("123", result.getOrNull()?.id)
            assertEquals("Juan Perez", result.getOrNull()?.nombreCompleto)
        }
    }

    @Test
    fun testGetAllPerfilesSuccessfully() = runTest {
        val mockResponse1 = JsonObject(
            mapOf(
                "id" to JsonPrimitive("1"),
                "nombre_completo" to JsonPrimitive("Juan"),
                "rol_slug" to JsonPrimitive("admin"),
                "activo" to JsonPrimitive(true)
            )
        )

        val mockResponse2 = JsonObject(
            mapOf(
                "id" to JsonPrimitive("2"),
                "nombre_completo" to JsonPrimitive("Maria"),
                "rol_slug" to JsonPrimitive("user"),
                "activo" to JsonPrimitive(true)
            )
        )

        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            val mockRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockSupabaseClient.from("perfil")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.select(any<Columns>(), any())).thenReturn(mockRequestBuilder)
            whenever(mockRequestBuilder.decodeList<JsonObject>()).thenReturn(
                listOf(mockResponse1, mockResponse2)
            )

            val result = repository.getAll()

            assertTrue(result.isSuccess)
            assertEquals(2, result.getOrNull()?.size)
        }
    }

    @Test
    fun testGetPerfilByIdSuccessfully() = runTest {
        val mockResponse = JsonObject(
            mapOf(
                "id" to JsonPrimitive("123"),
                "nombre_completo" to JsonPrimitive("Juan Perez"),
                "rol_slug" to JsonPrimitive("admin"),
                "activo" to JsonPrimitive(true)
            )
        )

        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            whenever(mockSupabaseClient.from("perfil")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.select(any())).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.decodeList<JsonObject>()).thenReturn(listOf(mockResponse))

            val result = repository.getById("123")

            assertTrue(result.isSuccess)
            assertEquals("123", result.getOrNull()?.id)
        }
    }

    @Test
    fun testUpdatePerfilSuccessfully() = runTest {
        val updateDto = PerfilUpdateDto(
            nombreCompleto = "Juan Pablo",
            rolSlug = "moderator",
            activo = true
        )

        val mockResponse = JsonObject(
            mapOf(
                "id" to JsonPrimitive("123"),
                "nombre_completo" to JsonPrimitive("Juan Pablo"),
                "rol_slug" to JsonPrimitive("moderator"),
                "activo" to JsonPrimitive(true)
            )
        )

        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            whenever(mockSupabaseClient.from("perfil")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.update(any())).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.decodeList<JsonObject>()).thenReturn(listOf(mockResponse))

            val result = repository.update(updateDto, "123")

            assertTrue(result.isSuccess)
            assertEquals("Juan Pablo", result.getOrNull()?.nombreCompleto)
        }
    }

    @Test
    fun testDeletePerfilSuccessfully() = runTest {
        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            whenever(mockSupabaseClient.from("perfil")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.delete()).thenReturn(mockQueryBuilder)

            val result = repository.delete("123")

            assertTrue(result.isSuccess)
        }
    }

    @Test
    fun testCreatePerfilReturnsFailureOnException() = runTest {
        val createDto = PerfilCreateDto(
            nombreCompleto = "Test",
            rolSlug = "user",
            activo = true
        )

        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            whenever(mockSupabaseClient.from("perfil")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.insert(any())).thenThrow(RuntimeException("Network error"))

            val result = repository.create(createDto)

            assertTrue(result.isFailure)
        }
    }
}

