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
        updateFromCurrentSession()
        listenerJob = scope.launch {
            auth.sessionStatus.collect {
                updateFromCurrentSession()
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

            val session = auth.currentSessionOrNull()
            if (session != null) {
                refreshBackoffMs = DEFAULT_REFRESH_BACKOFF_MS
                _state.value = SessionState.Authenticated(session.user?.id.orEmpty())
                return
            }

            _state.value = SessionState.Refreshing
            runCatching { auth.refreshCurrentSession() }
                .onSuccess {
                    refreshBackoffMs = DEFAULT_REFRESH_BACKOFF_MS
                    updateFromCurrentSession()
                }
                .onFailure { error ->
                    refreshBackoffMs = (refreshBackoffMs * 2).coerceAtMost(MAX_REFRESH_BACKOFF_MS)
                    _state.value = SessionState.Error(error.message ?: "Session refresh failed")
                }
        } finally {
            refreshMutex.unlock()
        }
    }

    private fun updateFromCurrentSession() {
        val session = auth.currentSessionOrNull()
        _state.value = if (session == null) {
            SessionState.Unauthenticated
        } else {
            SessionState.Authenticated(session.user?.id.orEmpty())
        }
    }

    companion object {
        private const val DEFAULT_REFRESH_BACKOFF_MS = 2_000L
        private const val MAX_REFRESH_BACKOFF_MS = 60_000L
    }
}
