package ec.cityalerta.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.remote.service.PushSubscriptionAuthRequiredException
import ec.cityalerta.app.model.remote.service.PushSubscriptionDisabledException
import ec.cityalerta.app.model.remote.service.PushSubscriptionRegistrar
import ec.cityalerta.app.model.remote.service.PushSubscriptionTokenMissingException
import ec.cityalerta.app.model.repository.AuthMappedException
import ec.cityalerta.app.model.utils.AuthErrorMapper
import ec.cityalerta.app.model.utils.MappedAuthError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RegisterPhase {
    data object Form : RegisterPhase()
    data class EmailVerificationPending(val email: String) : RegisterPhase()
}

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val ciudadNombre: String = "",
    val ciudadId: String = "",
    val notificationsEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val isEmailUnconfirmed: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val registerPhase: RegisterPhase = RegisterPhase.Form
)

sealed class AuthUiEvent {
    data object NavigateToHome : AuthUiEvent()
    data object NavigateToLogin : AuthUiEvent()
    data class ShowSnackbar(val message: String) : AuthUiEvent()
}

// No domain layer: auth flows map 1:1 to repository calls without shared business logic.
class AuthViewModel(
    private val repository: AuthRepositoryContract,
    private val pushRegistrar: PushSubscriptionRegistrar? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthUiEvent>()
    val events: SharedFlow<AuthUiEvent> = _events.asSharedFlow()

    init {
        val enabled = pushRegistrar?.isNotificationsEnabled() ?: false
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email.trim(),
            isEmailUnconfirmed = false,
            errorMessage = null
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onCiudadChange(nombre: String) {
        _uiState.value = _uiState.value.copy(ciudadNombre = nombre, ciudadId = "")
    }

    fun onCiudadSelected(nombre: String, id: String) {
        _uiState.value = _uiState.value.copy(ciudadNombre = nombre, ciudadId = id)
    }

    fun setAuthInfoMessage(message: String) {
        _uiState.value = _uiState.value.copy(
            infoMessage = message,
            errorMessage = null,
            isEmailUnconfirmed = false
        )
    }

    fun clearInfoMessage() {
        _uiState.value = _uiState.value.copy(infoMessage = null)
    }

    fun onLoginClick() {
        val state = _uiState.value
        if (state.email.isEmpty() || state.password.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "El correo y la contrasena no pueden estar vacios")
            return
        }

        if (state.isLoading) return
        _uiState.value = state.copy(isLoading = true)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                errorMessage = null,
                infoMessage = null,
                isEmailUnconfirmed = false
            )

            try {
                repository.signIn(_uiState.value.email, _uiState.value.password).fold(
                    onSuccess = {
                        registerPushSubscriptionIfPossible()
                        _events.emit(AuthUiEvent.NavigateToHome)
                    },
                    onFailure = { error -> applyAuthFailure(error) }
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                applyAuthFailure(e)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onRegisterClick() {
        val state = _uiState.value
        if (state.email.isEmpty() || state.password.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "El correo y la contrasena no pueden estar vacios")
            return
        }

        if (state.ciudadId.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "Selecciona tu ciudad")
            return
        }

        if (state.isLoading) return
        _uiState.value = state.copy(isLoading = true)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                errorMessage = null,
                infoMessage = null,
                isEmailUnconfirmed = false
            )

            try {
                repository.signUp(_uiState.value.email, _uiState.value.password, _uiState.value.ciudadId).fold(
                    onSuccess = {
                        val hasSession = repository.getUserId().isSuccess
                        if (hasSession) {
                            registerPushSubscriptionIfPossible()
                            _events.emit(AuthUiEvent.NavigateToHome)
                        } else {
                            _uiState.value = _uiState.value.copy(
                                registerPhase = RegisterPhase.EmailVerificationPending(_uiState.value.email)
                            )
                        }
                    },
                    onFailure = { error -> applyAuthFailure(error) }
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                applyAuthFailure(e)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun checkEmailVerified() {
        if (_uiState.value.isLoading) return
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                repository.checkEmailVerified().fold(
                    onSuccess = { verified ->
                        if (verified) {
                            registerPushSubscriptionIfPossible()
                            _events.emit(AuthUiEvent.NavigateToHome)
                        } else {
                            _events.emit(
                                AuthUiEvent.ShowSnackbar(
                                    "Aun no hemos detectado la verificacion. Abre el enlace del correo en este celular."
                                )
                            )
                        }
                    },
                    onFailure = { error ->
                        _events.emit(
                            AuthUiEvent.ShowSnackbar(
                                error.message ?: "No se pudo verificar el correo"
                            )
                        )
                    }
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _events.emit(AuthUiEvent.ShowSnackbar(e.message ?: "Error al verificar el correo"))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun resendActivationEmail() {
        if (_uiState.value.email.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Ingresa tu correo para reenviar la activacion")
            return
        }

        if (_uiState.value.isLoading) return
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null)

            try {
                repository.resendSignupConfirmation(_uiState.value.email).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            infoMessage = "Te reenviamos el correo de activacion. Revisa tu bandeja y spam.",
                            isEmailUnconfirmed = false
                        )
                    },
                    onFailure = { error -> applyAuthFailure(error) }
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                applyAuthFailure(e)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun applyAuthFailure(error: Throwable) {
        val mapped = when (error) {
            is AuthMappedException -> MappedAuthError(
                message = error.message.orEmpty(),
                isEmailUnconfirmed = error.isEmailUnconfirmed
            )
            else -> AuthErrorMapper.map(error)
        }
        _uiState.value = _uiState.value.copy(
            errorMessage = mapped.message,
            isEmailUnconfirmed = mapped.isEmailUnconfirmed
        )
    }

    fun onNotificationsPermissionGranted() {
        val registrar = pushRegistrar ?: return
        registrar.setNotificationsEnabled(true)
        _uiState.value = _uiState.value.copy(notificationsEnabled = true)
        registerPushSubscriptionIfPossible()
    }

    fun onNotificationsPermissionDenied() {
        val registrar = pushRegistrar ?: return
        registrar.setNotificationsEnabled(false)
        _uiState.value = _uiState.value.copy(notificationsEnabled = false)
    }

    fun onNotificationsDisabledByUser() {
        val registrar = pushRegistrar ?: return
        registrar.setNotificationsEnabled(false)
        _uiState.value = _uiState.value.copy(notificationsEnabled = false)
        viewModelScope.launch {
            registrar.unregisterToken().onFailure { }
        }
    }

    private fun registerPushSubscriptionIfPossible() {
        val registrar = pushRegistrar ?: return
        viewModelScope.launch {
            registrar.registerTokenIfAllowed().onFailure { error ->
                if (
                    error is PushSubscriptionAuthRequiredException ||
                    error is PushSubscriptionDisabledException ||
                    error is PushSubscriptionTokenMissingException
                ) {
                    return@onFailure
                }
            }
        }
    }
}
