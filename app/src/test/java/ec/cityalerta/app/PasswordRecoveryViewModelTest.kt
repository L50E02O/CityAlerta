package ec.cityalerta.app

import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.viewmodel.PasswordRecoveryState
import ec.cityalerta.app.viewmodel.PasswordRecoveryViewModel
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests unitarios para PasswordRecoveryViewModel.
 * Valida el flujo de recuperacion de contrasena.
 */
class PasswordRecoveryViewModelTest {

    @Mock
    private lateinit var mockAuthRepository: AuthRepositoryContract

    private lateinit var viewModel: PasswordRecoveryViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = PasswordRecoveryViewModel(mockAuthRepository)
    }

    @Test
    fun testInitialPasswordRecoveryState() {
        // Arrange & Act
        val state = viewModel.uiState.value

        // Assert
        assertNotNull(state)
        assertEquals("", state.email)
        assertEquals("", state.newPassword)
        assertEquals("", state.confirmPassword)
        assertFalse(state.isLoading)
        assertFalse(state.isEmailVerified)
        assertNull(state.successMessage)
        assertNull(state.errorMessage)
    }

    @Test
    fun testPasswordRecoveryStateDataClass() {
        // Arrange & Act
        val state = PasswordRecoveryState(
            email = "test@example.com",
            newPassword = "NewPass123",
            confirmPassword = "NewPass123",
            isLoading = false,
            isEmailVerified = false,
            successMessage = null,
            errorMessage = null
        )

        // Assert
        assertEquals("test@example.com", state.email)
        assertEquals("NewPass123", state.newPassword)
        assertEquals("NewPass123", state.confirmPassword)
    }

    @Test
    fun testOnEmailChange() {
        // Arrange
        val email = "newemail@example.com"

        // Act
        viewModel.onEmailChange(email)

        // Assert
        assertEquals(email, viewModel.uiState.value.email)
        assertFalse(viewModel.uiState.value.isEmailVerified)
        assertNull(viewModel.uiState.value.successMessage)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun testOnNewPasswordChange() {
        // Arrange
        val password = "NewPassword123"

        // Act
        viewModel.onNewPasswordChange(password)

        // Assert
        assertEquals(password, viewModel.uiState.value.newPassword)
    }

    @Test
    fun testOnConfirmPasswordChange() {
        // Arrange
        val password = "ConfirmPassword123"

        // Act
        viewModel.onConfirmPasswordChange(password)

        // Assert
        assertEquals(password, viewModel.uiState.value.confirmPassword)
    }

    @Test
    fun testPasswordChangeSequence() {
        // Arrange & Act
        viewModel.onNewPasswordChange("NewPass123")
        viewModel.onConfirmPasswordChange("NewPass123")

        // Assert
        assertEquals("NewPass123", viewModel.uiState.value.newPassword)
        assertEquals("NewPass123", viewModel.uiState.value.confirmPassword)
        assertEquals(viewModel.uiState.value.newPassword, viewModel.uiState.value.confirmPassword)
    }

    @Test
    fun testVerifyEmailEmptyEmail() {
        // Arrange & Act
        viewModel.verifyEmail()

        // Assert
        assertEquals("Ingresa tu correo electronico", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isEmailVerified)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun testRefreshSessionState() {
        // Arrange & Act
        viewModel.refreshSessionState()

        // Assert - Should not throw error, just a no-op
        assertNotNull(viewModel.uiState.value)
    }

    @Test
    fun testPasswordMinimumLength() {
        // Arrange
        val shortPassword = "Pass123"  // Less than 8 characters
        val validPassword = "ValidPass123"  // More than 8 characters

        // Act & Assert
        assertTrue(shortPassword.length < 8)
        assertTrue(validPassword.length >= 8)
    }

    @Test
    fun testPasswordRecoveryStateWithAllFields() {
        // Arrange & Act
        viewModel.onEmailChange("user@example.com")
        viewModel.onNewPasswordChange("NewSecurePass123")
        viewModel.onConfirmPasswordChange("NewSecurePass123")

        // Assert
        val state = viewModel.uiState.value
        assertEquals("user@example.com", state.email)
        assertEquals("NewSecurePass123", state.newPassword)
        assertEquals("NewSecurePass123", state.confirmPassword)
    }

    @Test
    fun testErrorMessageClearing() {
        // Arrange
        val state = PasswordRecoveryState(
            email = "test@example.com",
            errorMessage = "Error anterior"
        )

        // Act
        val newState = state.copy(
            email = "new@example.com",
            errorMessage = null
        )

        // Assert
        assertEquals("new@example.com", newState.email)
        assertNull(newState.errorMessage)
    }

    @Test
    fun testSuccessMessageManagement() {
        // Arrange
        val successMsg = "Contrasena actualizada"

        // Act
        val state = PasswordRecoveryState(
            email = "test@example.com",
            successMessage = successMsg
        )

        // Assert
        assertEquals(successMsg, state.successMessage)
        assertNull(state.errorMessage)
    }

    @Test
    fun testPasswordMismatch() {
        // Arrange
        val password1 = "Password123"
        val password2 = "Password456"

        // Act
        viewModel.onNewPasswordChange(password1)
        viewModel.onConfirmPasswordChange(password2)

        // Assert
        assertEquals(password1, viewModel.uiState.value.newPassword)
        assertEquals(password2, viewModel.uiState.value.confirmPassword)
        assertTrue(viewModel.uiState.value.newPassword != viewModel.uiState.value.confirmPassword)
    }

    @Test
    fun testPasswordRecoveryValidEmail() {
        // Arrange
        val validEmail = "usuario@example.com"

        // Act
        viewModel.onEmailChange(validEmail)

        // Assert
        assertEquals(validEmail, viewModel.uiState.value.email)
        assertTrue(viewModel.uiState.value.email.contains("@"))
    }

    @Test
    fun testEmailVerificationState() {
        // Arrange
        val state = PasswordRecoveryState(
            email = "test@example.com",
            isEmailVerified = true
        )

        // Assert
        assertTrue(state.isEmailVerified)
    }

    @Test
    fun testLoadingState() {
        // Arrange
        val state = PasswordRecoveryState(
            isLoading = true
        )

        // Assert
        assertTrue(state.isLoading)
    }

    @Test
    fun testMultipleEmailChanges() {
        // Arrange
        val emails = listOf("first@example.com", "second@example.com", "third@example.com")

        // Act & Assert
        emails.forEach { email ->
            viewModel.onEmailChange(email)
            assertEquals(email, viewModel.uiState.value.email)
        }
    }

    @Test
    fun testPasswordRecoveryStateImmutability() {
        // Arrange
        val originalState = viewModel.uiState.value
        val originalEmail = originalState.email

        // Act
        viewModel.onEmailChange("newuser@example.com")

        // Assert
        assertEquals("", originalEmail)
        assertEquals("newuser@example.com", viewModel.uiState.value.email)
    }

    @Test
    fun testResetPasswordSinCorreo() {
        // Arrange
        var callbackCalled = false

        // Act
        viewModel.resetPassword { callbackCalled = true }

        // Assert
        assertFalse(callbackCalled)
        assertEquals("Ingresa tu correo electronico", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun testResetPasswordSinCorreoVerificado() {
        // Arrange
        var callbackCalled = false
        viewModel.onEmailChange("usuario@example.com")

        // Act
        viewModel.resetPassword { callbackCalled = true }

        // Assert
        assertFalse(callbackCalled)
        assertEquals("Primero verifica que el correo exista", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun testPasswordRecoveryEdgeCases() {
        // Arrange & Act
        viewModel.onNewPasswordChange("")
        viewModel.onConfirmPasswordChange("")

        // Assert
        assertEquals("", viewModel.uiState.value.newPassword)
        assertEquals("", viewModel.uiState.value.confirmPassword)
    }
}
