package ec.cityalerta.app

import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.data.ciudad.CiudadCreateDto
import ec.cityalerta.app.model.data.ciudad.CiudadUpdateDto
import ec.cityalerta.app.model.data.geoJson.Geometry
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.repository.CiudadRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder
import io.github.jan.supabase.postgrest.query.decodeAs
import io.github.jan.supabase.postgrest.query.decodeList
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

/**
 * Tests unitarios para CiudadRepository. Valida las operaciones CRUD para ciudades.
 */
@RunWith(MockitoJUnitRunner::class)
class CiudadRepositoryTest {

    @Mock
    private lateinit var mockSupabaseClient: SupabaseClient

    @Mock
    private lateinit var mockPostgrest: Postgrest

    private lateinit var repository: CiudadRepository

    @Before
    fun setUp() {
        repository = CiudadRepository()
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
                "type" to JsonPrimitive("FeatureCollection"),
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
    fun testCreateCiudadSuccessfully() = runTest {
        val geometry = Geometry(
            type = "FeatureCollection",
            coordinates = listOf(listOf(listOf(-80.5, -0.5), listOf(-80.6, -0.6)))
        )
        val createDto = CiudadCreateDto("Manta", "Ecuador", geometry, -0.9542, -80.7314)
        
        val mockGeometryResponse = createMockGeometry()
        val mockCiudadResponse = JsonObject(mapOf(
            "id" to JsonPrimitive("ciudad-manta"),
            "nombre" to JsonPrimitive("Manta"),
            "pais" to JsonPrimitive("Ecuador"),
            "geojson" to mockGeometryResponse,
            "centro_lat" to JsonPrimitive(-0.9542),
            "centro_lng" to JsonPrimitive(-80.7314)
        ))

        mockSupabaseProvider {
            val mockRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockPostgrest.rpc(org.mockito.kotlin.eq("create_ciudad"), any())).thenReturn(mockRequestBuilder)
            whenever(mockRequestBuilder.decodeAs<String>()).thenReturn("ciudad-manta")
            
            val mockGetByIdRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockPostgrest.rpc(org.mockito.kotlin.eq("get_ciudad_by_id"), any())).thenReturn(mockGetByIdRequestBuilder)
            whenever(mockGetByIdRequestBuilder.decodeList<JsonObject>()).thenReturn(listOf(mockCiudadResponse))

            val result = repository.create(createDto)

            assertTrue(result.isSuccess)
            assertEquals("ciudad-manta", result.getOrNull()?.id)
        }
    }

    @Test
    fun testGetAllCiudadesSuccessfully() = runTest {
        val mockGeometry = createMockGeometry()
        val mockResponse1 = JsonObject(mapOf(
            "id" to JsonPrimitive("ciudad-1"),
            "nombre" to JsonPrimitive("Manta"),
            "pais" to JsonPrimitive("Ecuador"),
            "geojson" to mockGeometry,
            "centro_lat" to JsonPrimitive(-0.9542),
            "centro_lng" to JsonPrimitive(-80.7314)
        ))

        mockSupabaseProvider {
            val mockRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockPostgrest.rpc(org.mockito.kotlin.eq("get_ciudades"))).thenReturn(mockRequestBuilder)
            whenever(mockRequestBuilder.decodeList<JsonObject>()).thenReturn(listOf(mockResponse1))

            val result = repository.getAll()

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrNull()?.size)
            assertEquals("Manta", result.getOrNull()?.get(0)?.nombre)
        }
    }

    @Test
    fun testGetCiudadByIdSuccessfully() = runTest {
        val mockGeometry = createMockGeometry()
        val mockResponse = JsonObject(mapOf(
            "id" to JsonPrimitive("ciudad-manta"),
            "nombre" to JsonPrimitive("Manta"),
            "pais" to JsonPrimitive("Ecuador"),
            "geojson" to mockGeometry,
            "centro_lat" to JsonPrimitive(-0.9542),
            "centro_lng" to JsonPrimitive(-80.7314)
        ))

        mockSupabaseProvider {
            val mockRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockPostgrest.rpc(org.mockito.kotlin.eq("get_ciudad_by_id"), any())).thenReturn(mockRequestBuilder)
            whenever(mockRequestBuilder.decodeList<JsonObject>()).thenReturn(listOf(mockResponse))

            val result = repository.getById("ciudad-manta")

            assertTrue(result.isSuccess)
            assertEquals("ciudad-manta", result.getOrNull()?.id)
        }
    }

    @Test
    fun testUpdateCiudadSuccessfully() = runTest {
        val geometry = Geometry(
            type = "FeatureCollection",
            coordinates = listOf(listOf(listOf(-80.5, -0.5), listOf(-80.6, -0.6)))
        )
        val updateDto = CiudadUpdateDto("Manta Actualizado", "Ecuador", geometry, -0.9542, -80.7314)
        
        val mockGeometry = createMockGeometry()
        val mockResponse = JsonObject(mapOf(
            "id" to JsonPrimitive("ciudad-manta"),
            "nombre" to JsonPrimitive("Manta Actualizado"),
            "pais" to JsonPrimitive("Ecuador"),
            "geojson" to mockGeometry
        ))

        mockSupabaseProvider {
            val mockUpdateRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockPostgrest.rpc(org.mockito.kotlin.eq("update_ciudad"), any())).thenReturn(mockUpdateRequestBuilder)
            
            val mockGetByIdRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockPostgrest.rpc(org.mockito.kotlin.eq("get_ciudad_by_id"), any())).thenReturn(mockGetByIdRequestBuilder)
            whenever(mockGetByIdRequestBuilder.decodeList<JsonObject>()).thenReturn(listOf(mockResponse))

            val result = repository.update(updateDto, "ciudad-manta")

            assertTrue(result.isSuccess)
            assertEquals("Manta Actualizado", result.getOrNull()?.nombre)
        }
    }

    @Test
    fun testDeleteCiudadSuccessfully() = runTest {
        mockSupabaseProvider {
            val mockRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockPostgrest.rpc(org.mockito.kotlin.eq("delete_ciudad"), any())).thenReturn(mockRequestBuilder)

            val result = repository.delete("ciudad-manta")

            assertTrue(result.isSuccess)
        }
    }

    @Test
    fun testCreateCiudadReturnsFailureOnException() = runTest {
        val createDto = CiudadCreateDto("Manta", "Ecuador", Geometry("FeatureCollection", emptyList()), -0.9542, -80.7314)

        mockSupabaseProvider {
            whenever(mockPostgrest.rpc(org.mockito.kotlin.eq("create_ciudad"), any()))
                .thenThrow(RuntimeException("Error creating ciudad"))

            val result = repository.create(createDto)

            assertTrue(result.isFailure)
        }
    }
}

