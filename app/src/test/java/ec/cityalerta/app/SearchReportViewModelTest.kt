package ec.cityalerta.app

import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.repository.PerfilRepository
import ec.cityalerta.app.model.repository.ReporteImagenRepository
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteStorageRepository
import ec.cityalerta.app.testdoubles.SearchReportTestFixtures
import ec.cityalerta.app.util.MainDispatcherRule
import ec.cityalerta.app.viewmodel.ReporteUI
import ec.cityalerta.app.viewmodel.SearchReportState
import ec.cityalerta.app.viewmodel.SearchReportViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
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
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchReportViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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
            reporteRepository,
            reporteImagenRepository,
            reporteStorageRepository,
            perfilRepository
        )
    }

    @Test
    fun initialState_isEmptyAndNotLoading() {
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals("", state.searchQuery)
        assertNull(state.selectedCategory)
        assertTrue(state.reportes.isEmpty())
        assertNull(state.error)
        assertEquals("", state.ciudadId)
    }

    @Test
    fun updateSearchQuery_updatesQueryInState() = runTest {
        setCiudadId(SearchReportTestFixtures.CIUDAD_ID)
        whenever(reporteRepository.searchReportes(any(), anyOrNull(), anyOrNull()))
            .thenReturn(Result.success(emptyList()))

        viewModel.updateSearchQuery("centro")
        advanceUntilIdle()

        assertEquals("centro", viewModel.state.value.searchQuery)
    }

    @Test
    fun selectCategory_updatesSelectedCategory() = runTest {
        setCiudadId(SearchReportTestFixtures.CIUDAD_ID)
        whenever(reporteRepository.searchReportes(any(), anyOrNull(), anyOrNull()))
            .thenReturn(Result.success(emptyList()))

        viewModel.selectCategory(ReportType.AGUA)
        advanceUntilIdle()

        assertEquals(ReportType.AGUA, viewModel.state.value.selectedCategory)
    }

    @Test
    fun search_withoutCiudadId_setsError() = runTest {
        viewModel.updateSearchQuery("centro")
        advanceUntilIdle()

        assertEquals("No se pudo cargar la ciudad", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
        verify(reporteRepository, never()).searchReportes(any(), anyOrNull(), anyOrNull())
    }

    @Test
    fun search_withResults_mapsToReporteUi() = runTest {
        val result = SearchReportTestFixtures.sampleSearchResult()
        val imagen = SearchReportTestFixtures.sampleImagen()

        setCiudadId(SearchReportTestFixtures.CIUDAD_ID)
        whenever(
            reporteRepository.searchReportes(
                ciudadId = eq(SearchReportTestFixtures.CIUDAD_ID),
                categoria = eq(null),
                barrioNombreQuery = eq(null)
            )
        ).thenReturn(Result.success(listOf(result)))
        whenever(reporteImagenRepository.getFirstImagenesByReporteIds(listOf("reporte-1")))
            .thenReturn(Result.success(mapOf("reporte-1" to imagen)))
        whenever(reporteStorageRepository.generateSignedImageUrls(listOf("storage-uuid-1")))
            .thenReturn(Result.success(mapOf("storage-uuid-1" to "https://signed.url/img")))

        viewModel.updateSearchQuery("")
        advanceUntilIdle()

        val reportes = viewModel.state.value.reportes
        assertEquals(1, reportes.size)
        val ui = reportes.first()
        assertEquals("reporte-1", ui.id)
        assertEquals("Bache", ui.categoria)
        assertEquals("Centro", ui.barrio)
        assertEquals("Av. Principal 123", ui.direccion)
        assertEquals("https://signed.url/img", ui.imageUrl)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun search_withBarrioQuery_passesNombreToRepository() = runTest {
        setCiudadId(SearchReportTestFixtures.CIUDAD_ID)
        whenever(
            reporteRepository.searchReportes(
                ciudadId = eq(SearchReportTestFixtures.CIUDAD_ID),
                categoria = eq(null),
                barrioNombreQuery = eq("mira")
            )
        ).thenReturn(Result.success(emptyList()))

        viewModel.updateSearchQuery("mira")
        advanceUntilIdle()

        verify(reporteRepository).searchReportes(
            ciudadId = SearchReportTestFixtures.CIUDAD_ID,
            categoria = null,
            barrioNombreQuery = "mira"
        )
    }

    @Test
    fun search_withCategory_passesCategoriaToRepository() = runTest {
        setCiudadId(SearchReportTestFixtures.CIUDAD_ID)
        whenever(
            reporteRepository.searchReportes(
                ciudadId = eq(SearchReportTestFixtures.CIUDAD_ID),
                categoria = eq(ReportType.LUZ),
                barrioNombreQuery = eq(null)
            )
        ).thenReturn(Result.success(emptyList()))

        viewModel.selectCategory(ReportType.LUZ)
        advanceUntilIdle()

        verify(reporteRepository).searchReportes(
            ciudadId = SearchReportTestFixtures.CIUDAD_ID,
            categoria = ReportType.LUZ,
            barrioNombreQuery = null
        )
    }

    @Test
    fun search_emptyResults_showsEmptyList() = runTest {
        setCiudadId(SearchReportTestFixtures.CIUDAD_ID)
        whenever(reporteRepository.searchReportes(any(), anyOrNull(), anyOrNull()))
            .thenReturn(Result.success(emptyList()))

        viewModel.updateSearchQuery("inexistente")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.reportes.isEmpty())
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun search_repositoryFailure_returnsEmptyReportes() = runTest {
        setCiudadId(SearchReportTestFixtures.CIUDAD_ID)
        whenever(reporteRepository.searchReportes(any(), anyOrNull(), anyOrNull()))
            .thenReturn(Result.failure(Exception("RPC error")))

        viewModel.updateSearchQuery("centro")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.reportes.isEmpty())
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun search_blankBarrioNombre_usesFallbackLabels() = runTest {
        val result = SearchReportTestFixtures.sampleSearchResult(
            barrioNombre = "",
            direccionAproximada = null
        )

        setCiudadId(SearchReportTestFixtures.CIUDAD_ID)
        whenever(
            reporteRepository.searchReportes(
                ciudadId = eq(SearchReportTestFixtures.CIUDAD_ID),
                categoria = eq(null),
                barrioNombreQuery = eq(null)
            )
        ).thenReturn(Result.success(listOf(result)))
        whenever(reporteImagenRepository.getFirstImagenesByReporteIds(listOf("reporte-1")))
            .thenReturn(Result.success(emptyMap()))
        whenever(reporteStorageRepository.generateSignedImageUrls(any()))
            .thenReturn(Result.success(emptyMap()))

        viewModel.selectCategory(null)
        advanceUntilIdle()

        val ui = viewModel.state.value.reportes.first()
        assertEquals("Barrio desconocido", ui.barrio)
        assertEquals("Direccion no disponible", ui.direccion)
        assertNull(ui.imageUrl)
    }

    @Test
    fun search_mapsSeguridadCategoryLabel() = runTest {
        assertCategoryLabel(ReportType.ZONA_DE_RIESGO, "Seguridad")
    }

    @Test
    fun search_mapsBacheCategoryLabel() = runTest {
        assertCategoryLabel(ReportType.BACHE, "Bache")
    }

    @Test
    fun search_mapsAguaCategoryLabel() = runTest {
        assertCategoryLabel(ReportType.AGUA, "Agua")
    }

    @Test
    fun search_mapsLuzCategoryLabel() = runTest {
        assertCategoryLabel(ReportType.LUZ, "Luz")
    }

    private suspend fun TestScope.assertCategoryLabel(type: ReportType, label: String) {
        val vm = SearchReportViewModel(
            reporteRepository,
            reporteImagenRepository,
            reporteStorageRepository,
            perfilRepository
        )
        val result = SearchReportTestFixtures.sampleSearchResult(
            reporte = SearchReportTestFixtures.sampleReporte(categoria = type)
        )

        setCiudadIdOn(vm, SearchReportTestFixtures.CIUDAD_ID)
        whenever(
            reporteRepository.searchReportes(
                ciudadId = eq(SearchReportTestFixtures.CIUDAD_ID),
                categoria = eq(null),
                barrioNombreQuery = eq(null)
            )
        ).thenReturn(Result.success(listOf(result)))
        whenever(reporteImagenRepository.getFirstImagenesByReporteIds(listOf(result.reporte.id)))
            .thenReturn(Result.success(emptyMap()))
        whenever(reporteStorageRepository.generateSignedImageUrls(any()))
            .thenReturn(Result.success(emptyMap()))

        vm.selectCategory(null)
        advanceUntilIdle()

        assertEquals(label, vm.state.value.reportes.first().categoria)
    }

    @Test
    fun searchReportState_copyPreservesFields() {
        val reportes = listOf(
            ReporteUI(
                id = "r1",
                categoria = "Bache",
                imageUrl = null,
                barrio = "Centro",
                direccion = "Calle 1",
                descripcion = "Detalle",
                estado = "Pendiente",
                fecha = "2024-01-01",
                timeAgo = "Hace poco"
            )
        )
        val state = SearchReportState(
            isLoading = true,
            searchQuery = "centro",
            selectedCategory = ReportType.BACHE,
            reportes = reportes,
            error = null,
            ciudadId = SearchReportTestFixtures.CIUDAD_ID
        )

        val copy = state.copy(isLoading = false)

        assertFalse(copy.isLoading)
        assertEquals("centro", copy.searchQuery)
        assertEquals(ReportType.BACHE, copy.selectedCategory)
        assertEquals(1, copy.reportes.size)
    }

    private fun setCiudadId(ciudadId: String) = setCiudadIdOn(viewModel, ciudadId)

    private fun setCiudadIdOn(target: SearchReportViewModel, ciudadId: String) {
        val field = SearchReportViewModel::class.java.getDeclaredField("_state")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(target) as MutableStateFlow<SearchReportState>
        stateFlow.value = stateFlow.value.copy(ciudadId = ciudadId)
    }
}
