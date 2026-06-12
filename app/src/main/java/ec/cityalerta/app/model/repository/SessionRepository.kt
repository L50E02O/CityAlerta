package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.contracts.session.SessionRepositoryContract
import ec.cityalerta.app.model.data.local.UserSessionDao
import ec.cityalerta.app.model.data.local.UserSessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SessionRepository(
    private val dao: UserSessionDao
) : SessionRepositoryContract {

    override fun getSession(): Flow<UserSessionEntity?> = dao.getSession()

    override suspend fun saveSession(session: UserSessionEntity) {
        withContext(Dispatchers.IO) {
            dao.saveSession(session)
        }
    }

    override suspend fun clearSession() {
        withContext(Dispatchers.IO) {
            dao.clearSession()
        }
    }
}
