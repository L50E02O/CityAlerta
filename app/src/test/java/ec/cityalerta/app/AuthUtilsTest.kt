package ec.cityalerta.app

import ec.cityalerta.app.model.utils.AuthApiResponseParser
import ec.cityalerta.app.model.utils.AuthErrorMapper
import ec.cityalerta.app.model.utils.EmailValidator
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests para las utilidades relacionadas con autenticación.
 * Cubre EmailValidator, AuthErrorMapper y AuthApiResponseParser.
 */
class AuthUtilsTest {

    @Test
    fun testEmailValidator_ValidEmails() {
        assertTrue(EmailValidator.isValid("test@example.com"))
        assertTrue(EmailValidator.isValid("user.name@domain.co"))
        assertTrue(EmailValidator.isValid("user+extra@domain.com"))
        assertTrue(EmailValidator.isValid("123@domain.org"))
    }

    @Test
    fun testEmailValidator_InvalidEmails() {
        assertFalse(EmailValidator.isValid("invalid-email"))
        assertFalse(EmailValidator.isValid("test@"))
        assertFalse(EmailValidator.isValid("@domain.com"))
        assertFalse(EmailValidator.isValid("test@domain"))
        assertFalse(EmailValidator.isValid("test @domain.com"))
        assertFalse(EmailValidator.isValid(""))
    }

    @Test
    fun testAuthErrorMapper_EmailNotConfirmed() {
        val throwable = RuntimeException("Email not confirmed")
        val result = AuthErrorMapper.map(throwable)
        assertTrue(result.isEmailUnconfirmed)
        assertTrue(result.message.contains("activada", ignoreCase = true))
    }

    @Test
    fun testAuthErrorMapper_InvalidCredentials() {
        val throwable = RuntimeException("Invalid login credentials")
        val result = AuthErrorMapper.map(throwable)
        assertFalse(result.isEmailUnconfirmed)
        assertTrue(result.message.contains("incorrectos", ignoreCase = true))
    }

    @Test
    fun testAuthErrorMapper_UserAlreadyRegistered() {
        val throwable = RuntimeException("User already registered")
        val result = AuthErrorMapper.map(throwable)
        assertTrue(result.message.contains("ya tiene una cuenta", ignoreCase = true))
    }

    @Test
    fun testAuthErrorMapper_RateLimit() {
        val throwable = RuntimeException("Email rate limit exceeded")
        val result = AuthErrorMapper.map(throwable)
        assertTrue(result.message.contains("Limite", ignoreCase = true))
    }

    @Test
    fun testAuthErrorMapper_Fallback() {
        val throwable = RuntimeException("Unknown error message")
        val result = AuthErrorMapper.map(throwable)
        assertEquals("Unknown error message", result.message)
    }

    @Test
    fun testAuthApiResponseParser_ParseErrorField() {
        val body = "{\"error\": \"Specific error message\"}"
        val message = AuthApiResponseParser.parseErrorMessage(body, 400)
        assertEquals("Specific error message", message)
    }

    @Test
    fun testAuthApiResponseParser_ParseMsgField() {
        val body = "{\"msg\": \"Message content\"}"
        val message = AuthApiResponseParser.parseErrorMessage(body, 400)
        assertEquals("Message content", message)
    }

    @Test
    fun testAuthApiResponseParser_InvalidJson() {
        val body = "not json"
        val message = AuthApiResponseParser.parseErrorMessage(body, 500)
        assertEquals("No se pudo completar la operacion (500)", message)
    }
}
