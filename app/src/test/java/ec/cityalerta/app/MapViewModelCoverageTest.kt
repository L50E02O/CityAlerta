package ec.cityalerta.app

import ec.cityalerta.app.model.data.contracts.map.MapRepositoryContract
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.testdoubles.FakeAuthRepository
import ec.cityalerta.app.testdoubles.MapViewModelTestFixtures
import ec.cityalerta.app.testdoubles.MapViewModelTestFixtures.CIUDAD_UUID
import ec.cityalerta.app.testdoubles.NoOpRiskZoneDetector
import ec.cityalerta.app.testdoubles.RepositoryMockHelpers
import ec.cityalerta.app.viewmodel.MapViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelCoverageTest {

    private val testDispatcher = StandardTestDispatcher()
    private val authRepository = FakeAuthRepository()
    private lateinit var viewModel: MapViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun TestScope.awaitMapLoad() {
        withContext(Dispatchers.Default.limitedParallelism(1)) {
            withTimeout(5_000) {
                while (viewModel.uiState.value.isLoading) {
                    testDispatcher.scheduler.advanceUntilIdle()
                    delay(20)
                }
            }
        }
    }

    @Test
    fun onClusterClicked_actualizaEstadoMultiReporte() {
        viewModel = createViewModel()
        viewModel.onClusterClicked(ReportType.AGUA, 5)

        val state = viewModel.uiState
        assertTrue(state.isMultiReport)
        assertEquals(5, state.reportCount)
        assertNotNull(state.selectedReport)
        assertEquals(ReportType.AGUA, state.selectedReport?.categoria)
    }

    @Test
    fun onDismissReport_limpiaEstadoMultiReporte() {
        viewModel = createViewModel()
        viewModel.onClusterClicked(ReportType.LUZ, 3)
        viewModel.onDismissReport()

        assertNull(viewModel.uiState.value.selectedReport)
        assertFalse(viewModel.uiState.value.isMultiReport)
        assertEquals(0, viewModel.uiState.value.reportCount)
    }

    @Test
    fun loadCiudad_current_resuelveDesdeAuth() = runTest(testDispatcher) {
        authRepository.getCiudadIdResult = Result.success(CIUDAD_UUID)
        val ciudad = MapViewModelTestFixtures.sampleCiudad()
        val mapRepository = MapRepositoryContract { id ->
            assertEquals(CIUDAD_UUID, id)
            ciudad
        }
        viewModel = MapViewModel(
            mapRepository,
            RepositoryMockHelpers.reporteRepositoryReturning(emptyList()),
            RepositoryMockHelpers.ubicacionRepositoryReturning(emptyList()),
            authRepository,
            RepositoryMockHelpers.barrioRepository(),
            NoOpRiskZoneDetector
        )

        viewModel.loadCiudad("current")
        awaitMapLoad()

        assertEquals(ciudad, viewModel.uiState.value.ciudad)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun loadCiudad_sinCiudad_muestraError() = runTest(testDispatcher) {
        authRepository.getCiudadIdResult = Result.success("")
        viewModel = createViewModel()

        viewModel.loadCiudad("Sin ciudad")
        awaitMapLoad()

        assertEquals("No se pudo determinar la ciudad actual", viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.ciudad)
    }

    @Test
    fun loadCiudad_blank_muestraError() = runTest(testDispatcher) {
        authRepository.getCiudadIdResult = Result.failure(RuntimeException("sin perfil"))
        viewModel = createViewModel()

        viewModel.loadCiudad("   ")
        awaitMapLoad()

        assertEquals("No se pudo determinar la ciudad actual", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun onCategorySelected_limpiaReporteSeleccionado() {
        viewModel = createViewModel()
        viewModel.onClusterClicked(ReportType.BACHE, 2)
        viewModel.onCategorySelected(ReportType.AGUA)

        assertEquals(ReportType.AGUA, viewModel.uiState.value.selectedCategory)
        assertNull(viewModel.uiState.value.selectedReport)
        assertFalse(viewModel.uiState.value.isMultiReport)
    }

    private fun createViewModel(): MapViewModel {
        val mapRepository = MapRepositoryContract { MapViewModelTestFixtures.sampleCiudad() }
        return MapViewModel(
            mapRepository,
            RepositoryMockHelpers.reporteRepositoryReturning(emptyList()),
            RepositoryMockHelpers.ubicacionRepositoryReturning(emptyList()),
            authRepository,
            RepositoryMockHelpers.barrioRepository(),
            NoOpRiskZoneDetector
        )
    }
}
