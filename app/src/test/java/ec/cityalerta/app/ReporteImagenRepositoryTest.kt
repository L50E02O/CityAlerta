package ec.cityalerta.app

import ec.cityalerta.app.model.data.reporteimagen.ReporteImagen
import ec.cityalerta.app.model.data.reporteimagen.ReporteImagenCreateDto
import ec.cityalerta.app.model.data.reporteimagen.ReporteImagenUpdateDto
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.repository.ReporteImagenRepository
import io.github.jan.supabase.SupabaseClient
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests unitarios para ReporteImagenRepository. Valida las operaciones CRUD para imagenes de reportes.
 */
@RunWith(MockitoJUnitRunner::class)
class ReporteImagenRepositoryTest {

    @Mock
    private lateinit var mockSupabaseClient: SupabaseClient

    private lateinit var repository: ReporteImagenRepository

    @Before
    fun setUp() {
        repository = ReporteImagenRepository()
    }

    private fun mockSupabaseProvider(block: suspend () -> Unit) = runTest {
        org.mockito.kotlin.mockStatic(SupabaseProvider::class.java).use { mockedStatic ->
            whenever(SupabaseProvider.client).thenReturn(mockSupabaseClient)
            block()
        }
    }

    @Test
    fun testCreateReporteImagenSuccessfully() = runTest {
        val createDto = ReporteImagenCreateDto(
            reporteId = "reporte-1",
            storageUuid = "uuid-123",
            urlPath = "reports/reporte-1/image-1.jpg"
        )

        val mockResponse = JsonObject(
            mapOf(
                "id" to JsonPrimitive("imagen-1"),
                "reporte_id" to JsonPrimitive("reporte-1"),
                "storage_uuid" to JsonPrimitive("uuid-123"),
                "url_path" to JsonPrimitive("reports/reporte-1/image-1.jpg"),
                "created_at" to JsonPrimitive("2024-01-01T00:00:00Z")
            )
        )

        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            val mockRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockSupabaseClient.from("reporte_imagen")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.insert(any<JsonObject>(), any())).thenReturn(mockRequestBuilder)
            whenever(mockRequestBuilder.decodeList<JsonObject>()).thenReturn(listOf(mockResponse))

            val result = repository.create(createDto)

            assertTrue(result.isSuccess)
            assertEquals("imagen-1", result.getOrNull()?.id)
        }
    }

    @Test
    fun testGetAllReporteImagenesSuccessfully() = runTest {
        val mockResponse1 = JsonObject(
            mapOf(
                "id" to JsonPrimitive("imagen-1"),
                "reporte_id" to JsonPrimitive("reporte-1"),
                "storage_uuid" to JsonPrimitive("uuid-1"),
                "url_path" to JsonPrimitive("path-1.jpg")
            )
        )

        val mockResponse2 = JsonObject(
            mapOf(
                "id" to JsonPrimitive("imagen-2"),
                "reporte_id" to JsonPrimitive("reporte-1"),
                "storage_uuid" to JsonPrimitive("uuid-2"),
                "url_path" to JsonPrimitive("path-2.jpg")
            )
        )

        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            whenever(mockSupabaseClient.from("reporte_imagen")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.select(any())).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.decodeList<JsonObject>()).thenReturn(
                listOf(mockResponse1, mockResponse2)
            )

            val result = repository.getAll()

            assertTrue(result.isSuccess)
            assertEquals(2, result.getOrNull()?.size)
        }
    }

    @Test
    fun testGetReporteImagenByIdSuccessfully() = runTest {
        val mockResponse = JsonObject(
            mapOf(
                "id" to JsonPrimitive("imagen-1"),
                "reporte_id" to JsonPrimitive("reporte-1"),
                "storage_uuid" to JsonPrimitive("uuid-123"),
                "url_path" to JsonPrimitive("reports/reporte-1/image-1.jpg")
            )
        )

        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            whenever(mockSupabaseClient.from("reporte_imagen")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.select(any())).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.decodeList<JsonObject>()).thenReturn(listOf(mockResponse))

            val result = repository.getById("imagen-1")

            assertTrue(result.isSuccess)
            assertEquals("imagen-1", result.getOrNull()?.id)
        }
    }

    @Test
    fun testUpdateReporteImagenSuccessfully() = runTest {
        val updateDto = ReporteImagenUpdateDto(
            reporteId = null,
            storageUuid = "uuid-updated",
            urlPath = "reports/reporte-1/image-updated.jpg"
        )

        val mockResponse = JsonObject(
            mapOf(
                "id" to JsonPrimitive("imagen-1"),
                "storage_uuid" to JsonPrimitive("uuid-updated"),
                "url_path" to JsonPrimitive("reports/reporte-1/image-updated.jpg")
            )
        )

        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            whenever(mockSupabaseClient.from("reporte_imagen")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.update(any())).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.decodeList<JsonObject>()).thenReturn(listOf(mockResponse))

            val result = repository.update(updateDto, "imagen-1")

            assertTrue(result.isSuccess)
        }
    }

    @Test
    fun testDeleteReporteImagenSuccessfully() = runTest {
        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            whenever(mockSupabaseClient.from("reporte_imagen")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.delete()).thenReturn(mockQueryBuilder)

            val result = repository.delete("imagen-1")

            assertTrue(result.isSuccess)
        }
    }

    @Test
    fun testCreateReporteImagenReturnsFailureOnException() = runTest {
        val createDto = ReporteImagenCreateDto(
            reporteId = "reporte-1",
            storageUuid = "uuid-123",
            urlPath = "reports/reporte-1/image-1.jpg"
        )

        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            whenever(mockSupabaseClient.from("reporte_imagen")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.insert(any())).thenThrow(RuntimeException("Error storing image"))

            val result = repository.create(createDto)

            assertTrue(result.isFailure)
        }
    }

    @Test
    fun testGetByIdReturnsNullWhenNotFound() = runTest {
        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            whenever(mockSupabaseClient.from("reporte_imagen")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.select(any())).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.decodeList<JsonObject>()).thenReturn(emptyList<JsonObject>())

            val result = repository.getById("non-existent-id")

            assertTrue(result.isSuccess)
            assertEquals(null, result.getOrNull())
        }
    }
}

