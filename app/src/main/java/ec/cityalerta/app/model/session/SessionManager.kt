package ec.cityalerta.app.model.session

import io.github.jan.supabase.gotrue.Auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class SessionManager(
    private val auth: Auth
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val refreshMutex = Mutex()
    private var listenerJob: Job? = null
    private var lastRefreshAttemptMs = 0L
    private var refreshBackoffMs = DEFAULT_REFRESH_BACKOFF_MS

    private val _state = MutableStateFlow<SessionState>(SessionState.Refreshing)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    fun start() {
        if (listenerJob != null) return
        
        listenerJob = scope.launch {
            auth.sessionStatus.collect { status ->
                when (status) {
                    is io.github.jan.supabase.gotrue.SessionStatus.Authenticated -> {
                        _state.value = SessionState.Authenticated(status.session.user?.id.orEmpty())
                    }
                    is io.github.jan.supabase.gotrue.SessionStatus.LoadingFromStorage -> {
                        _state.value = SessionState.Refreshing
                    }
                    is io.github.jan.supabase.gotrue.SessionStatus.NetworkError -> {
                        _state.value = SessionState.Error("Error de red", isTransient = true)
                    }
                    is io.github.jan.supabase.gotrue.SessionStatus.NotAuthenticated -> {
                        _state.value = SessionState.Unauthenticated
                    }
                }
            }
        }
    }

    fun stop() {
        listenerJob?.cancel()
        listenerJob = null
    }

    suspend fun refreshIfNeeded() {
        if (!refreshMutex.tryLock()) return
        try {
            val now = System.currentTimeMillis()
            if (now - lastRefreshAttemptMs < refreshBackoffMs) return
            lastRefreshAttemptMs = now

            if (_state.value is SessionState.Authenticated) return

            runCatching { auth.refreshCurrentSession() }
                .onFailure { error ->
                    refreshBackoffMs = (refreshBackoffMs * 2).coerceAtMost(MAX_REFRESH_BACKOFF_MS)
                    if (isNetworkError(error)) {
                        _state.value = SessionState.Error("Sin conexión", isTransient = true)
                    }
                }
        } finally {
            refreshMutex.unlock()
        }
    }

    private fun isNetworkError(throwable: Throwable): Boolean {
        val msg = throwable.message?.lowercase() ?: ""
        return throwable is java.io.IOException || 
               msg.contains("timeout") || 
               msg.contains("network") || 
               msg.contains("connection")
    }

    companion object {
        private const val DEFAULT_REFRESH_BACKOFF_MS = 2_000L
        private const val MAX_REFRESH_BACKOFF_MS = 60_000L
    }
}
