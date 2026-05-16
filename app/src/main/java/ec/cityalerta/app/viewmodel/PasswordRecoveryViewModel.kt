package ec.cityalerta.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ec.cityalerta.app.model.repository.interfaces.IAuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class PasswordRecoveryState(
    val email: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

private const val RECOVERY_ERROR_MESSAGE = "Error desconocido"

class PasswordRecoveryViewModel(
    private val repository: IAuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(PasswordRecoveryState())
        private set

    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email)
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

    fun resetPassword(onSuccess: () -> Unit) {
        if (uiState.email.isBlank()) {
            uiState = uiState.copy(errorMessage = "Ingresa tu correo electronico")
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
                            confirmPassword = ""
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
