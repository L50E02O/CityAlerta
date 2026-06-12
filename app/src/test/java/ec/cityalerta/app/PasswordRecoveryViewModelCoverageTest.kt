package ec.cityalerta.app

import ec.cityalerta.app.testdoubles.FakeAuthRepository
import ec.cityalerta.app.viewmodel.PasswordRecoveryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests de cobertura para flujos asincronos de PasswordRecoveryViewModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PasswordRecoveryViewModelCoverageTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var viewModel: PasswordRecoveryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        viewModel = PasswordRecoveryViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun verifyEmail_exito_correoExiste() = runTest {
        authRepository.verifyRecoveryEmailResult = Result.success(true)
        viewModel.onEmailChange("usuario@example.com")

        viewModel.verifyEmail()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEmailVerified)
        assertEquals("Correo verificado. Ya puedes escribir la nueva contrasena.", viewModel.uiState.value.successMessage)
        assertNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("usuario@example.com", authRepository.lastVerifyEmail)
    }

    @Test
    fun verifyEmail_exito_correoNoExiste() = runTest {
        authRepository.verifyRecoveryEmailResult = Result.success(false)
        viewModel.onEmailChange("desconocido@example.com")

        viewModel.verifyEmail()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isEmailVerified)
        assertNull(viewModel.uiState.value.successMessage)
        assertEquals(
            "No encontramos una cuenta con ese correo. Revisa que este bien escrito.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun verifyEmail_falloRepositorio() = runTest {
        authRepository.verifyRecoveryEmailResult = Result.failure(RuntimeException("Servicio caido"))
        viewModel.onEmailChange("usuario@example.com")

        viewModel.verifyEmail()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isEmailVerified)
        assertEquals("Servicio caido", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun verifyEmail_falloSinMensaje_usaMensajePorDefecto() = runTest {
        authRepository.verifyRecoveryEmailResult = Result.failure(RuntimeException())
        viewModel.onEmailChange("usuario@example.com")

        viewModel.verifyEmail()
        advanceUntilIdle()

        assertEquals("Error desconocido", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun verifyEmail_noEjecutaSiYaEstaCargando() = runTest {
        authRepository.verifyRecoveryEmailResult = Result.success(true)
        viewModel.onEmailChange("usuario@example.com")
        viewModel.verifyEmail()
        viewModel.verifyEmail()
        advanceUntilIdle()

        assertEquals(1, authRepository.verifyRecoveryEmailCalls)
    }

    @Test
    fun resetPassword_contrasenasVacias() = runTest {
        prepararCorreoVerificado()

        viewModel.resetPassword()
        advanceUntilIdle()

        assertEquals("Completa la nueva contrasena", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun resetPassword_contrasenaCorta() = runTest {
        prepararCorreoVerificado()
        viewModel.onNewPasswordChange("abc")
        viewModel.onConfirmPasswordChange("abc")

        viewModel.resetPassword()
        advanceUntilIdle()

        assertEquals("La contrasena debe tener al menos 8 caracteres", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun resetPassword_contrasenasNoCoinciden() = runTest {
        prepararCorreoVerificado()
        viewModel.onNewPasswordChange("Password123")
        viewModel.onConfirmPasswordChange("Password456")

        viewModel.resetPassword()
        advanceUntilIdle()

        assertEquals("Las contrasenas no coinciden", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun resetPassword_exito_invocaCallback() = runTest {
        prepararCorreoVerificado()
        viewModel.onNewPasswordChange("NuevaPass123")
        viewModel.onConfirmPasswordChange("NuevaPass123")
        var callbackInvocado = false

        viewModel.resetPassword()
        advanceUntilIdle()

        assertTrue(callbackInvocado)
        assertEquals("Contrasena actualizada correctamente", viewModel.uiState.value.successMessage)
        assertEquals("", viewModel.uiState.value.newPassword)
        assertEquals("", viewModel.uiState.value.confirmPassword)
        assertFalse(viewModel.uiState.value.isEmailVerified)
        assertEquals("usuario@example.com", authRepository.lastResetEmail)
        assertEquals("NuevaPass123", authRepository.lastResetPassword)
    }

    @Test
    fun resetPassword_falloRepositorio() = runTest {
        prepararCorreoVerificado()
        viewModel.onNewPasswordChange("NuevaPass123")
        viewModel.onConfirmPasswordChange("NuevaPass123")
        authRepository.resetPasswordByEmailResult = Result.failure(RuntimeException("No autorizado"))

        viewModel.resetPassword()
        advanceUntilIdle()

        assertEquals("No autorizado", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun resetPassword_falloSinMensaje_usaMensajePorDefecto() = runTest {
        prepararCorreoVerificado()
        viewModel.onNewPasswordChange("NuevaPass123")
        viewModel.onConfirmPasswordChange("NuevaPass123")
        authRepository.resetPasswordByEmailResult = Result.failure(RuntimeException())

        viewModel.resetPassword()
        advanceUntilIdle()

        assertEquals("Error desconocido", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun resetPassword_noEjecutaSiYaEstaCargando() = runTest {
        prepararCorreoVerificado()
        viewModel.onNewPasswordChange("NuevaPass123")
        viewModel.onConfirmPasswordChange("NuevaPass123")
        authRepository.resetPasswordByEmailResult = Result.success(Unit)

        viewModel.resetPassword()
        viewModel.resetPassword()
        advanceUntilIdle()

        assertEquals("NuevaPass123", authRepository.lastResetPassword)
    }

    @Test
    fun updatePassword_delegaEnResetPassword() = runTest {
        prepararCorreoVerificado()
        viewModel.onNewPasswordChange("NuevaPass123")
        viewModel.onConfirmPasswordChange("NuevaPass123")
        var callbackInvocado = false

        viewModel.updatePassword { callbackInvocado = true }
        advanceUntilIdle()

        assertTrue(callbackInvocado)
        assertEquals("Contrasena actualizada correctamente", viewModel.uiState.value.successMessage)
    }

    private fun prepararCorreoVerificado() {
        authRepository.verifyRecoveryEmailResult = Result.success(true)
        viewModel.onEmailChange("usuario@example.com")
        viewModel.verifyEmail()
        testDispatcher.scheduler.advanceUntilIdle()
    }
}
