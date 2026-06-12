package ec.cityalerta.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.data.contracts.session.SessionRepositoryContract
import ec.cityalerta.app.model.data.local.UserSessionEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SplashUiState(
    val isRestoring: Boolean = true
)

sealed class SplashUiEvent {
    data object NavigateToHome : SplashUiEvent()
    data object NavigateToLogin : SplashUiEvent()
}

// No domain layer: splash only orchestrates session restore from Room + Supabase.
class SplashViewModel(
    private val sessionRepository: SessionRepositoryContract,
    private val authRepository: AuthRepositoryContract
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SplashUiEvent>()
    val events: SharedFlow<SplashUiEvent> = _events.asSharedFlow()

    fun restoreSession() {
        viewModelScope.launch {
            _uiState.value = SplashUiState(isRestoring = true)

            val stored = sessionRepository.getSession().first()
            if (stored == null) {
                _uiState.value = SplashUiState(isRestoring = false)
                _events.emit(SplashUiEvent.NavigateToLogin)
                return@launch
            }

            val now = System.currentTimeMillis()
            val restored = if (stored.expiresAt > now) {
                authRepository.restoreSession(stored.accessToken, stored.refreshToken)
            } else {
                authRepository.refreshAndPersistSession()
            }

            restored.fold(
                onSuccess = {
                    _uiState.value = SplashUiState(isRestoring = false)
                    _events.emit(SplashUiEvent.NavigateToHome)
                },
                onFailure = {
                    sessionRepository.clearSession()
                    authRepository.logOut()
                    _uiState.value = SplashUiState(isRestoring = false)
                    _events.emit(SplashUiEvent.NavigateToLogin)
                }
            )
        }
    }

    fun persistSessionFromEntity(entity: UserSessionEntity) {
        viewModelScope.launch {
            sessionRepository.saveSession(entity)
        }
    }
}
