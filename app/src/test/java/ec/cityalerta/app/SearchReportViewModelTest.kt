package ec.cityalerta.app

import ec.cityalerta.app.model.data.perfil.Perfil
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.data.reporte.ReporteSearchResult
import ec.cityalerta.app.model.repository.PerfilRepository
import ec.cityalerta.app.model.repository.ReporteImagenRepository
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteStorageRepository
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class SearchReportViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var authRepository: ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
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
    fun testInitialState() {
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("", state.searchQuery)
        assertNull(state.selectedCategory)
        assertEquals(0, state.reportes.size)
    }

    @Test
    fun testUpdateSearchQuery_Debounce() = runTest {
        val userId = "user-123"
        val ciudadId = "city-1"
        val perfil = Perfil(userId, "Juan", "user", true, null, null, ciudadId)

        whenever(authRepository.getUserId()).thenReturn(Result.success(userId))
        whenever(perfilRepository.getById(userId)).thenReturn(Result.success(perfil))
        whenever(reporteRepository.searchReportes(eq(ciudadId), any(), any())).thenReturn(Result.success(emptyList()))

        viewModel.loadData()
        advanceUntilIdle()

        viewModel.updateSearchQuery("test")
        advanceTimeBy(600) // Advance past 500ms delay
        advanceUntilIdle()
        
        assertEquals("test", viewModel.state.value.searchQuery)
        assertEquals(ciudadId, viewModel.state.value.ciudadId)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun testSelectCategory() = runTest {
        val userId = "user-123"
        val ciudadId = "city-1"
        val perfil = Perfil(userId, "Juan", "user", true, null, null, ciudadId)

        whenever(authRepository.getUserId()).thenReturn(Result.success(userId))
        whenever(perfilRepository.getById(userId)).thenReturn(Result.success(perfil))
        whenever(reporteRepository.searchReportes(any(), any(), any())).thenReturn(Result.success(emptyList()))

        viewModel.loadData()
        advanceUntilIdle()

        viewModel.selectCategory(ReportType.BACHE)
        advanceUntilIdle()
        
        assertEquals(ReportType.BACHE, viewModel.state.value.selectedCategory)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun testSearch_Success() = runTest {
        val userId = "user-123"
        val ciudadId = "city-1"
        val perfil = Perfil(userId, "Juan", "user", true, null, null, ciudadId)
        val reporte = Reporte("rep-1", userId, ciudadId, "ubic-1", "Desc", ReporteEstado.PENDIENTE, "2024-01-01", ReportType.BACHE, "2024-01-01T12:00:00Z", null, "barrio-1")
        val searchResult = ReporteSearchResult(reporte, "Centro", "Calle 1")

        whenever(authRepository.getUserId()).thenReturn(Result.success(userId))
        whenever(perfilRepository.getById(userId)).thenReturn(Result.success(perfil))
        whenever(reporteRepository.searchReportes(eq(ciudadId), any(), any())).thenReturn(Result.success(listOf(searchResult)))
        whenever(reporteImagenRepository.getFirstImagenesByReporteIds(any())).thenReturn(Result.success(emptyMap()))
        whenever(reporteStorageRepository.generateSignedImageUrls(any())).thenReturn(Result.success(emptyMap()))

        viewModel.loadData()
        advanceUntilIdle()

        assertNull(viewModel.state.value.error)
        assertEquals(ciudadId, viewModel.state.value.ciudadId)
        
        // Ensure all background tasks finished
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.reportes.size)
        assertEquals("Centro", viewModel.state.value.reportes[0].barrio)
    }
}
