package ec.cityalerta.app.viewmodel

import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.data.contracts.geocoding.GeocodingRepositoryContract
import ec.cityalerta.app.model.data.contracts.location.LocationProviderContract
import ec.cityalerta.app.model.data.contracts.map.MapRepositoryContract
import ec.cityalerta.app.model.data.contracts.moderation.ImageModerationContract
import ec.cityalerta.app.model.data.location.UserLocation
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.repository.BarrioRepository
import ec.cityalerta.app.model.repository.ReporteImagenRepository
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteStorageRepository
import ec.cityalerta.app.model.repository.ReporteUbicacionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ReporteViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: ReporteViewModel
    private lateinit var reporteRepository: ReporteRepository
    private lateinit var reporteImagenRepository: ReporteImagenRepository
    private lateinit var reporteUbicacionRepository: ReporteUbicacionRepository
    private lateinit var reporteStorageRepository: ReporteStorageRepository
    private lateinit var locationProvider: LocationProviderContract
    private lateinit var authRepository: AuthRepositoryContract
    private lateinit var mapRepository: MapRepositoryContract
    private lateinit var barrioRepository: BarrioRepository
    private lateinit var geocodingRepository: GeocodingRepositoryContract
    private lateinit var imageModerationRepository: ImageModerationContract

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        reporteRepository = mock()
        reporteImagenRepository = mock()
        reporteUbicacionRepository = mock()
        reporteStorageRepository = mock()
        locationProvider = mock()
        authRepository = mock()
        mapRepository = mock()
        barrioRepository = mock()
        geocodingRepository = mock()
        imageModerationRepository = mock()

        viewModel = ReporteViewModel(
            reporteRepository,
            reporteImagenRepository,
            reporteUbicacionRepository,
            reporteStorageRepository,
            locationProvider,
            authRepository,
            mapRepository,
            barrioRepository,
            geocodingRepository,
            imageModerationRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty and not submitting`() {
        val state = viewModel.uiState.value
        assertEquals("", state.descripcion)
        assertEquals(null, state.categoria)
        assertFalse(state.isSubmitting)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `sendReport fails when description is missing`() = runTest {
        whenever(authRepository.getUserId()).thenReturn(Result.success("user-id"))
        whenever(authRepository.getCiudadId()).thenReturn(Result.success("city-id"))

        // Set valid category, image, location, but leave description empty
        viewModel.onCategoriaChange(ReportType.BACHE)
        viewModel.setImagenData(byteArrayOf(1, 2, 3))
        viewModel.setImagen("url")
        viewModel.setUbicacion(-0.95, -80.7)
        viewModel.onDescriptionChange("") // Blank description

        viewModel.sendReport()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Falta descripción", state.errorMessage)
        assertFalse(state.isSubmitting)
    }

    @Test
    fun `sendReport fails when category is missing`() = runTest {
        whenever(authRepository.getUserId()).thenReturn(Result.success("user-id"))
        whenever(authRepository.getCiudadId()).thenReturn(Result.success("city-id"))

        viewModel.onDescriptionChange("Robo en la calle")
        viewModel.setImagenData(byteArrayOf(1, 2, 3))
        viewModel.setImagen("url")
        viewModel.setUbicacion(-0.95, -80.7)
        // No category set

        viewModel.sendReport()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Categoría no seleccionada", state.errorMessage)
        assertFalse(state.isSubmitting)
    }

    @Test
    fun `sendReport fails when image is missing`() = runTest {
        whenever(authRepository.getUserId()).thenReturn(Result.success("user-id"))
        whenever(authRepository.getCiudadId()).thenReturn(Result.success("city-id"))

        viewModel.onDescriptionChange("Robo en la calle")
        viewModel.onCategoriaChange(ReportType.LUZ)
        viewModel.setUbicacion(-0.95, -80.7)
        // No image set

        viewModel.sendReport()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Falta imagen", state.errorMessage)
        assertFalse(state.isSubmitting)
    }
}
