package ec.cityalerta.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class PasswordRecoveryState(
    val email: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isEmailVerified: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

private const val RECOVERY_ERROR_MESSAGE = "Error desconocido"

class PasswordRecoveryViewModel(
    private val repository: AuthRepositoryContract
) : ViewModel() {

    var uiState by mutableStateOf(PasswordRecoveryState())
        private set

    fun onEmailChange(email: String) {
        uiState = uiState.copy(
            email = email,
            isEmailVerified = false,
            successMessage = null,
            errorMessage = null
        )
    }

    fun onNewPasswordChange(password: String) {
        uiState = uiState.copy(newPassword = password)
    }

    fun onConfirmPasswordChange(password: String) {
        uiState = uiState.copy(confirmPassword = password)
    }

    fun refreshSessionState() {
        // No-op: el reset ya no depende de una sesión ni de un enlace de correo.
    }

    fun verifyEmail() {
        if (uiState.email.isBlank()) {
            uiState = uiState.copy(errorMessage = "Ingresa tu correo electronico")
            return
        }

        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true, errorMessage = null, successMessage = null)

        viewModelScope.launch {
            try {
                repository.verifyRecoveryEmail(uiState.email).fold(
                    onSuccess = { exists ->
                        uiState = uiState.copy(
                            isEmailVerified = exists,
                            successMessage = if (exists) {
                                "Correo verificado. Ya puedes escribir la nueva contrasena."
                            } else {
                                null
                            },
                            errorMessage = if (exists) null else "No encontramos una cuenta con ese correo. Revisa que este bien escrito."
                        )
                    },
                    onFailure = { error ->
                        uiState = uiState.copy(
                            isEmailVerified = false,
                            errorMessage = error.message ?: RECOVERY_ERROR_MESSAGE
                        )
                    }
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                uiState = uiState.copy(isEmailVerified = false, errorMessage = e.message ?: RECOVERY_ERROR_MESSAGE)
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun resetPassword(onSuccess: () -> Unit) {
        if (uiState.email.isBlank()) {
            uiState = uiState.copy(errorMessage = "Ingresa tu correo electronico")
            return
        }

        if (!uiState.isEmailVerified) {
            uiState = uiState.copy(errorMessage = "Primero verifica que el correo exista")
            return
        }

        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true)

        viewModelScope.launch {
            uiState = uiState.copy(errorMessage = null, successMessage = null)

            if (uiState.newPassword.isBlank() || uiState.confirmPassword.isBlank()) {
                uiState = uiState.copy(errorMessage = "Completa la nueva contrasena")
                uiState = uiState.copy(isLoading = false)
                return@launch
            }

            if (uiState.newPassword.length < 8) {
                uiState = uiState.copy(errorMessage = "La contrasena debe tener al menos 8 caracteres")
                uiState = uiState.copy(isLoading = false)
                return@launch
            }

            if (uiState.newPassword != uiState.confirmPassword) {
                uiState = uiState.copy(errorMessage = "Las contrasenas no coinciden")
                uiState = uiState.copy(isLoading = false)
                return@launch
            }

            try {
                repository.resetPasswordByEmail(uiState.email, uiState.newPassword).fold(
                    onSuccess = {
                        uiState = uiState.copy(
                            successMessage = "Contrasena actualizada correctamente",
                            newPassword = "",
                            confirmPassword = "",
                            isEmailVerified = false
                        )
                        onSuccess()
                    },
                    onFailure = { error ->
                        uiState = uiState.copy(errorMessage = error.message ?: RECOVERY_ERROR_MESSAGE)
                    }
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                uiState = uiState.copy(errorMessage = e.message ?: RECOVERY_ERROR_MESSAGE)
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun updatePassword(onSuccess: () -> Unit) {
        resetPassword(onSuccess)
    }
}
