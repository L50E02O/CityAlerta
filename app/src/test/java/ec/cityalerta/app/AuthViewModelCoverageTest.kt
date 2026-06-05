package ec.cityalerta.app

import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.remote.service.PushSubscriptionAuthRequiredException
import ec.cityalerta.app.model.remote.service.PushSubscriptionDisabledException
import ec.cityalerta.app.model.remote.service.PushSubscriptionRegistrar
import ec.cityalerta.app.model.remote.service.PushSubscriptionTokenMissingException
import ec.cityalerta.app.model.repository.AuthMappedException
import ec.cityalerta.app.testdoubles.FakeAuthRepository
import ec.cityalerta.app.util.MainDispatcherRule
import ec.cityalerta.app.viewmodel.AuthViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelCoverageTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var mockPushRegistrar: PushSubscriptionRegistrar

    private lateinit var authRepository: FakeAuthRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        authRepository = FakeAuthRepository()
        whenever(mockPushRegistrar.isNotificationsEnabled()).thenReturn(true)
    }

    @Test
    fun init_conPushRegistrar_cargaEstadoNotificaciones() {
        whenever(mockPushRegistrar.isNotificationsEnabled()).thenReturn(true)

        val viewModel = AuthViewModel(authRepository, mockPushRegistrar)

        assertTrue(viewModel.uiState.notificationsEnabled)
    }

    @Test
    fun onRegisterClick_conSesion_registraPushYCompleta() = runTest {
        authRepository.getUserIdResult = Result.success("user-1")
        whenever(mockPushRegistrar.registerTokenIfAllowed()).thenReturn(Result.failure(PushSubscriptionDisabledException("off")))
        val viewModel = AuthViewModel(authRepository, mockPushRegistrar)
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.onCiudadSelected("Manta", "ciudad-1")

        var success = false
        viewModel.onRegisterClick { success = true }
        advanceUntilIdle()

        assertTrue(success)
        assertNull(viewModel.uiState.infoMessage)
        verify(mockPushRegistrar).registerTokenIfAllowed()
    }

    @Test
    fun onLoginClick_conPush_registraToken() = runTest {
        whenever(mockPushRegistrar.registerTokenIfAllowed()).thenReturn(Result.failure(PushSubscriptionTokenMissingException("sin token")))
        val viewModel = AuthViewModel(authRepository, mockPushRegistrar)
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("password123")

        viewModel.onLoginClick { }
        advanceUntilIdle()

        verify(mockPushRegistrar).registerTokenIfAllowed()
    }

    @Test
    fun onNotificationsPermissionGranted_registraPush() = runTest {
        whenever(mockPushRegistrar.registerTokenIfAllowed()).thenReturn(Result.failure(PushSubscriptionAuthRequiredException("sin auth")))
        val viewModel = AuthViewModel(authRepository, mockPushRegistrar)

        viewModel.onNotificationsPermissionGranted()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.notificationsEnabled)
        verify(mockPushRegistrar).setNotificationsEnabled(true)
        verify(mockPushRegistrar).registerTokenIfAllowed()
    }

    @Test
    fun onNotificationsDisabledByUser_desregistraToken() = runTest {
        whenever(mockPushRegistrar.unregisterToken()).thenReturn(Result.success(Unit))
        val viewModel = AuthViewModel(authRepository, mockPushRegistrar)

        viewModel.onNotificationsDisabledByUser()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.notificationsEnabled)
        verify(mockPushRegistrar).setNotificationsEnabled(false)
        verify(mockPushRegistrar).unregisterToken()
    }

    @Test
    fun onLoginClick_mientrasCarga_noRelanza() = runTest {
        authRepository.signInResult = Result.success(Unit)
        val viewModel = AuthViewModel(authRepository)
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.onLoginClick { }
        viewModel.onLoginClick { }

        advanceUntilIdle()

        assertFalse(viewModel.uiState.isLoading)
    }

    @Test
    fun onRegisterClick_mientrasCarga_noRelanza() = runTest {
        val viewModel = AuthViewModel(authRepository)
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.onCiudadSelected("Manta", "ciudad-1")
        viewModel.onRegisterClick { }
        viewModel.onRegisterClick { }

        advanceUntilIdle()

        assertFalse(viewModel.uiState.isLoading)
    }

    @Test
    fun resendActivationEmail_fallo_muestraError() = runTest {
        authRepository.resendSignupConfirmationResult = Result.failure(
            AuthMappedException("Correo no confirmado", true)
        )
        val viewModel = AuthViewModel(authRepository)
        viewModel.onEmailChange("user@example.com")

        viewModel.resendActivationEmail()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.errorMessage)
        assertTrue(viewModel.uiState.isEmailUnconfirmed)
    }

    @Test
    fun onLoginClick_excepcionInesperada_mapeaError() = runTest {
        val mockRepository = org.mockito.kotlin.mock<AuthRepositoryContract>()
        whenever(mockRepository.signIn(any(), any())).thenThrow(RuntimeException("red caida"))
        val viewModel = AuthViewModel(mockRepository)
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("password123")

        viewModel.onLoginClick { }
        advanceUntilIdle()

        assertEquals("red caida", viewModel.uiState.errorMessage)
    }

    @Test
    fun authViewModel_sinPushRegistrar_ignoraNotificaciones() = runTest {
        val viewModel = AuthViewModel(authRepository)

        viewModel.onNotificationsPermissionGranted()
        viewModel.onNotificationsDisabledByUser()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.notificationsEnabled)
    }
}
