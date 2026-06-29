package ec.cityalerta.app

import ec.cityalerta.app.model.session.SessionState
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SessionStateTest {

    @Test
    fun testUnauthenticatedState() {
        val state = SessionState.Unauthenticated
        assertTrue(state is SessionState.Unauthenticated)
    }

    @Test
    fun testRefreshingState() {
        val state = SessionState.Refreshing
        assertTrue(state is SessionState.Refreshing)
    }

    @Test
    fun testAuthenticatedState() {
        val userId = "user-123"
        val state = SessionState.Authenticated(userId)
        assertTrue(state is SessionState.Authenticated)
        assertEquals(userId, state.userId)
    }

    @Test
    fun testErrorState() {
        val message = "Error de conexión"
        val state = SessionState.Error(message)
        assertTrue(state is SessionState.Error)
        assertEquals(message, state.message)
        assertFalse(state.isTransient)
    }

    @Test
    fun testErrorStateWithTransient() {
        val message = "Error de red"
        val state = SessionState.Error(message, isTransient = true)
        assertTrue(state is SessionState.Error)
        assertEquals(message, state.message)
        assertTrue(state.isTransient)
    }
}
