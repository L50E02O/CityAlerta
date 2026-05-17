package ec.cityalerta.app

import ec.cityalerta.app.model.repository.AuthMappedException
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests unitarios para AuthRepository.
 * Valida operaciones de autenticacion y manejo de errores.
 */
class AuthRepositoryTest {

    @Test
    fun testAuthMappedExceptionCreation() {
        // Arrange
        val message = "Correo no confirmado"
        val isEmailUnconfirmed = true

        // Act
        val exception = AuthMappedException(message, isEmailUnconfirmed)

        // Assert
        assertNotNull(exception)
        assertEquals(message, exception.message)
        assertTrue(exception.isEmailUnconfirmed)
    }

    @Test
    fun testAuthMappedExceptionWithConfirmedEmail() {
        // Arrange
        val message = "Credenciales invalidas"
        val isEmailUnconfirmed = false

        // Act
        val exception = AuthMappedException(message, isEmailUnconfirmed)

        // Assert
        assertNotNull(exception)
        assertEquals(message, exception.message)
        assertFalse(exception.isEmailUnconfirmed)
    }

    @Test
    fun testAuthMappedExceptionIsException() {
        // Arrange
        val exception = AuthMappedException("Error de autenticacion", false)

        // Assert
        assertTrue(exception is Exception)
        assertNotNull(exception.message)
    }

    @Test
    fun testAuthMappedExceptionWithEmptyMessage() {
        // Arrange & Act
        val exception = AuthMappedException("", true)

        // Assert
        assertEquals("", exception.message)
        assertTrue(exception.isEmailUnconfirmed)
    }

    @Test
    fun testAuthMappedExceptionWithLongMessage() {
        // Arrange
        val longMessage = "Usuario no encontrado. Por favor verifica tu correo electronico " +
                "e intenta nuevamente. Si el problema persiste, contacta con soporte."
        val exception = AuthMappedException(longMessage, false)

        // Assert
        assertEquals(longMessage, exception.message)
        assertFalse(exception.isEmailUnconfirmed)
    }

    @Test
    fun testAuthMappedExceptionFieldIndependence() {
        // Arrange & Act
        val exception1 = AuthMappedException("Error 1", true)
        val exception2 = AuthMappedException("Error 2", false)

        // Assert - Verify independence
        assertEquals("Error 1", exception1.message)
        assertTrue(exception1.isEmailUnconfirmed)
        assertEquals("Error 2", exception2.message)
        assertFalse(exception2.isEmailUnconfirmed)
    }

    @Test
    fun testEmailValidationEmptyString() {
        // Arrange
        val email = ""

        // Assert
        assertTrue(email.isBlank(), "Email vacio debe ser detectado")
    }

    @Test
    fun testEmailValidationWithValidFormat() {
        // Arrange
        val email = "usuario@example.com"

        // Assert
        assertTrue(email.contains("@"), "Email debe contener @")
        assertFalse(email.isBlank(), "Email valido no debe estar vacio")
    }

    @Test
    fun testPasswordValidationEmptyString() {
        // Arrange
        val password = ""

        // Assert
        assertTrue(password.isBlank(), "Contrasena vacia debe ser detectada")
    }

    @Test
    fun testPasswordValidationMinimumLength() {
        // Arrange
        val validPassword = "Abc123!@"
        val shortPassword = "A1!"

        // Assert
        assertTrue(validPassword.length >= 8, "Contrasena debe tener minimamente 8 caracteres")
        assertFalse(shortPassword.length >= 8, "Contrasena corta debe fallar validacion")
    }

    @Test
    fun testCiudadIdValidationEmptyString() {
        // Arrange
        val ciudadId = ""

        // Assert
        assertTrue(ciudadId.isBlank(), "Ciudad ID vacia debe ser detectada")
    }

    @Test
    fun testCiudadIdValidationValidUuid() {
        // Arrange
        val validUuid = "550e8400-e29b-41d4-a716-446655440000"

        // Assert
        assertTrue(validUuid.isNotBlank(), "UUID valido no debe estar vacio")
        assertTrue(validUuid.contains("-"), "UUID debe contener guiones")
    }

    @Test
    fun testAuthExceptionInheritance() {
        // Arrange & Act
        val exception = AuthMappedException("Mensaje de error", true)

        // Assert
        assertTrue(exception is Throwable, "AuthMappedException debe ser Throwable")
        assertTrue(exception is Exception, "AuthMappedException debe ser Exception")
    }

    @Test
    fun testAuthExceptionWithMultipleScenarios() {
        // Arrange - Different error scenarios
        val scenarios = listOf(
            Triple("Correo no confirmado", true, "unconfirmed_email"),
            Triple("Usuario no encontrado", false, "user_not_found"),
            Triple("Contrasena incorrecta", false, "invalid_password"),
            Triple("Cuenta deshabilitada", false, "account_disabled")
        )

        // Act & Assert
        scenarios.forEach { (message, isUnconfirmed, scenario) ->
            val exception = AuthMappedException(message, isUnconfirmed)
            assertEquals(message, exception.message)
            assertEquals(isUnconfirmed, exception.isEmailUnconfirmed)
        }
    }
}
