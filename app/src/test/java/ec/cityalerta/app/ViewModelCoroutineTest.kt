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
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteUbicacionRepository
import ec.cityalerta.app.util.MainDispatcherRule
import ec.cityalerta.app.viewmodel.AuthViewModel
import ec.cityalerta.app.viewmodel.ExploreViewModel
import ec.cityalerta.app.viewmodel.MapViewModel
import ec.cityalerta.app.viewmodel.PasswordRecoveryViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    private val ciudadId = "550e8400-e29b-41d4-a716-446655440000"

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

        var success = false
        viewModel.onLoginClick { success = true }
        advanceUntilIdle()

        assertTrue(success)
        assertFalse(viewModel.uiState.isLoading)
        assertNull(viewModel.uiState.errorMessage)
    }

    @Test
    fun authViewModel_onLoginClick_failure() = runTest {
        whenever(authRepository.signIn(any(), any())).thenReturn(
            Result.failure(AuthMappedException("Correo o contrasena incorrectos", false))
        )

        val viewModel = AuthViewModel(authRepository)
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("password123")

        viewModel.onLoginClick { }
        advanceUntilIdle()

        assertEquals("Correo o contrasena incorrectos", viewModel.uiState.errorMessage)
        assertFalse(viewModel.uiState.isEmailUnconfirmed)
    }

    @Test
    fun authViewModel_onRegisterClick_withoutSession_showsInfo() = runTest {
        whenever(authRepository.signUp(any(), any(), any())).thenReturn(Result.success(Unit))
        whenever(authRepository.getUserId()).thenReturn(Result.failure(Exception("no session")))

        val viewModel = AuthViewModel(authRepository)
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.onCiudadSelected("Manta", ciudadId)

        var success = false
        viewModel.onRegisterClick { success = true }
        advanceUntilIdle()

        assertTrue(success)
        assertNotNull(viewModel.uiState.infoMessage)
        assertTrue(viewModel.uiState.infoMessage!!.contains("activacion"))
    }

    @Test
    fun authViewModel_resendActivationEmail_success() = runTest {
        whenever(authRepository.resendSignupConfirmation(any())).thenReturn(Result.success(Unit))

        val viewModel = AuthViewModel(authRepository)
        viewModel.onEmailChange("user@example.com")
        viewModel.resendActivationEmail()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.infoMessage)
        assertTrue(viewModel.uiState.infoMessage!!.contains("reenviamos"))
    }

    @Test
    fun passwordRecoveryViewModel_verifyEmail_success() = runTest {
        whenever(authRepository.verifyRecoveryEmail(any())).thenReturn(Result.success(true))

        val viewModel = PasswordRecoveryViewModel(authRepository)
        viewModel.onEmailChange("user@example.com")
        viewModel.verifyEmail()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.isEmailVerified)
        assertNotNull(viewModel.uiState.successMessage)
    }

    @Test
    fun passwordRecoveryViewModel_verifyEmail_notFound() = runTest {
        whenever(authRepository.verifyRecoveryEmail(any())).thenReturn(Result.success(false))

        val viewModel = PasswordRecoveryViewModel(authRepository)
        viewModel.onEmailChange("unknown@example.com")
        viewModel.verifyEmail()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.isEmailVerified)
        assertNotNull(viewModel.uiState.errorMessage)
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
        viewModel.resetPassword { success = true }
        advanceUntilIdle()

        assertTrue(success)
        assertEquals("Contrasena actualizada correctamente", viewModel.uiState.successMessage)
    }

    @Test
    fun mapViewModel_loadCiudad_success() = runTest {
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
            authRepository
        )
        viewModel.loadCiudad(ciudadId)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.ciudad)
        assertEquals(1, viewModel.uiState.reportMarkers.size)
        assertEquals(1, viewModel.uiState.reports.size)
        assertFalse(viewModel.uiState.isLoading)
    }

    @Test
    fun mapViewModel_loadCiudad_notFound() = runTest {
        whenever(mapRepository.getCiudadById(ciudadId)).thenReturn(null)

        val viewModel = MapViewModel(
            mapRepository,
            reporteRepository,
            ubicacionRepository,
            authRepository
        )
        viewModel.loadCiudad(ciudadId)
        advanceUntilIdle()

        assertNull(viewModel.uiState.ciudad)
        assertEquals("Ciudad no encontrada en el sistema", viewModel.uiState.errorMessage)
    }

    @Test
    fun mapViewModel_loadCiudad_resolvesNameWhenIdIsShort() = runTest {
        val ciudad = sampleCiudad()
        whenever(authRepository.buscarCiudadPorNombre("Manta")).thenReturn(Result.success(ciudadId))
        whenever(mapRepository.getCiudadById(ciudadId)).thenReturn(ciudad)
        whenever(reporteRepository.getAll()).thenReturn(Result.success(emptyList()))
        whenever(ubicacionRepository.getAll()).thenReturn(Result.success(emptyList()))

        val viewModel = MapViewModel(
            mapRepository,
            reporteRepository,
            ubicacionRepository,
            authRepository
        )
        viewModel.loadCiudad("Manta")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.ciudad)
        assertEquals("Manta", viewModel.uiState.ciudad?.nombre)
    }

    @Test
    fun exploreViewModel_initialState() {
        val viewModel = ExploreViewModel()
        assertEquals("Cargando...", viewModel.state.value.ciudadNombre)
    }
}
