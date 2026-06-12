package ec.cityalerta.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

sealed class PasswordRecoveryUiEvent {
    data object NavigateToLogin : PasswordRecoveryUiEvent()
}

private const val RECOVERY_ERROR_MESSAGE = "Error desconocido"

// No domain layer: recovery maps directly to auth repository calls.
class PasswordRecoveryViewModel(
    private val repository: AuthRepositoryContract
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordRecoveryState())
    val uiState: StateFlow<PasswordRecoveryState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PasswordRecoveryUiEvent>()
    val events: SharedFlow<PasswordRecoveryUiEvent> = _events.asSharedFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            isEmailVerified = false,
            successMessage = null,
            errorMessage = null
        )
    }

    fun onNewPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(newPassword = password)
    }

    fun onConfirmPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = password)
    }

    fun refreshSessionState() {
        // No-op: el reset ya no depende de una sesión ni de un enlace de correo.
    }

    fun verifyEmail() {
        val state = _uiState.value
        if (state.email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Ingresa tu correo electronico")
            return
        }

        if (state.isLoading) return
        _uiState.value = state.copy(isLoading = true, errorMessage = null, successMessage = null)

        viewModelScope.launch {
            try {
                repository.verifyRecoveryEmail(_uiState.value.email).fold(
                    onSuccess = { exists ->
                        _uiState.value = _uiState.value.copy(
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
                        _uiState.value = _uiState.value.copy(
                            isEmailVerified = false,
                            errorMessage = error.message ?: RECOVERY_ERROR_MESSAGE
                        )
                    }
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.value = _uiState.value.copy(
                    isEmailVerified = false,
                    errorMessage = e.message ?: RECOVERY_ERROR_MESSAGE
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun resetPassword() {
        val state = _uiState.value
        if (state.email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Ingresa tu correo electronico")
            return
        }

        if (!state.isEmailVerified) {
            _uiState.value = state.copy(errorMessage = "Primero verifica que el correo exista")
            return
        }

        if (state.isLoading) return
        _uiState.value = state.copy(isLoading = true)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)

            if (_uiState.value.newPassword.isBlank() || _uiState.value.confirmPassword.isBlank()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Completa la nueva contrasena", isLoading = false)
                return@launch
            }

            if (_uiState.value.newPassword.length < 8) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "La contrasena debe tener al menos 8 caracteres",
                    isLoading = false
                )
                return@launch
            }

            if (_uiState.value.newPassword != _uiState.value.confirmPassword) {
                _uiState.value = _uiState.value.copy(errorMessage = "Las contrasenas no coinciden", isLoading = false)
                return@launch
            }

            try {
                repository.resetPasswordByEmail(_uiState.value.email, _uiState.value.newPassword).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Contrasena actualizada correctamente",
                            newPassword = "",
                            confirmPassword = "",
                            isEmailVerified = false
                        )
                        _events.emit(PasswordRecoveryUiEvent.NavigateToLogin)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(errorMessage = error.message ?: RECOVERY_ERROR_MESSAGE)
                    }
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: RECOVERY_ERROR_MESSAGE)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun updatePassword() {
        resetPassword()
    }
}
