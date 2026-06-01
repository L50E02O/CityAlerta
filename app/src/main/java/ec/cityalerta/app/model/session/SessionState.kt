package ec.cityalerta.app.model.session

sealed class SessionState {
    data object Unauthenticated : SessionState()
    data object Refreshing : SessionState()
    data class Authenticated(val userId: String) : SessionState()
    data class Error(val message: String) : SessionState()
}
