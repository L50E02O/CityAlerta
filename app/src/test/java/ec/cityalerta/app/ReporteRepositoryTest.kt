package ec.cityalerta.app

import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporte.ReporteCreateDto
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.data.reporte.ReporteUpdateDto
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.repository.ReporteRepository
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
 * Tests unitarios para ReporteRepository. Valida las operaciones CRUD para reportes.
 */
@RunWith(MockitoJUnitRunner::class)
class ReporteRepositoryTest {

    @Mock
    private lateinit var mockSupabaseClient: SupabaseClient

    private lateinit var repository: ReporteRepository

    @Before
    fun setUp() {
        repository = ReporteRepository()
    }

    private fun mockSupabaseProvider(block: suspend () -> Unit) = runTest {
        org.mockito.kotlin.mockStatic(SupabaseProvider::class.java).use { mockedStatic ->
            whenever(SupabaseProvider.client).thenReturn(mockSupabaseClient)
            block()
        }
    }

    @Test
    fun testCreateReporteSuccessfully() = runTest {
        val createDto = ReporteCreateDto(
            usuarioId = "user-1",
            ciudadId = "ciudad-1",
            ubicacionId = "ubicacion-1",
            descripcion = "Robo en la esquina",
            estado = ReporteEstado.PENDIENTE,
            fechaReporte = "2024-01-01",
            categoria = "ROBO"
        )

        val mockResponse = JsonObject(
            mapOf(
                "id" to JsonPrimitive("report-1"),
                "usuario_id" to JsonPrimitive("user-1"),
                "ciudad_id" to JsonPrimitive("ciudad-1"),
                "ubicacion_id" to JsonPrimitive("ubicacion-1"),
                "descripcion" to JsonPrimitive("Robo en la esquina"),
                "estado_slug" to JsonPrimitive("PENDIENTE"),
                "fecha_reporte" to JsonPrimitive("2024-01-01"),
                "categoria" to JsonPrimitive("ROBO")
            )
        )

        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            val mockRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockSupabaseClient.from("reporte")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.insert(any<JsonObject>(), any())).thenReturn(mockRequestBuilder)
            whenever(mockRequestBuilder.decodeList<JsonObject>()).thenReturn(listOf(mockResponse))

            val result = repository.create(createDto)

            assertTrue(result.isSuccess)
            assertEquals("report-1", result.getOrNull()?.id)
            assertEquals("Robo en la esquina", result.getOrNull()?.descripcion)
        }
    }

    @Test
    fun testGetAllReportesSuccessfully() = runTest {
        val mockResponse1 = JsonObject(
            mapOf(
                "id" to JsonPrimitive("1"),
                "usuario_id" to JsonPrimitive("user-1"),
                "ciudad_id" to JsonPrimitive("ciudad-1"),
                "descripcion" to JsonPrimitive("Robo"),
                "estado_slug" to JsonPrimitive("PENDIENTE"),
                "categoria" to JsonPrimitive("ROBO")
            )
        )

        val mockResponse2 = JsonObject(
            mapOf(
                "id" to JsonPrimitive("2"),
                "usuario_id" to JsonPrimitive("user-2"),
                "ciudad_id" to JsonPrimitive("ciudad-1"),
                "descripcion" to JsonPrimitive("Asalto"),
                "estado_slug" to JsonPrimitive("RESUELTO"),
                "categoria" to JsonPrimitive("ASALTO")
            )
        )

        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            val mockRequestBuilder = mock<PostgrestRequestBuilder>()
            whenever(mockSupabaseClient.from("reporte")).thenReturn(mockQueryBuilder)
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
    fun testGetReporteByIdSuccessfully() = runTest {
        val mockResponse = JsonObject(
            mapOf(
                "id" to JsonPrimitive("report-1"),
                "usuario_id" to JsonPrimitive("user-1"),
                "ciudad_id" to JsonPrimitive("ciudad-1"),
                "descripcion" to JsonPrimitive("Reporte test"),
                "estado_slug" to JsonPrimitive("PENDIENTE"),
                "categoria" to JsonPrimitive("OTRO")
            )
        )

        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            whenever(mockSupabaseClient.from("reporte")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.select(any())).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.decodeList<JsonObject>()).thenReturn(listOf(mockResponse))

            val result = repository.getById("report-1")

            assertTrue(result.isSuccess)
            assertEquals("report-1", result.getOrNull()?.id)
        }
    }

    @Test
    fun testUpdateReporteSuccessfully() = runTest {
        val updateDto = ReporteUpdateDto(
            usuarioId = null,
            ciudadId = null,
            ubicacionId = null,
            descripcion = "Reporte actualizado",
            estado = ReporteEstado.RESUELTO,
            fechaReporte = null,
            categoria = null
        )

        val mockResponse = JsonObject(
            mapOf(
                "id" to JsonPrimitive("report-1"),
                "descripcion" to JsonPrimitive("Reporte actualizado"),
                "estado_slug" to JsonPrimitive("RESUELTO")
            )
        )

        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            whenever(mockSupabaseClient.from("reporte")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.update(any())).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.decodeList<JsonObject>()).thenReturn(listOf(mockResponse))

            val result = repository.update(updateDto, "report-1")

            assertTrue(result.isSuccess)
        }
    }

    @Test
    fun testDeleteReporteSuccessfully() = runTest {
        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            whenever(mockSupabaseClient.from("reporte")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.delete()).thenReturn(mockQueryBuilder)

            val result = repository.delete("report-1")

            assertTrue(result.isSuccess)
        }
    }

    @Test
    fun testCreateReporteReturnsFailureOnException() = runTest {
        val createDto = ReporteCreateDto(
            usuarioId = "user-1",
            ciudadId = "ciudad-1",
            ubicacionId = "ubicacion-1",
            descripcion = "Test",
            estado = ReporteEstado.PENDIENTE,
            fechaReporte = "2024-01-01",
            categoria = "OTRO"
        )

        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            whenever(mockSupabaseClient.from("reporte")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.insert(any())).thenThrow(RuntimeException("Error creating"))

            val result = repository.create(createDto)

            assertTrue(result.isFailure)
        }
    }

    @Test
    fun testGetByIdReturnsNullWhenNotFound() = runTest {
        mockSupabaseProvider {
            val mockQueryBuilder = mock<PostgrestQueryBuilder>()
            whenever(mockSupabaseClient.from("reporte")).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.select(any())).thenReturn(mockQueryBuilder)
            whenever(mockQueryBuilder.decodeList<JsonObject>()).thenReturn(emptyList<JsonObject>())

            val result = repository.getById("non-existent-id")

            assertTrue(result.isSuccess)
            assertEquals(null, result.getOrNull())
        }
    }
}

