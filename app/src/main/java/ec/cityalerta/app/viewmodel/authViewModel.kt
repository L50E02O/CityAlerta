package ec.cityalerta.app.viewmodel

import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.remote.service.PushSubscriptionAuthRequiredException
import ec.cityalerta.app.model.remote.service.PushSubscriptionDisabledException
import ec.cityalerta.app.model.remote.service.PushSubscriptionRegistrar
import ec.cityalerta.app.model.remote.service.PushSubscriptionTokenMissingException
import ec.cityalerta.app.model.repository.AuthMappedException
import ec.cityalerta.app.model.repository.CiudadRepository
import ec.cityalerta.app.model.utils.AuthErrorMapper
import ec.cityalerta.app.model.utils.MappedAuthError
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


data class AuthState(
    val email: String = "",
    val password: String = "",
    val ciudadNombre: String = "",
    val ciudadId: String = "",
    val notificationsEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val isEmailUnconfirmed: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class AuthViewModel(
    private val repository: AuthRepositoryContract,
    private val ciudadRepository: CiudadRepository,
    private val pushRegistrar: PushSubscriptionRegistrar? = null
) : ViewModel() {
    var uiState by mutableStateOf(AuthState())
        private set

    private val _ciudades = MutableStateFlow<List<Ciudad>>(emptyList())
    val ciudades: StateFlow<List<Ciudad>> = _ciudades

    private val _ciudadLoadError = MutableStateFlow<String?>(null)
    val ciudadLoadError: StateFlow<String?> = _ciudadLoadError

    init {
        val enabled = pushRegistrar?.isNotificationsEnabled() ?: false
        uiState = uiState.copy(notificationsEnabled = enabled)
    }

    fun loadCiudades(country: String = "Ecuador") {
        viewModelScope.launch {
            ciudadRepository.getAllByCountry(country).fold(
                onSuccess = { ciudades ->
                    _ciudades.value = ciudades
                    _ciudadLoadError.value = null
                },
                onFailure = { error ->
                    _ciudadLoadError.value = error.message ?: "No se pudo cargar las ciudades"
                }
            )
        }
    }

    fun onEmailChange(email: String){
        uiState = uiState.copy(
            email = email.trim(),
            isEmailUnconfirmed = false,
            errorMessage = null
        )
    }

    fun onPasswordChange(password: String){
        uiState = uiState.copy(password = password)
    }

    fun onCiudadChange(nombre: String) {
        uiState = uiState.copy(ciudadNombre = nombre, ciudadId = "")
    }

    fun onCiudadSelected(nombre: String, id: String) {
        uiState = uiState.copy(ciudadNombre = nombre, ciudadId = id)
    }

    fun setAuthInfoMessage(message: String) {
        uiState = uiState.copy(
            infoMessage = message,
            errorMessage = null,
            isEmailUnconfirmed = false
        )
    }

    fun clearInfoMessage() {
        uiState = uiState.copy(infoMessage = null)
    }

    fun onLoginClick(onSuccess: () -> Unit) {
        if (uiState.email.isEmpty() || uiState.password.isEmpty()){
            uiState = uiState.copy(errorMessage = "El correo y la contrasena no pueden estar vacios")
            return
        }

        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true)

        viewModelScope.launch {
            uiState = uiState.copy(errorMessage = null, infoMessage = null, isEmailUnconfirmed = false)

            try {
                repository.signIn(uiState.email, uiState.password).fold(
                    onSuccess = {
                        registerPushSubscriptionIfPossible()
                        onSuccess()
                    },
                    onFailure = { error -> applyAuthFailure(error) }
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                applyAuthFailure(e)
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun onRegisterClick(onSuccess: () -> Unit) {
        if (uiState.email.isEmpty() || uiState.password.isEmpty()) {
            uiState = uiState.copy(errorMessage = "El correo y la contrasena no pueden estar vacios")
            return
        }

        if (uiState.ciudadId.isEmpty()){
            uiState = uiState.copy(errorMessage = "Selecciona tu ciudad")
            return
        }

        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true)

        viewModelScope.launch {
            uiState = uiState.copy(errorMessage = null, infoMessage = null, isEmailUnconfirmed = false)

            try {
                repository.signUp(uiState.email, uiState.password, uiState.ciudadId).fold(
                    onSuccess = {
                        val hasSession = repository.getUserId().isSuccess
                        if (hasSession) {
                            registerPushSubscriptionIfPossible()
                            onSuccess()
                        } else {
                            uiState = uiState.copy(
                                infoMessage = "Registro exitoso. Te enviamos un correo de activacion. " +
                                    "Abre el enlace en este celular (debe abrir CityAlerta, no el navegador web) y luego inicia sesion."
                            )
                            onSuccess()
                        }
                    },
                    onFailure = { error -> applyAuthFailure(error) }
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                applyAuthFailure(e)
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun resendActivationEmail() {
        if (uiState.email.isBlank()) {
            uiState = uiState.copy(errorMessage = "Ingresa tu correo para reenviar la activacion")
            return
        }

        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true)

        viewModelScope.launch {
            uiState = uiState.copy(errorMessage = null)

            try {
                repository.resendSignupConfirmation(uiState.email).fold(
                    onSuccess = {
                        uiState = uiState.copy(
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
                uiState = uiState.copy(isLoading = false)
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
        uiState = uiState.copy(
            errorMessage = mapped.message,
            isEmailUnconfirmed = mapped.isEmailUnconfirmed
        )
    }

    fun onNotificationsPermissionGranted() {
        val registrar = pushRegistrar ?: return
        registrar.setNotificationsEnabled(true)
        uiState = uiState.copy(notificationsEnabled = true)
        registerPushSubscriptionIfPossible()
    }

    fun onNotificationsPermissionDenied() {
        val registrar = pushRegistrar ?: return
        registrar.setNotificationsEnabled(false)
        uiState = uiState.copy(notificationsEnabled = false)
    }

    fun onNotificationsDisabledByUser() {
        val registrar = pushRegistrar ?: return
        registrar.setNotificationsEnabled(false)
        uiState = uiState.copy(notificationsEnabled = false)
        viewModelScope.launch {
            registrar.unregisterToken().onFailure { error ->
            }
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

    companion object {
    }
}
