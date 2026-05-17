package ec.cityalerta.app

import ec.cityalerta.app.model.utils.AuthErrorMapper
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthErrorMapperTest {

    @Test
    fun map_detectsUnconfirmedEmail() {
        val result = AuthErrorMapper.map(Exception("Email not confirmed"))

        assertTrue(result.isEmailUnconfirmed)
        assertTrue(result.message.contains("no esta activada"))
    }

    @Test
    fun map_detectsUnconfirmedEmailInSpanish() {
        val result = AuthErrorMapper.map(Exception("usuario no confirmado"))

        assertTrue(result.isEmailUnconfirmed)
    }

    @Test
    fun map_detectsInvalidCredentials() {
        val result = AuthErrorMapper.map(Exception("Invalid login credentials"))

        assertFalse(result.isEmailUnconfirmed)
        assertTrue(result.message.contains("incorrectos"))
    }

    @Test
    fun map_detectsUserAlreadyRegistered() {
        val result = AuthErrorMapper.map(Exception("User already registered"))

        assertFalse(result.isEmailUnconfirmed)
        assertTrue(result.message.contains("ya tiene una cuenta"))
    }

    @Test
    fun map_detectsRateLimit() {
        val result = AuthErrorMapper.map(Exception("email rate limit exceeded"))

        assertFalse(result.isEmailUnconfirmed)
        assertTrue(result.message.contains("Limite de correos"))
    }

    @Test
    fun map_stripsUrlSuffixFromMessage() {
        val result = AuthErrorMapper.map(
            Exception("Invalid login credentials\nURL: https://example.supabase.co")
        )

        assertTrue(result.message.contains("incorrectos"))
    }

    @Test
    fun map_usesCauseMessageWhenPresent() {
        val error = Exception("Request failed")
        error.initCause(Exception("email not confirmed"))

        val result = AuthErrorMapper.map(error)

        assertTrue(result.isEmailUnconfirmed)
    }

    @Test
    fun map_returnsGenericMessageForUnknownError() {
        val result = AuthErrorMapper.map(Exception("unexpected backend failure"))

        assertFalse(result.isEmailUnconfirmed)
        assertEquals("unexpected backend failure", result.message)
    }

    @Test
    fun map_detectsSignupDisabled() {
        val result = AuthErrorMapper.map(Exception("signup is disabled"))

        assertTrue(result.message.contains("registro no esta disponible"))
    }

    @Test
    fun map_detectsSmtpFailure() {
        val result = AuthErrorMapper.map(Exception("smtp mail fail"))

        assertTrue(result.message.contains("SMTP"))
    }

    @Test
    fun map_detectsInvalidRedirect() {
        val result = AuthErrorMapper.map(Exception("redirect url invalid"))

        assertTrue(result.message.contains("Redirect URLs"))
    }

    @Test
    fun map_returnsDefaultWhenMessageMissing() {
        val result = AuthErrorMapper.map(Exception())

        assertEquals("No se pudo completar la operacion. Intenta de nuevo.", result.message)
    }
}
