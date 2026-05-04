package ec.cityalerta.app
 
import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacion
import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacionCreateDto
import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacionUpdateDto
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.repository.ReporteUbicacionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder
import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder
import io.github.jan.supabase.postgrest.query.decodeAs
import io.github.jan.supabase.postgrest.query.decodeList
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
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
 * Tests unitarios para ReporteUbicacionRepository. Valida las operaciones CRUD
 * para ubicaciones de reportes.
 */
@RunWith(MockitoJUnitRunner::class)
class ReporteUbicacionRepositoryTest {

    @Mock
    private lateinit var mockSupabaseClient: SupabaseClient

    private lateinit var repository: ReporteUbicacionRepository

    @Before
    fun setUp() {
        repository = ReporteUbicacionRepository()
    }

    private fun mockSupabaseProvider(block: suspend () -> Unit) = runTest {
        org.mockito.kotlin.mockStatic(SupabaseProvider::class.java).use { mockedStatic ->
            whenever(SupabaseProvider.client).thenReturn(mockSupabaseClient)
            block()
        }
    }

    @Test
    fun testCreateReporteUbicacionSuccessfully() = runTest {
        val createDto = ReporteUbicacionCreateDto(
            lat = -0.9542,
            lng = -80.7314,
            direccionAproximada = "Avenida principal, Manta"
        )

        val mockResponse = JsonObject(
            mapOf(
                "id" to JsonPrimitive("ubicacion-1"),
                "lat" to JsonPrimitive(-0.9542),
                "lng" to JsonPrimitive(-80.7314),
                "direccion_aproximada" to JsonPrimitive("Avenida principal, Manta"),
                "created_at" to JsonPrimitive("2024-01-01T00:00:00Z")
            )
        )

        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            val mockRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockSupabaseClient.from("reporte_ubicaciones")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.insert(any<JsonObject>(), any())).thenReturn(mockRequestBuilder)
            whenever(mockRequestBuilder.decodeList<JsonObject>()).thenReturn(listOf(mockResponse))

            val result = repository.create(createDto)

            assertTrue(result.isSuccess)
            assertEquals("ubicacion-1", result.getOrNull()?.id)
            assertEquals(-0.9542, result.getOrNull()?.lat)
            assertEquals(-80.7314, result.getOrNull()?.lng)
        }
    }

    @Test
    fun testGetAllReporteUbicacionesSuccessfully() = runTest {
        val mockResponse1 = JsonObject(
            mapOf(
                "id" to JsonPrimitive("ubicacion-1"),
                "lat" to JsonPrimitive(-0.9542),
                "lng" to JsonPrimitive(-80.7314),
                "direccion_aproximada" to JsonPrimitive("Calle 1")
            )
        )

        val mockResponse2 = JsonObject(
            mapOf(
                "id" to JsonPrimitive("ubicacion-2"),
                "lat" to JsonPrimitive(-0.9500),
                "lng" to JsonPrimitive(-80.7300),
                "direccion_aproximada" to JsonPrimitive("Calle 2")
            )
        )

        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            val mockRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockSupabaseClient.from("reporte_ubicaciones")).thenReturn(mockQueryBuilder)
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
    fun testGetReporteUbicacionByIdSuccessfully() = runTest {
        val mockResponse = JsonObject(
            mapOf(
                "id" to JsonPrimitive("ubicacion-1"),
                "lat" to JsonPrimitive(-0.9542),
                "lng" to JsonPrimitive(-80.7314),
                "direccion_aproximada" to JsonPrimitive("Avenida principal, Manta")
            )
        )

        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            val mockRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockSupabaseClient.from("reporte_ubicaciones")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.select(any<Columns>(), any())).thenReturn(mockRequestBuilder)
            whenever(mockRequestBuilder.decodeList<JsonObject>()).thenReturn(listOf(mockResponse))

            val result = repository.getById("ubicacion-1")

            assertTrue(result.isSuccess)
            assertEquals("ubicacion-1", result.getOrNull()?.id)
            assertEquals(-0.9542, result.getOrNull()?.lat)
        }
    }

    @Test
    fun testUpdateReporteUbicacionSuccessfully() = runTest {
        val updateDto = ReporteUbicacionUpdateDto(
            lat = -0.9500,
            lng = -80.7300,
            direccionAproximada = "Direccion actualizada"
        )

        val mockResponse = JsonObject(
            mapOf(
                "id" to JsonPrimitive("ubicacion-1"),
                "lat" to JsonPrimitive(-0.9500),
                "lng" to JsonPrimitive(-80.7300),
                "direccion_aproximada" to JsonPrimitive("Direccion actualizada")
            )
        )

        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            val mockRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockSupabaseClient.from("reporte_ubicaciones")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.update(any<JsonObject>(), any())).thenReturn(mockRequestBuilder)
            whenever(mockRequestBuilder.decodeList<JsonObject>()).thenReturn(listOf(mockResponse))

            val result = repository.update(updateDto, "ubicacion-1")

            assertTrue(result.isSuccess)
            assertEquals(-0.9500, result.getOrNull()?.lat)
        }
    }

    @Test
    fun testDeleteReporteUbicacionSuccessfully() = runTest {
        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            whenever(mockSupabaseClient.from("reporte_ubicaciones")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.delete()).thenReturn(mockQueryBuilder)

            val result = repository.delete("ubicacion-1")

            assertTrue(result.isSuccess)
        }
    }

    @Test
    fun testCreateReporteUbicacionReturnsFailureOnException() = runTest {
        val createDto = ReporteUbicacionCreateDto(
            lat = -0.9542,
            lng = -80.7314,
            direccionAproximada = "Avenida principal"
        )

        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            whenever(mockSupabaseClient.from("reporte_ubicaciones")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.insert(any<JsonObject>(), any())).thenThrow(RuntimeException("Error storing location"))

            val result = repository.create(createDto)

            assertTrue(result.isFailure)
        }
    }

    @Test
    fun testGetByIdReturnsNullWhenNotFound() = runTest {
        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            val mockRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockSupabaseClient.from("reporte_ubicaciones")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.select(any<Columns>(), any())).thenReturn(mockRequestBuilder)
            whenever(mockRequestBuilder.decodeList<JsonObject>()).thenReturn(emptyList<JsonObject>())

            val result = repository.getById("non-existent-id")

            assertTrue(result.isSuccess)
            assertEquals(null, result.getOrNull())
        }
    }
}

