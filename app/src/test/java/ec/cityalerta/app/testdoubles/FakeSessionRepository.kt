package ec.cityalerta.app.testdoubles

import ec.cityalerta.app.model.data.contracts.session.SessionRepositoryContract
import ec.cityalerta.app.model.data.local.UserSessionEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSessionRepository : SessionRepositoryContract {

    private val _session = MutableStateFlow<UserSessionEntity?>(null)

    var saveCalls: Int = 0
    var clearCalls: Int = 0

    override fun getSession(): Flow<UserSessionEntity?> = _session.asStateFlow()

    override suspend fun saveSession(session: UserSessionEntity) {
        delay(50)
        saveCalls++
        _session.value = session
    }

    override suspend fun clearSession() {
        delay(50)
        clearCalls++
        _session.value = null
    }

    fun setSession(entity: UserSessionEntity?) {
        _session.value = entity
    }
}
