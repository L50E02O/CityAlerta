package ec.cityalerta.app.model.data.contracts.session

import ec.cityalerta.app.model.data.local.UserSessionEntity
import kotlinx.coroutines.flow.Flow

interface SessionRepositoryContract {
    fun getSession(): Flow<UserSessionEntity?>
    suspend fun saveSession(session: UserSessionEntity)
    suspend fun clearSession()
}
