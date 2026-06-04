package ec.cityalerta.app

import ec.cityalerta.app.model.data.contracts.map.MapRepositoryContract
import ec.cityalerta.app.testdoubles.FakeAuthRepository
import ec.cityalerta.app.testdoubles.MapViewModelTestFixtures
import ec.cityalerta.app.testdoubles.NoOpRiskZoneDetector
import ec.cityalerta.app.testdoubles.MapViewModelTestFixtures.CIUDAD_UUID
import ec.cityalerta.app.testdoubles.RepositoryMockHelpers
import ec.cityalerta.app.viewmodel.MapViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests de cobertura para loadCiudad y flujos asociados de MapViewModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelLoadCiudadCoverageTest {

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
                while (viewModel.uiState.isLoading) {
                    advanceUntilIdle()
                    testDispatcher.scheduler.advanceUntilIdle()
                    delay(20)
                }
            }
        }
    }

    @Test
    fun loadCiudad_uuid_cargaReportesYMarcadores() = runTest(testDispatcher) {
        val ciudad = MapViewModelTestFixtures.sampleCiudad()
        val reporte = MapViewModelTestFixtures.sampleReporte()
        val ubicacion = MapViewModelTestFixtures.sampleUbicacion()
        val mapRepository = MapRepositoryContract { id ->
            assertEquals(CIUDAD_UUID, id)
            ciudad
        }
        viewModel = MapViewModel(
            mapRepository,
            RepositoryMockHelpers.reporteRepositoryReturning(listOf(reporte)),
            RepositoryMockHelpers.ubicacionRepositoryReturning(listOf(ubicacion)),
            authRepository,
            RepositoryMockHelpers.barrioRepository(),
            NoOpRiskZoneDetector
        )

        viewModel.loadCiudad(CIUDAD_UUID)
        awaitMapLoad()

        val state = viewModel.uiState
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(ciudad, state.ciudad)
        assertEquals(1, state.reports.size)
        assertEquals(1, state.reportMarkers.size)
        assertEquals("reporte-1", state.reportMarkers.first().id)
        assertEquals("BACHE", state.reportMarkers.first().title)
    }

    @Test
    fun loadCiudad_porNombre_resuelveUuid() = runTest(testDispatcher) {
        val ciudad = MapViewModelTestFixtures.sampleCiudad(nombre = "Manta")
        authRepository.buscarCiudadPorNombreResult = Result.success(CIUDAD_UUID)
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

        viewModel.loadCiudad("Manta")
        awaitMapLoad()
        assertEquals("Manta", authRepository.lastCiudadNombre)
        assertEquals(ciudad, viewModel.uiState.ciudad)
        assertTrue(viewModel.uiState.reportMarkers.isEmpty())
    }

    @Test
    fun loadCiudad_porNombre_sinResultado_usaNombreOriginal() = runTest(testDispatcher) {
        authRepository.buscarCiudadPorNombreResult = Result.success(null)
        val mapRepository = MapRepositoryContract { id ->
            assertEquals("Manta", id)
            null
        }
        viewModel = MapViewModel(
            mapRepository,
            RepositoryMockHelpers.reporteRepositoryReturning(emptyList()),
            RepositoryMockHelpers.ubicacionRepositoryReturning(emptyList()),
            authRepository,
            RepositoryMockHelpers.barrioRepository(),
            NoOpRiskZoneDetector
        )

        viewModel.loadCiudad("Manta")
        awaitMapLoad()
        assertEquals("Ciudad no encontrada en el sistema", viewModel.uiState.errorMessage)
        assertFalse(viewModel.uiState.isLoading)
    }

    @Test
    fun loadCiudad_ciudadNoEncontrada() = runTest(testDispatcher) {
        val mapRepository = MapRepositoryContract { null }
        viewModel = MapViewModel(
            mapRepository,
            RepositoryMockHelpers.reporteRepositoryReturning(emptyList()),
            RepositoryMockHelpers.ubicacionRepositoryReturning(emptyList()),
            authRepository,
            RepositoryMockHelpers.barrioRepository(),
            NoOpRiskZoneDetector
        )

        viewModel.loadCiudad(CIUDAD_UUID)
        awaitMapLoad()
        assertEquals("Ciudad no encontrada en el sistema", viewModel.uiState.errorMessage)
        assertNull(viewModel.uiState.ciudad)
    }

    @Test
    fun loadCiudad_falloAlObtenerReportes() = runTest(testDispatcher) {
        val ciudad = MapViewModelTestFixtures.sampleCiudad()
        val mapRepository = MapRepositoryContract { ciudad }
        viewModel = MapViewModel(
            mapRepository,
            RepositoryMockHelpers.reporteRepositoryFailing("DB error"),
            RepositoryMockHelpers.ubicacionRepositoryReturning(emptyList()),
            authRepository,
            RepositoryMockHelpers.barrioRepository(),
            NoOpRiskZoneDetector
        )

        viewModel.loadCiudad(CIUDAD_UUID)
        awaitMapLoad()
        assertEquals(ciudad, viewModel.uiState.ciudad)
        assertEquals("Error al obtener datos de la base de datos", viewModel.uiState.errorMessage)
        assertTrue(viewModel.uiState.reportMarkers.isEmpty())
    }

    @Test
    fun loadCiudad_falloAlObtenerUbicaciones() = runTest(testDispatcher) {
        val ciudad = MapViewModelTestFixtures.sampleCiudad()
        val mapRepository = MapRepositoryContract { ciudad }
        viewModel = MapViewModel(
            mapRepository,
            RepositoryMockHelpers.reporteRepositoryReturning(emptyList()),
            RepositoryMockHelpers.ubicacionRepositoryFailing("ubicaciones"),
            authRepository,
            RepositoryMockHelpers.barrioRepository(),
            NoOpRiskZoneDetector
        )

        viewModel.loadCiudad(CIUDAD_UUID)
        awaitMapLoad()
        assertEquals("Error al obtener datos de la base de datos", viewModel.uiState.errorMessage)
    }

    @Test
    fun loadCiudad_excepcion_muestraMensaje() = runTest(testDispatcher) {
        val mapRepository = MapRepositoryContract { throw IllegalStateException("fallo red") }
        viewModel = MapViewModel(
            mapRepository,
            RepositoryMockHelpers.reporteRepositoryReturning(emptyList()),
            RepositoryMockHelpers.ubicacionRepositoryReturning(emptyList()),
            authRepository,
            RepositoryMockHelpers.barrioRepository(),
            NoOpRiskZoneDetector
        )

        viewModel.loadCiudad(CIUDAD_UUID)
        awaitMapLoad()
        assertEquals("Error cargando ciudad: fallo red", viewModel.uiState.errorMessage)
    }

    @Test
    fun loadCiudad_omiteReporteSinUbicacion() = runTest(testDispatcher) {
        val ciudad = MapViewModelTestFixtures.sampleCiudad()
        val reporteConUbicacion = MapViewModelTestFixtures.sampleReporte(id = "r1", ubicacionId = "u1")
        val reporteSinUbicacion = MapViewModelTestFixtures.sampleReporte(
            id = "r2",
            ubicacionId = "u-inexistente"
        )
        val mapRepository = MapRepositoryContract { ciudad }
        viewModel = MapViewModel(
            mapRepository,
            RepositoryMockHelpers.reporteRepositoryReturning(listOf(reporteConUbicacion, reporteSinUbicacion)),
            RepositoryMockHelpers.ubicacionRepositoryReturning(
                listOf(MapViewModelTestFixtures.sampleUbicacion(id = "u1"))
            ),
            authRepository,
            RepositoryMockHelpers.barrioRepository(),
            NoOpRiskZoneDetector
        )

        viewModel.loadCiudad(CIUDAD_UUID)
        awaitMapLoad()
        assertEquals(2, viewModel.uiState.reports.size)
        assertEquals(1, viewModel.uiState.reportMarkers.size)
    }

    @Test
    fun loadCiudad_filtraReportesPorCiudad() = runTest(testDispatcher) {
        val ciudad = MapViewModelTestFixtures.sampleCiudad()
        val otroCiudadId = "87654321-4321-4321-4321-210987654321"
        val reporteCiudad = MapViewModelTestFixtures.sampleReporte(id = "r1", ciudadId = CIUDAD_UUID)
        val reporteOtraCiudad = MapViewModelTestFixtures.sampleReporte(id = "r2", ciudadId = otroCiudadId)
        val mapRepository = MapRepositoryContract { ciudad }
        viewModel = MapViewModel(
            mapRepository,
            RepositoryMockHelpers.reporteRepositoryReturning(listOf(reporteCiudad, reporteOtraCiudad)),
            RepositoryMockHelpers.ubicacionRepositoryReturning(emptyList()),
            authRepository,
            RepositoryMockHelpers.barrioRepository(),
            NoOpRiskZoneDetector
        )

        viewModel.loadCiudad(CIUDAD_UUID)
        awaitMapLoad()
        assertEquals(1, viewModel.uiState.reports.size)
        assertEquals("r1", viewModel.uiState.reports.first().id)
    }

    @Test
    fun onReportClicked_conReporteCargado() = runTest(testDispatcher) {
        val ciudad = MapViewModelTestFixtures.sampleCiudad()
        val reporte = MapViewModelTestFixtures.sampleReporte()
        val ubicacion = MapViewModelTestFixtures.sampleUbicacion()
        val mapRepository = MapRepositoryContract { ciudad }
        viewModel = MapViewModel(
            mapRepository,
            RepositoryMockHelpers.reporteRepositoryReturning(listOf(reporte)),
            RepositoryMockHelpers.ubicacionRepositoryReturning(listOf(ubicacion)),
            authRepository,
            RepositoryMockHelpers.barrioRepository(),
            NoOpRiskZoneDetector
        )

        viewModel.loadCiudad(CIUDAD_UUID)
        awaitMapLoad()
        viewModel.onReportClicked("reporte-1")

        assertNotNull(viewModel.uiState.selectedReport)
        assertEquals("reporte-1", viewModel.uiState.selectedReport?.id)
    }

    @Test
    fun loadCiudad_activaLoadingAlIniciar() {
        val mapRepository = MapRepositoryContract { MapViewModelTestFixtures.sampleCiudad() }
        viewModel = MapViewModel(
            mapRepository,
            RepositoryMockHelpers.reporteRepositoryReturning(emptyList()),
            RepositoryMockHelpers.ubicacionRepositoryReturning(emptyList()),
            authRepository,
            RepositoryMockHelpers.barrioRepository(),
            NoOpRiskZoneDetector
        )

        viewModel.loadCiudad(CIUDAD_UUID)

        assertTrue(viewModel.uiState.isLoading)
        assertNull(viewModel.uiState.errorMessage)
    }
}
