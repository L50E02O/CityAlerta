package ec.cityalerta.app

import ec.cityalerta.app.model.data.local.UserSessionEntity
import ec.cityalerta.app.testdoubles.FakeAuthRepository
import ec.cityalerta.app.testdoubles.FakeSessionRepository
import ec.cityalerta.app.util.MainDispatcherRule
import ec.cityalerta.app.viewmodel.AuthUiEvent
import ec.cityalerta.app.viewmodel.AuthViewModel
import ec.cityalerta.app.viewmodel.RegisterPhase
import ec.cityalerta.app.viewmodel.SplashUiEvent
import ec.cityalerta.app.viewmodel.SplashViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertIs
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        fakeAuthRepository = FakeAuthRepository()
        viewModel = AuthViewModel(fakeAuthRepository)
    }

    @Test
    fun `uiState initial state is correct`() = runTest {
        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertFalse(state.isLoading)
    }

    @Test
    fun `login success emits NavigateToHome event`() = runTest {
        fakeAuthRepository.shouldSucceed = true
        val events = mutableListOf<AuthUiEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.onEmailChange("test@test.com")
        viewModel.onPasswordChange("password123")
        viewModel.onLoginClick()
        advanceUntilIdle()

        assertTrue(events.any { it is AuthUiEvent.NavigateToHome })
        job.cancel()
    }

    @Test
    fun `login failure shows error message`() = runTest {
        fakeAuthRepository.shouldSucceed = false
        fakeAuthRepository.signInResult = Result.failure(Exception("Credenciales incorrectas"))

        viewModel.onEmailChange("test@test.com")
        viewModel.onPasswordChange("password123")
        viewModel.onLoginClick()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `register without session shows email verification pending`() = runTest {
        fakeAuthRepository.getUserIdResult = Result.failure(Exception("no session"))

        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.onCiudadSelected("Manta", "550e8400-e29b-41d4-a716-446655440000")
        viewModel.onRegisterClick()
        advanceUntilIdle()

        assertIs<RegisterPhase.EmailVerificationPending>(viewModel.uiState.value.registerPhase)
    }

    @Test
    fun `checkEmailVerified success emits NavigateToHome`() = runTest {
        fakeAuthRepository.checkEmailVerifiedResult = Result.success(true)
        val events = mutableListOf<AuthUiEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.checkEmailVerified()
        advanceUntilIdle()

        assertTrue(events.any { it is AuthUiEvent.NavigateToHome })
        job.cancel()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeSessionRepository: FakeSessionRepository
    private lateinit var viewModel: SplashViewModel

    @Before
    fun setup() {
        fakeAuthRepository = FakeAuthRepository()
        fakeSessionRepository = FakeSessionRepository()
        viewModel = SplashViewModel(fakeSessionRepository, fakeAuthRepository)
    }

    @Test
    fun `restoreSession with no stored session navigates to login`() = runTest {
        val events = mutableListOf<SplashUiEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.restoreSession()
        advanceUntilIdle()

        assertTrue(events.contains(SplashUiEvent.NavigateToLogin))
        job.cancel()
    }

    @Test
    fun `restoreSession with valid session navigates to home`() = runTest {
        fakeSessionRepository.setSession(
            UserSessionEntity(
                userId = "user-1",
                email = "test@test.com",
                roomId = "room-1",
                accessToken = "access",
                refreshToken = "refresh",
                expiresAt = System.currentTimeMillis() + 60_000
            )
        )
        fakeAuthRepository.restoreSessionResult = Result.success(Unit)

        val events = mutableListOf<SplashUiEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.restoreSession()
        advanceUntilIdle()

        assertTrue(events.contains(SplashUiEvent.NavigateToHome))
        job.cancel()
    }

    @Test
    fun `restoreSession failure clears session and navigates to login`() = runTest {
        fakeSessionRepository.setSession(
            UserSessionEntity(
                userId = "user-1",
                email = "test@test.com",
                roomId = "room-1",
                accessToken = "access",
                refreshToken = "refresh",
                expiresAt = System.currentTimeMillis() + 60_000
            )
        )
        fakeAuthRepository.restoreSessionResult = Result.failure(Exception("expired"))

        val events = mutableListOf<SplashUiEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.restoreSession()
        advanceUntilIdle()

        assertTrue(events.contains(SplashUiEvent.NavigateToLogin))
        assertEquals(1, fakeSessionRepository.clearCalls)
        job.cancel()
    }
}
