package ec.cityalerta.app

import ec.cityalerta.app.model.utils.EmailValidator
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmailValidatorTest {

    @Test
    fun isValid_acceptsStandardEmail() {
        assertTrue(EmailValidator.isValid("usuario@example.com"))
    }

    @Test
    fun isValid_rejectsBlankEmail() {
        assertFalse(EmailValidator.isValid("   "))
    }

    @Test
    fun isValid_rejectsMissingAtSymbol() {
        assertFalse(EmailValidator.isValid("usuario.example.com"))
    }

    @Test
    fun isValid_trimsWhitespace() {
        assertTrue(EmailValidator.isValid("  usuario@example.com  "))
    }
}
