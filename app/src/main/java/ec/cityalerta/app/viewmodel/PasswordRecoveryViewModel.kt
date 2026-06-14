package ec.cityalerta.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    private val _uiState = MutableStateFlow(PasswordRecoveryState())
    val uiState: StateFlow<PasswordRecoveryState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                isEmailVerified = false,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun onNewPasswordChange(password: String) {
        _uiState.update { it.copy(newPassword = password) }
    }

    fun onConfirmPasswordChange(password: String) {
        _uiState.update { it.copy(confirmPassword = password) }
    }

    fun refreshSessionState() {
        // No-op: el reset ya no depende de una sesión ni de un enlace de correo.
    }

    fun verifyEmail() {
        val currentState = _uiState.value
        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingresa tu correo electronico") }
            return
        }

        if (currentState.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            try {
                repository.verifyRecoveryEmail(currentState.email).fold(
                    onSuccess = { exists ->
                        _uiState.update {
                            it.copy(
                                isEmailVerified = exists,
                                successMessage = if (exists) {
                                    "Correo verificado. Ya puedes escribir la nueva contrasena."
                                } else {
                                    null
                                },
                                errorMessage = if (exists) null else "No encontramos una cuenta con ese correo. Revisa que este bien escrito."
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isEmailVerified = false,
                                errorMessage = error.message ?: RECOVERY_ERROR_MESSAGE
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(isEmailVerified = false, errorMessage = e.message ?: RECOVERY_ERROR_MESSAGE) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetPassword(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingresa tu correo electronico") }
            return
        }

        if (!currentState.isEmailVerified) {
            _uiState.update { it.copy(errorMessage = "Primero verifica que el correo exista") }
            return
        }

        if (currentState.isLoading) return
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null, successMessage = null) }

            if (currentState.newPassword.isBlank() || currentState.confirmPassword.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Completa la nueva contrasena", isLoading = false) }
                return@launch
            }

            if (currentState.newPassword.length < 8) {
                _uiState.update { it.copy(errorMessage = "La contrasena debe tener al menos 8 caracteres", isLoading = false) }
                return@launch
            }

            if (currentState.newPassword != currentState.confirmPassword) {
                _uiState.update { it.copy(errorMessage = "Las contrasenas no coinciden", isLoading = false) }
                return@launch
            }

            try {
                repository.resetPasswordByEmail(currentState.email, currentState.newPassword).fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                successMessage = "Contrasena actualizada correctamente",
                                newPassword = "",
                                confirmPassword = "",
                                isEmailVerified = false
                            )
                        }
                        onSuccess()
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(errorMessage = error.message ?: RECOVERY_ERROR_MESSAGE, isLoading = false) }
                    }
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(errorMessage = e.message ?: RECOVERY_ERROR_MESSAGE, isLoading = false) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updatePassword(onSuccess: () -> Unit) {
        resetPassword(onSuccess)
    }
}
