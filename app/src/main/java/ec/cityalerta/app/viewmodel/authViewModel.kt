package ec.cityalerta.app.viewmodel

import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.remote.service.PushSubscriptionAuthRequiredException
import ec.cityalerta.app.model.remote.service.PushSubscriptionDisabledException
import ec.cityalerta.app.model.remote.service.PushSubscriptionRegistrar
import ec.cityalerta.app.model.remote.service.PushSubscriptionTokenMissingException
import ec.cityalerta.app.model.repository.AuthMappedException
import ec.cityalerta.app.model.repository.CiudadRepository
import ec.cityalerta.app.model.repository.PerfilLocalRepository
import ec.cityalerta.app.model.repository.PerfilResumenRepository
import ec.cityalerta.app.model.utils.AuthErrorMapper
import ec.cityalerta.app.model.utils.MappedAuthError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    private val perfilResumenRepository: PerfilResumenRepository? = null,
    private val perfilLocalRepository: PerfilLocalRepository? = null,
    private val pushRegistrar: PushSubscriptionRegistrar? = null
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthState())
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    private val _ciudades = MutableStateFlow<List<Ciudad>>(emptyList())
    val ciudades: StateFlow<List<Ciudad>> = _ciudades

    private val _ciudadLoadError = MutableStateFlow<String?>(null)
    val ciudadLoadError: StateFlow<String?> = _ciudadLoadError

    init {
        val enabled = pushRegistrar?.isNotificationsEnabled() ?: false
        _uiState.update { it.copy(notificationsEnabled = enabled) }
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
        _uiState.update { 
            it.copy(
                email = email.trim(),
                isEmailUnconfirmed = false,
                errorMessage = null
            )
        }
    }

    fun onPasswordChange(password: String){
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onCiudadChange(nombre: String) {
        _uiState.update { it.copy(ciudadNombre = nombre, ciudadId = "") }
    }

    fun onCiudadSelected(nombre: String, id: String) {
        _uiState.update { it.copy(ciudadNombre = nombre, ciudadId = id) }
    }

    fun setAuthInfoMessage(message: String) {
        _uiState.update { 
            it.copy(
                infoMessage = message,
                errorMessage = null,
                isEmailUnconfirmed = false
            )
        }
    }

    fun clearInfoMessage() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun onLoginClick(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        if (currentState.email.isEmpty() || currentState.password.isEmpty()){
            _uiState.update { it.copy(errorMessage = "El correo y la contrasena no pueden estar vacios") }
            return
        }

        if (currentState.isLoading) return
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null, infoMessage = null, isEmailUnconfirmed = false) }

            try {
                repository.signIn(currentState.email, currentState.password).fold(
                    onSuccess = {
                        // Persistir perfil localmente tras login exitoso
                        fetchAndPersistProfile()
                        registerPushSubscriptionIfPossible()
                        onSuccess()
                    },
                    onFailure = { error -> applyAuthFailure(error) }
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                applyAuthFailure(e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun fetchAndPersistProfile() {
        val remoteRepo = perfilResumenRepository ?: return
        val localRepo = perfilLocalRepository ?: return

        remoteRepo.getCurrentResumen().onSuccess { perfil ->
            if (perfil != null) {
                localRepo.guardarPerfilResumen(perfil)
            }
        }
    }

    fun onRegisterClick(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        if (currentState.email.isEmpty() || currentState.password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "El correo y la contrasena no pueden estar vacios") }
            return
        }

        if (currentState.ciudadId.isEmpty()){
            _uiState.update { it.copy(errorMessage = "Selecciona tu ciudad") }
            return
        }

        if (currentState.isLoading) return
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null, infoMessage = null, isEmailUnconfirmed = false) }

            try {
                repository.signUp(currentState.email, currentState.password, currentState.ciudadId).fold(
                    onSuccess = {
                        val hasSession = repository.getUserId().isSuccess
                        if (hasSession) {
                            registerPushSubscriptionIfPossible()
                            onSuccess()
                        } else {
                            _uiState.update { 
                                it.copy(
                                    infoMessage = "Registro exitoso. Te enviamos un correo de activacion. " +
                                        "Abre el enlace en este celular (debe abrir CityAlerta, no el navegador web) y luego inicia sesion."
                                )
                            }
                            onSuccess()
                        }
                    },
                    onFailure = { error -> applyAuthFailure(error) }
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                applyAuthFailure(e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resendActivationEmail() {
        val currentState = _uiState.value
        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingresa tu correo para reenviar la activacion") }
            return
        }

        if (currentState.isLoading) return
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }

            try {
                repository.resendSignupConfirmation(currentState.email).fold(
                    onSuccess = {
                        _uiState.update { 
                            it.copy(
                                infoMessage = "Te reenviamos el correo de activacion. Revisa tu bandeja y spam.",
                                isEmailUnconfirmed = false
                            )
                        }
                    },
                    onFailure = { error -> applyAuthFailure(error) }
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                applyAuthFailure(e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
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
        _uiState.update { 
            it.copy(
                errorMessage = mapped.message,
                isEmailUnconfirmed = mapped.isEmailUnconfirmed
            )
        }
    }

    fun onNotificationsPermissionGranted() {
        val registrar = pushRegistrar ?: return
        registrar.setNotificationsEnabled(true)
        _uiState.update { it.copy(notificationsEnabled = true) }
        registerPushSubscriptionIfPossible()
    }

    fun onNotificationsPermissionDenied() {
        val registrar = pushRegistrar ?: return
        registrar.setNotificationsEnabled(false)
        _uiState.update { it.copy(notificationsEnabled = false) }
    }

    fun onNotificationsDisabledByUser() {
        val registrar = pushRegistrar ?: return
        registrar.setNotificationsEnabled(false)
        _uiState.update { it.copy(notificationsEnabled = false) }
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
