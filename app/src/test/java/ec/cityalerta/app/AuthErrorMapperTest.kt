package ec.cityalerta.app

import ec.cityalerta.app.model.utils.AuthErrorMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthErrorMapperTest {

    @Test
    fun map_detectsUnconfirmedEmail() {
        val result = AuthErrorMapper.map(Exception("Email not confirmed"))

        assertTrue(result.isEmailUnconfirmed)
        assertTrue(result.message.contains("no esta activada"))
    }

    @Test
    fun map_detectsInvalidCredentials() {
        val result = AuthErrorMapper.map(Exception("Invalid login credentials"))

        assertEquals(false, result.isEmailUnconfirmed)
        assertTrue(result.message.contains("incorrectos"))
    }
}
