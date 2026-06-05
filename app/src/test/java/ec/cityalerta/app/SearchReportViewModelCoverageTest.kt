package ec.cityalerta.app

import ec.cityalerta.app.model.data.perfil.Perfil
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.data.reporte.ReporteSearchResult
import ec.cityalerta.app.model.data.reporteimagen.ReporteImagen
import ec.cityalerta.app.model.repository.PerfilRepository
import ec.cityalerta.app.model.repository.ReporteImagenRepository
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteStorageRepository
import ec.cityalerta.app.testdoubles.FakeAuthRepository
import ec.cityalerta.app.util.MainDispatcherRule
import ec.cityalerta.app.viewmodel.SearchReportViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchReportViewModelCoverageTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository()

    @Mock
    private lateinit var reporteRepository: ReporteRepository
    @Mock
    private lateinit var reporteImagenRepository: ReporteImagenRepository
    @Mock
    private lateinit var reporteStorageRepository: ReporteStorageRepository
    @Mock
    private lateinit var perfilRepository: PerfilRepository

    private lateinit var viewModel: SearchReportViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = SearchReportViewModel(
            authRepository,
            reporteRepository,
            reporteImagenRepository,
            reporteStorageRepository,
            perfilRepository
        )
    }

    @Test
    fun loadData_sinUsuario_noCargaCiudad() = runTest {
        authRepository.getUserIdResult = Result.failure(RuntimeException("sin sesion"))

        viewModel.loadData()
        advanceUntilIdle()

        assertEquals("", viewModel.state.value.ciudadId)
        assertEquals(0, viewModel.state.value.reportes.size)
    }

    @Test
    fun loadData_sinPerfil_noMarcaError() = runTest {
        authRepository.getUserIdResult = Result.success("user-1")
        whenever(perfilRepository.getById("user-1")).thenReturn(Result.success(null))

        viewModel.loadData()
        advanceUntilIdle()

        assertEquals("", viewModel.state.value.ciudadId)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun loadData_excepcion_muestraError() = runTest {
        authRepository.getUserIdResult = Result.success("user-1")
        whenever(perfilRepository.getById("user-1")).thenThrow(RuntimeException("db"))

        viewModel.loadData()
        advanceUntilIdle()

        assertEquals("Error al cargar ciudad", viewModel.state.value.error)
    }

    @Test
    fun search_sinCiudadId_muestraError() = runTest {
        viewModel.updateSearchQuery("centro")
        advanceTimeBy(600)
        advanceUntilIdle()

        assertEquals("No se pudo cargar la ciudad", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun search_conImagenes_mapeaUrlFirmada() = runTest {
        val userId = "user-1"
        val ciudadId = "city-1"
        val reporte = Reporte(
            id = "rep-1",
            usuario_id = userId,
            ciudad_id = ciudadId,
            ubicacion_id = "ubic-1",
            descripcion = "Fuga de agua",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-01",
            categoria = ReportType.AGUA,
            created_at = Instant.now().minusSeconds(86_400).toString(),
            barrio_id = "barrio-1"
        )
        val searchResult = ReporteSearchResult(reporte, "Centro", "Calle 1")
        val imagen = ReporteImagen("img-1", "rep-1", "uuid-1", "path/1", "2024-01-01")

        authRepository.getUserIdResult = Result.success(userId)
        whenever(perfilRepository.getById(userId)).thenReturn(
            Result.success(Perfil(userId, "Juan", "user", true, null, null, ciudadId))
        )
        whenever(reporteRepository.searchReportes(eq(ciudadId), anyOrNull(), anyOrNull()))
            .thenReturn(Result.success(listOf(searchResult)))
        whenever(reporteImagenRepository.getFirstImagenesByReporteIds(any()))
            .thenReturn(Result.success(mapOf("rep-1" to imagen)))
        whenever(reporteStorageRepository.generateSignedImageUrls(any()))
            .thenReturn(Result.success(mapOf("uuid-1" to "https://signed.example/1")))

        viewModel.loadData()
        advanceUntilIdle()

        val reporteUi = viewModel.state.value.reportes.first()
        assertEquals("https://signed.example/1", reporteUi.imageUrl)
        assertEquals("Agua", reporteUi.categoria)
        assertEquals("Pendiente", reporteUi.estado)
        assertTrue(reporteUi.timeAgo.contains("dia"))
    }

    @Test
    fun search_falloRepositorio_muestraError() = runTest {
        val userId = "user-1"
        val ciudadId = "city-1"
        authRepository.getUserIdResult = Result.success(userId)
        whenever(perfilRepository.getById(userId)).thenReturn(
            Result.success(Perfil(userId, "Juan", "user", true, null, null, ciudadId))
        )
        whenever(reporteRepository.searchReportes(any(), anyOrNull(), anyOrNull()))
            .thenThrow(RuntimeException("timeout"))

        viewModel.loadData()
        advanceUntilIdle()

        assertEquals("timeout", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun selectCategory_null_limpiaFiltro() = runTest {
        val userId = "user-1"
        val ciudadId = "city-1"
        authRepository.getUserIdResult = Result.success(userId)
        whenever(perfilRepository.getById(userId)).thenReturn(
            Result.success(Perfil(userId, "Juan", "user", true, null, null, ciudadId))
        )
        whenever(reporteRepository.searchReportes(any(), anyOrNull(), anyOrNull()))
            .thenReturn(Result.success(emptyList()))

        viewModel.loadData()
        advanceUntilIdle()
        viewModel.selectCategory(ReportType.BACHE)
        advanceUntilIdle()
        viewModel.selectCategory(null)
        advanceUntilIdle()

        assertNull(viewModel.state.value.selectedCategory)
    }

    @Test
    fun loadData_noRecargaSiYaHayReportes() = runTest {
        val userId = "user-1"
        val ciudadId = "city-1"
        val reporte = Reporte(
            id = "rep-1",
            usuario_id = userId,
            ciudad_id = ciudadId,
            ubicacion_id = "ubic-1",
            descripcion = "Desc",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-01",
            categoria = ReportType.BACHE,
            barrio_id = "barrio-1"
        )
        authRepository.getUserIdResult = Result.success(userId)
        whenever(perfilRepository.getById(userId)).thenReturn(
            Result.success(Perfil(userId, "Juan", "user", true, null, null, ciudadId))
        )
        whenever(reporteRepository.searchReportes(any(), anyOrNull(), anyOrNull()))
            .thenReturn(Result.success(listOf(ReporteSearchResult(reporte, "Centro", "Calle 1"))))
        whenever(reporteImagenRepository.getFirstImagenesByReporteIds(any()))
            .thenReturn(Result.success(emptyMap()))
        whenever(reporteStorageRepository.generateSignedImageUrls(any()))
            .thenReturn(Result.success(emptyMap()))

        viewModel.loadData()
        advanceUntilIdle()
        viewModel.loadData()
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.reportes.size)
    }

    @Test
    fun search_barrioVacio_usaTextoPorDefecto() = runTest {
        val userId = "user-1"
        val ciudadId = "city-1"
        val reporte = Reporte(
            id = "rep-1",
            usuario_id = userId,
            ciudad_id = ciudadId,
            ubicacion_id = "ubic-1",
            descripcion = "Desc",
            estado = ReporteEstado.EN_PROCESO,
            fecha_reporte = "2024-01-01",
            categoria = ReportType.ZONA_DE_RIESGO,
            barrio_id = "barrio-1"
        )
        authRepository.getUserIdResult = Result.success(userId)
        whenever(perfilRepository.getById(userId)).thenReturn(
            Result.success(Perfil(userId, "Juan", "user", true, null, null, ciudadId))
        )
        whenever(reporteRepository.searchReportes(any(), anyOrNull(), anyOrNull()))
            .thenReturn(Result.success(listOf(ReporteSearchResult(reporte, "", null))))
        whenever(reporteImagenRepository.getFirstImagenesByReporteIds(any()))
            .thenReturn(Result.success(emptyMap()))
        whenever(reporteStorageRepository.generateSignedImageUrls(any()))
            .thenReturn(Result.success(emptyMap()))

        viewModel.loadData()
        advanceUntilIdle()

        val reporteUi = viewModel.state.value.reportes.first()
        assertEquals("Barrio desconocido", reporteUi.barrio)
        assertEquals("Direccion no disponible", reporteUi.direccion)
        assertEquals("Seguridad", reporteUi.categoria)
        assertEquals("En Proceso", reporteUi.estado)
    }
}
