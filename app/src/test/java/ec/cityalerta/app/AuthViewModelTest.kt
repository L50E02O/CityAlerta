package ec.cityalerta.app

import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.remote.service.PushSubscriptionRegistrar
import ec.cityalerta.app.util.MainDispatcherRule
import ec.cityalerta.app.viewmodel.AuthState
import ec.cityalerta.app.viewmodel.AuthViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests unitarios para AuthViewModel.
 * Valida cambios de estado y validaciones de formulario.
 */
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var mockAuthRepository: AuthRepositoryContract

    @Mock
    private lateinit var mockPushRegistrar: PushSubscriptionRegistrar

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        whenever(mockPushRegistrar.isNotificationsEnabled()).thenReturn(false)
        viewModel = AuthViewModel(mockAuthRepository, mockPushRegistrar)
    }

    @Test
    fun testInitialAuthState() {
        // Arrange & Act
        val state = viewModel.uiState

        // Assert
        assertNotNull(state)
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.ciudadNombre)
        assertEquals("", state.ciudadId)
        assertFalse(state.isLoading)
        assertFalse(state.isEmailUnconfirmed)
        assertFalse(state.notificationsEnabled)
        assertEquals(null, state.errorMessage)
        assertEquals(null, state.infoMessage)
    }

    @Test
    fun testOnNotificationsPermissionGranted() {
        // Act
        viewModel.onNotificationsPermissionGranted()

        // Assert
        assertTrue(viewModel.uiState.notificationsEnabled)
        verify(mockPushRegistrar).setNotificationsEnabled(true)
    }

    @Test
    fun testOnNotificationsPermissionDenied() {
        // Act
        viewModel.onNotificationsPermissionDenied()

        // Assert
        assertFalse(viewModel.uiState.notificationsEnabled)
        verify(mockPushRegistrar).setNotificationsEnabled(false)
    }

    @Test
    fun testOnNotificationsDisabledByUser() {
        // Act
        viewModel.onNotificationsDisabledByUser()

        // Assert
        assertFalse(viewModel.uiState.notificationsEnabled)
        verify(mockPushRegistrar).setNotificationsEnabled(false)
    }

    @Test
    fun testAuthStateCreation() {
        // Arrange & Act
        val state = AuthState(
            email = "test@example.com",
            password = "password123",
            ciudadNombre = "Manta",
            ciudadId = "ciudad-123",
            isLoading = false,
            isEmailUnconfirmed = false,
            errorMessage = null,
            infoMessage = null
        )

        // Assert
        assertEquals("test@example.com", state.email)
        assertEquals("password123", state.password)
        assertEquals("Manta", state.ciudadNombre)
        assertEquals("ciudad-123", state.ciudadId)
        assertFalse(state.isLoading)
    }

    @Test
    fun testOnEmailChange() {
        // Arrange
        val newEmail = "newemail@example.com"

        // Act
        viewModel.onEmailChange(newEmail)

        // Assert
        assertEquals(newEmail, viewModel.uiState.email)
        assertFalse(viewModel.uiState.isEmailUnconfirmed)
        assertEquals(null, viewModel.uiState.errorMessage)
    }

    @Test
    fun testOnPasswordChange() {
        // Arrange
        val newPassword = "newPassword123"

        // Act
        viewModel.onPasswordChange(newPassword)

        // Assert
        assertEquals(newPassword, viewModel.uiState.password)
    }

    @Test
    fun testOnCiudadChange() {
        // Arrange
        val ciudadName = "Quito"

        // Act
        viewModel.onCiudadChange(ciudadName)

        // Assert
        assertEquals(ciudadName, viewModel.uiState.ciudadNombre)
        assertEquals("", viewModel.uiState.ciudadId)
    }

    @Test
    fun testOnCiudadSelected() {
        // Arrange
        val ciudadName = "Guayaquil"
        val ciudadId = "ciudad-456"

        // Act
        viewModel.onCiudadSelected(ciudadName, ciudadId)

        // Assert
        assertEquals(ciudadName, viewModel.uiState.ciudadNombre)
        assertEquals(ciudadId, viewModel.uiState.ciudadId)
    }

    @Test
    fun testSetAuthInfoMessage() {
        // Arrange
        val message = "Registro exitoso"

        // Act
        viewModel.setAuthInfoMessage(message)

        // Assert
        assertEquals(message, viewModel.uiState.infoMessage)
        assertEquals(null, viewModel.uiState.errorMessage)
        assertFalse(viewModel.uiState.isEmailUnconfirmed)
    }

    @Test
    fun testClearInfoMessage() {
        // Arrange
        viewModel.setAuthInfoMessage("Mensaje temporal")

        // Act
        viewModel.clearInfoMessage()

        // Assert
        assertEquals(null, viewModel.uiState.infoMessage)
    }

    @Test
    fun testOnLoginClickEmptyEmail() {
        // Arrange
        viewModel.onPasswordChange("password123")
        var successCalled = false

        // Act
        viewModel.onLoginClick { successCalled = true }

        // Assert
        assertFalse(successCalled)
        assertEquals(
            "El correo y la contrasena no pueden estar vacios",
            viewModel.uiState.errorMessage
        )
    }

    @Test
    fun testOnLoginClickEmptyPassword() {
        // Arrange
        viewModel.onEmailChange("test@example.com")
        var successCalled = false

        // Act
        viewModel.onLoginClick { successCalled = true }

        // Assert
        assertFalse(successCalled)
        assertEquals(
            "El correo y la contrasena no pueden estar vacios",
            viewModel.uiState.errorMessage
        )
    }

    @Test
    fun testOnRegisterClickEmptyEmail() {
        // Arrange
        viewModel.onPasswordChange("password123")
        viewModel.onCiudadSelected("Manta", "ciudad-123")
        var successCalled = false

        // Act
        viewModel.onRegisterClick { successCalled = true }

        // Assert
        assertFalse(successCalled)
        assertNotNull(viewModel.uiState.errorMessage)
    }

    @Test
    fun testOnRegisterClickEmptyPassword() {
        // Arrange
        viewModel.onEmailChange("test@example.com")
        viewModel.onCiudadSelected("Manta", "ciudad-123")
        var successCalled = false

        // Act
        viewModel.onRegisterClick { successCalled = true }

        // Assert
        assertFalse(successCalled)
        assertNotNull(viewModel.uiState.errorMessage)
    }

    @Test
    fun testOnRegisterClickEmptyCiudad() {
        // Arrange
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")
        var successCalled = false

        // Act
        viewModel.onRegisterClick { successCalled = true }

        // Assert
        assertFalse(successCalled)
        assertEquals("Selecciona tu ciudad", viewModel.uiState.errorMessage)
    }

    @Test
    fun testAuthStateWithAllFields() {
        // Arrange
        val email = "usuario@example.com"
        val password = "SecurePass123"
        val ciudadNombre = "Manta"
        val ciudadId = "manta-123"

        // Act
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        viewModel.onCiudadSelected(ciudadNombre, ciudadId)

        // Assert
        val state = viewModel.uiState
        assertEquals(email, state.email)
        assertEquals(password, state.password)
        assertEquals(ciudadNombre, state.ciudadNombre)
        assertEquals(ciudadId, state.ciudadId)
    }

    @Test
    fun testResendActivationEmailEmptyEmail() {
        // Arrange
        var successCalled = false

        // Act
        viewModel.resendActivationEmail()

        // Assert
        assertEquals(
            "Ingresa tu correo para reenviar la activacion",
            viewModel.uiState.errorMessage
        )
    }

    @Test
    fun testAuthStateMultipleTransitions() {
        // Arrange & Act
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password1")
        viewModel.onCiudadSelected("Manta", "ciudad-1")

        // Assert
        assertEquals("test@example.com", viewModel.uiState.email)
        assertEquals("password1", viewModel.uiState.password)

        // Act - Update again
        viewModel.onEmailChange("new@example.com")
        viewModel.onPasswordChange("password2")

        // Assert
        assertEquals("new@example.com", viewModel.uiState.email)
        assertEquals("password2", viewModel.uiState.password)
        // Ciudad should remain
        assertEquals("ciudad-1", viewModel.uiState.ciudadId)
    }

    @Test
    fun testAuthStateErrorMessageClear() {
        // Arrange
        viewModel.setAuthInfoMessage("Test message")

        // Act
        viewModel.onEmailChange("test@example.com")

        // Assert
        assertEquals(null, viewModel.uiState.errorMessage)
    }

    @Test
    fun testAuthStateWithSpecialCharacters() {
        // Arrange
        val specialEmail = "user+test@example.co.uk"
        val specialPassword = "P@ssw0rd!#$%"

        // Act
        viewModel.onEmailChange(specialEmail)
        viewModel.onPasswordChange(specialPassword)

        // Assert
        assertEquals(specialEmail, viewModel.uiState.email)
        assertEquals(specialPassword, viewModel.uiState.password)
    }

    @Test
    fun testAuthStateCiudadResetOnChange() {
        // Arrange
        viewModel.onCiudadSelected("Manta", "ciudad-1")

        // Act
        viewModel.onCiudadChange("Quito")

        // Assert
        assertEquals("Quito", viewModel.uiState.ciudadNombre)
        assertEquals("", viewModel.uiState.ciudadId)
    }
}
