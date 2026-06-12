package ec.cityalerta.app

import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.data.contracts.map.MapRepositoryContract
import ec.cityalerta.app.model.data.geoJson.Geometry
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacion
import ec.cityalerta.app.model.repository.AuthMappedException
import ec.cityalerta.app.model.repository.BarrioRepository
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteUbicacionRepository
import ec.cityalerta.app.testdoubles.FakeAuthRepository
import ec.cityalerta.app.testdoubles.NoOpRiskZoneDetector
import ec.cityalerta.app.util.MainDispatcherRule
import ec.cityalerta.app.viewmodel.AuthViewModel
import ec.cityalerta.app.viewmodel.ExploreViewModel
import ec.cityalerta.app.viewmodel.MapViewModel
import ec.cityalerta.app.viewmodel.PasswordRecoveryViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelCoroutineTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var authRepository: AuthRepositoryContract

    @Mock
    private lateinit var mapRepository: MapRepositoryContract

    @Mock
    private lateinit var reporteRepository: ReporteRepository

    @Mock
    private lateinit var ubicacionRepository: ReporteUbicacionRepository

    @Mock
    private lateinit var barrioRepository: BarrioRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    private val ciudadId = "550e8400-e29b-41d4-a716-446655440000"

    private suspend fun TestScope.awaitMapLoad(viewModel: MapViewModel) {
        withContext(Dispatchers.Default.limitedParallelism(1)) {
            withTimeout(5_000) {
                while (viewModel.uiState.value.isLoading) {
                    advanceUntilIdle()
                    mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
                    delay(20)
                }
            }
        }
    }

    private fun sampleCiudad() = Ciudad(
        id = ciudadId,
        nombre = "Manta",
        pais = "Ecuador",
        geojson = Geometry(
            type = "Polygon",
            coordinates = listOf(
                listOf(
                    listOf(-80.0, -1.0),
                    listOf(-80.0, 0.0),
                    listOf(-79.0, 0.0),
                    listOf(-80.0, -1.0)
                )
            )
        ),
        centroLat = -1.0,
        centroLng = -80.0
    )

    private fun sampleReporte(ubicacionId: String) = Reporte(
        id = "reporte-1",
        usuario_id = "user-1",
        ciudad_id = ciudadId,
        ubicacion_id = ubicacionId,
        descripcion = "Bache en la via",
        estado = ReporteEstado.PENDIENTE,
        fecha_reporte = "2024-01-01",
        categoria = ReportType.BACHE,
        barrio_id = "barrio-1"
    )

    @Test
    fun authViewModel_onLoginClick_success() = runTest {
        whenever(authRepository.signIn(any(), any())).thenReturn(Result.success(Unit))

        val viewModel = AuthViewModel(authRepository)
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("password123")

        val events = mutableListOf<AuthUiEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        viewModel.onLoginClick()
        advanceUntilIdle()
        job.cancel()

        assertTrue(events.any { it is AuthUiEvent.NavigateToHome })
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun authViewModel_onLoginClick_failure() = runTest {
        whenever(authRepository.signIn(any(), any())).thenReturn(
            Result.failure(AuthMappedException("Correo o contrasena incorrectos", false))
        )

        val viewModel = AuthViewModel(authRepository)
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("password123")

        viewModel.onLoginClick()
        advanceUntilIdle()

        assertEquals("Correo o contrasena incorrectos", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isEmailUnconfirmed)
    }

    @Test
    fun authViewModel_onRegisterClick_withoutSession_showsVerificationPending() = runTest {
        whenever(authRepository.signUp(any(), any(), any())).thenReturn(Result.success(Unit))
        whenever(authRepository.getUserId()).thenReturn(Result.failure(Exception("no session")))

        val viewModel = AuthViewModel(authRepository)
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.onCiudadSelected("Manta", ciudadId)

        viewModel.onRegisterClick()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.registerPhase is ec.cityalerta.app.viewmodel.RegisterPhase.EmailVerificationPending)
    }

    @Test
    fun authViewModel_resendActivationEmail_success() = runTest {
        whenever(authRepository.resendSignupConfirmation(any())).thenReturn(Result.success(Unit))

        val viewModel = AuthViewModel(authRepository)
        viewModel.onEmailChange("user@example.com")
        viewModel.resendActivationEmail()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.infoMessage)
        assertTrue(viewModel.uiState.value.infoMessage!!.contains("reenviamos"))
    }

    @Test
    fun passwordRecoveryViewModel_verifyEmail_success() = runTest {
        whenever(authRepository.verifyRecoveryEmail(any())).thenReturn(Result.success(true))

        val viewModel = PasswordRecoveryViewModel(authRepository)
        viewModel.onEmailChange("user@example.com")
        viewModel.verifyEmail()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEmailVerified)
        assertNotNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun passwordRecoveryViewModel_verifyEmail_notFound() = runTest {
        whenever(authRepository.verifyRecoveryEmail(any())).thenReturn(Result.success(false))

        val viewModel = PasswordRecoveryViewModel(authRepository)
        viewModel.onEmailChange("unknown@example.com")
        viewModel.verifyEmail()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isEmailVerified)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun passwordRecoveryViewModel_resetPassword_success() = runTest {
        whenever(authRepository.verifyRecoveryEmail(any())).thenReturn(Result.success(true))
        whenever(authRepository.resetPasswordByEmail(any(), any())).thenReturn(Result.success(Unit))

        val viewModel = PasswordRecoveryViewModel(authRepository)
        viewModel.onEmailChange("user@example.com")
        viewModel.verifyEmail()
        advanceUntilIdle()

        viewModel.onNewPasswordChange("NewPass123")
        viewModel.onConfirmPasswordChange("NewPass123")

        var success = false
        val job = launch {
            viewModel.events.collect { event ->
                if (event is ec.cityalerta.app.viewmodel.PasswordRecoveryUiEvent.NavigateToLogin) {
                    success = true
                }
            }
        }
        viewModel.resetPassword()
        advanceUntilIdle()
        job.cancel()

        assertTrue(success)
        assertEquals("Contrasena actualizada correctamente", viewModel.uiState.value.successMessage)
    }

    @Test
    fun mapViewModel_loadCiudad_success() = runTest(mainDispatcherRule.dispatcher) {
        val ciudad = sampleCiudad()
        val ubicacion = ReporteUbicacion(
            id = "loc-1",
            lat = -1.0,
            lng = -80.0,
            direccion_aproximada = "Calle principal"
        )
        val reporte = sampleReporte(ubicacion.id)

        whenever(mapRepository.getCiudadById(ciudadId)).thenReturn(ciudad)
        whenever(reporteRepository.getAll()).thenReturn(Result.success(listOf(reporte)))
        whenever(ubicacionRepository.getAll()).thenReturn(Result.success(listOf(ubicacion)))

        val viewModel = MapViewModel(
            mapRepository,
            reporteRepository,
            ubicacionRepository,
            authRepository,
            barrioRepository,
            NoOpRiskZoneDetector
        )
        viewModel.loadCiudad(ciudadId)
        awaitMapLoad(viewModel)

        assertNotNull(viewModel.uiState.value.ciudad)
        assertEquals(1, viewModel.uiState.value.reportMarkers.size)
        assertEquals(1, viewModel.uiState.value.reports.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun mapViewModel_loadCiudad_notFound() = runTest(mainDispatcherRule.dispatcher) {
        whenever(mapRepository.getCiudadById(ciudadId)).thenReturn(null)

        val viewModel = MapViewModel(
            mapRepository,
            reporteRepository,
            ubicacionRepository,
            authRepository,
            barrioRepository,
            NoOpRiskZoneDetector
        )
        viewModel.loadCiudad(ciudadId)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.ciudad)
        assertEquals("Ciudad no encontrada en el sistema", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun mapViewModel_loadCiudad_resolvesNameWhenIdIsShort() = runTest(mainDispatcherRule.dispatcher) {
        val ciudad = sampleCiudad()
        whenever(authRepository.buscarCiudadPorNombre("Manta")).thenReturn(Result.success(ciudadId))
        whenever(mapRepository.getCiudadById(ciudadId)).thenReturn(ciudad)
        whenever(reporteRepository.getAll()).thenReturn(Result.success(emptyList()))
        whenever(ubicacionRepository.getAll()).thenReturn(Result.success(emptyList()))

        val viewModel = MapViewModel(
            mapRepository,
            reporteRepository,
            ubicacionRepository,
            authRepository,
            barrioRepository,
            NoOpRiskZoneDetector
        )
        viewModel.loadCiudad("Manta")
        awaitMapLoad(viewModel)

        assertNotNull(viewModel.uiState.value.ciudad)
        assertEquals("Manta", viewModel.uiState.value.ciudad?.nombre)
    }

    @Test
    fun exploreViewModel_initialState() {
        val viewModel = ExploreViewModel(FakeAuthRepository())
        assertEquals("Cargando...", viewModel.state.value.ciudadNombre)
    }
}
