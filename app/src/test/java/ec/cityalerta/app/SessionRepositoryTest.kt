package ec.cityalerta.app

import ec.cityalerta.app.model.data.local.UserSessionEntity
import ec.cityalerta.app.model.repository.SessionRepository
import ec.cityalerta.app.testdoubles.FakeSessionRepository
import ec.cityalerta.app.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SessionRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var dao: ec.cityalerta.app.model.data.local.UserSessionDao
    private lateinit var repository: SessionRepository

    @Before
    fun setup() {
        dao = mock()
        repository = SessionRepository(dao)
    }

    @Test
    fun `saveSession delegates to dao`() = runTest {
        val entity = UserSessionEntity(
            userId = "user-1",
            email = "test@test.com",
            roomId = "room-1",
            accessToken = "access",
            refreshToken = "refresh",
            expiresAt = 123L
        )

        repository.saveSession(entity)
        verify(dao).saveSession(entity)
    }

    @Test
    fun `clearSession delegates to dao`() = runTest {
        repository.clearSession()
        verify(dao).clearSession()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class FakeSessionRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `getSession emits null when empty`() = runTest {
        val repository = FakeSessionRepository()
        assertNull(repository.getSession().first())
    }

    @Test
    fun `saveSession and retrieve returns correct entity`() = runTest {
        val repository = FakeSessionRepository()
        val entity = UserSessionEntity(
            userId = "user-1",
            email = "test@test.com",
            roomId = "room-1",
            accessToken = "access",
            refreshToken = "refresh",
            expiresAt = 999L
        )

        repository.saveSession(entity)
        assertEquals(entity, repository.getSession().first())
    }

    @Test
    fun `clearSession removes all records`() = runTest {
        val repository = FakeSessionRepository()
        repository.saveSession(
            UserSessionEntity("u", "e", "r", "a", "rf", 1L)
        )
        repository.clearSession()
        assertNull(repository.getSession().first())
    }
}
