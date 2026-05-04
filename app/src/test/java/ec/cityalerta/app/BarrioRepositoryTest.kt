package ec.cityalerta.app

import ec.cityalerta.app.model.data.barrio.Barrio
import ec.cityalerta.app.model.data.barrio.BarrioCreateDto
import ec.cityalerta.app.model.data.barrio.BarrioUpdateDto
import ec.cityalerta.app.model.data.geoJson.Geometry
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.repository.BarrioRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder
import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder
import io.github.jan.supabase.postgrest.query.decodeAs
import io.github.jan.supabase.postgrest.query.decodeList

/**
 * Tests unitarios para BarrioRepository. Valida las operaciones CRUD para barrios.
 */
@RunWith(MockitoJUnitRunner::class)
class BarrioRepositoryTest {

    @Mock
    private lateinit var mockSupabaseClient: SupabaseClient

    @Mock
    private lateinit var mockPostgrest: Postgrest

    private lateinit var repository: BarrioRepository

    @Before
    fun setUp() {
        repository = BarrioRepository()
    }

    private fun mockSupabaseProvider(block: suspend () -> Unit) = runTest {
        org.mockito.kotlin.mockStatic(SupabaseProvider::class.java).use { mockedStatic ->
            whenever(SupabaseProvider.client).thenReturn(mockSupabaseClient)
            whenever(mockSupabaseClient.postgrest).thenReturn(mockPostgrest)
            block()
        }
    }

    private fun createMockGeometry(): JsonObject {
        return JsonObject(
            mapOf(
                "type" to JsonPrimitive("Polygon"),
                "coordinates" to JsonArray(
                    listOf(
                        JsonArray(
                            listOf(
                                JsonArray(listOf(JsonPrimitive(-80.5), JsonPrimitive(-0.5))),
                                JsonArray(listOf(JsonPrimitive(-80.6), JsonPrimitive(-0.6)))
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    fun testCreateBarrioSuccessfully() = runTest {
        val geometry = Geometry(
            type = "Polygon",
            coordinates = listOf(listOf(listOf(-80.5, -0.5), listOf(-80.6, -0.6)))
        )
        val createDto = BarrioCreateDto("ciudad-1", "Centro", "MEDIO", geometry)
        
        val mockGeometryResponse = createMockGeometry()
        val mockBarrioResponse = JsonObject(mapOf(
            "id" to JsonPrimitive("barrio-1"),
            "ciudad_id" to JsonPrimitive("ciudad-1"),
            "nombre" to JsonPrimitive("Centro"),
            "nivel_peligrosidad" to JsonPrimitive("MEDIO"),
            "perimetro" to mockGeometryResponse
        ))

        mockSupabaseProvider {
            val mockRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockPostgrest.rpc(any<String>(), any<Map<String, Any>>())).thenReturn(mockRequestBuilder)
            whenever(mockRequestBuilder.decodeAs<String>()).thenReturn("barrio-1")
            
            // Mock for getById call inside create
            val mockGetByIdRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockPostgrest.rpc(org.mockito.kotlin.eq("get_barrio_by_id"), any())).thenReturn(mockGetByIdRequestBuilder)
            whenever(mockGetByIdRequestBuilder.decodeList<JsonObject>()).thenReturn(listOf(mockBarrioResponse))

            val result = repository.create(createDto)

            assertTrue(result.isSuccess)
            assertEquals("barrio-1", result.getOrNull()?.id)
        }
    }

    @Test
    fun testGetAllBarriosSuccessfully() = runTest {
        val mockGeometry = createMockGeometry()
        val mockResponse1 = JsonObject(mapOf(
            "id" to JsonPrimitive("1"),
            "ciudad_id" to JsonPrimitive("ciudad-1"),
            "nombre" to JsonPrimitive("Centro"),
            "nivel_peligrosidad" to JsonPrimitive("BAJO"),
            "perimetro" to mockGeometry
        ))

        mockSupabaseProvider {
            val mockRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockPostgrest.rpc(org.mockito.kotlin.eq("get_barrios"))).thenReturn(mockRequestBuilder)
            whenever(mockRequestBuilder.decodeList<JsonObject>()).thenReturn(listOf(mockResponse1))

            val result = repository.getAll()

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrNull()?.size)
            assertEquals("Centro", result.getOrNull()?.get(0)?.nombre)
        }
    }

    @Test
    fun testGetBarrioByIdSuccessfully() = runTest {
        val mockGeometry = createMockGeometry()
        val mockResponse = JsonObject(mapOf(
            "id" to JsonPrimitive("barrio-1"),
            "ciudad_id" to JsonPrimitive("ciudad-1"),
            "nombre" to JsonPrimitive("Centro"),
            "nivel_peligrosidad" to JsonPrimitive("MEDIO"),
            "perimetro" to mockGeometry
        ))

        mockSupabaseProvider {
            val mockRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockPostgrest.rpc(org.mockito.kotlin.eq("get_barrio_by_id"), any())).thenReturn(mockRequestBuilder)
            whenever(mockRequestBuilder.decodeList<JsonObject>()).thenReturn(listOf(mockResponse))

            val result = repository.getById("barrio-1")

            assertTrue(result.isSuccess)
            assertEquals("barrio-1", result.getOrNull()?.id)
        }
    }

    @Test
    fun testUpdateBarrioSuccessfully() = runTest {
        val geometry = Geometry(
            type = "Polygon",
            coordinates = listOf(listOf(listOf(-80.5, -0.5), listOf(-80.6, -0.6)))
        )
        val updateDto = BarrioUpdateDto("Centro Actualizado", "BAJO", geometry)
        
        val mockGeometry = createMockGeometry()
        val mockResponse = JsonObject(mapOf(
            "id" to JsonPrimitive("barrio-1"),
            "nombre" to JsonPrimitive("Centro Actualizado"),
            "nivel_peligrosidad" to JsonPrimitive("BAJO"),
            "perimetro" to mockGeometry
        ))

        mockSupabaseProvider {
            val mockUpdateRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockPostgrest.rpc(org.mockito.kotlin.eq("update_barrio"), any())).thenReturn(mockUpdateRequestBuilder)
            
            val mockGetByIdRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockPostgrest.rpc(org.mockito.kotlin.eq("get_barrio_by_id"), any())).thenReturn(mockGetByIdRequestBuilder)
            whenever(mockGetByIdRequestBuilder.decodeList<JsonObject>()).thenReturn(listOf(mockResponse))

            val result = repository.update(updateDto, "barrio-1")

            assertTrue(result.isSuccess)
            assertEquals("Centro Actualizado", result.getOrNull()?.nombre)
        }
    }

    @Test
    fun testDeleteBarrioSuccessfully() = runTest {
        mockSupabaseProvider {
            val mockRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockPostgrest.rpc(org.mockito.kotlin.eq("delete_barrio"), any())).thenReturn(mockRequestBuilder)

            val result = repository.delete("barrio-1")

            assertTrue(result.isSuccess)
        }
    }

    @Test
    fun testCreateBarrioReturnsFailureOnException() = runTest {
        val createDto = BarrioCreateDto("ciudad-1", "Centro", "MEDIO", Geometry("Polygon", emptyList()))

        mockSupabaseProvider {
            whenever(mockPostgrest.rpc(org.mockito.kotlin.eq("create_barrio"), any()))
                .thenThrow(RuntimeException("Error creating barrio"))

            val result = repository.create(createDto)

            assertTrue(result.isFailure)
        }
    }
}

