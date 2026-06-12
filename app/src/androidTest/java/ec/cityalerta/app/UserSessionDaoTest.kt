package ec.cityalerta.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ec.cityalerta.app.model.data.local.AppDatabase
import ec.cityalerta.app.model.data.local.UserSessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserSessionDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ec.cityalerta.app.model.data.local.UserSessionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.userSessionDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun saveSession_and_retrieve_returns_correct_entity() = runTest {
        val entity = UserSessionEntity(
            userId = "user-1",
            email = "test@test.com",
            roomId = "room-1",
            accessToken = "access",
            refreshToken = "refresh",
            expiresAt = 999L
        )

        dao.saveSession(entity)
        assertEquals(entity, dao.getSession().first())
    }

    @Test
    fun clearSession_removes_all_records() = runTest {
        dao.saveSession(
            UserSessionEntity("u", "e", "r", "a", "rf", 1L)
        )
        dao.clearSession()
        assertNull(dao.getSession().first())
    }

    @Test
    fun getSession_flow_emits_null_when_empty() = runTest {
        assertNull(dao.getSession().first())
    }
}
