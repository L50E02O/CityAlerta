package ec.cityalerta.app

import ec.cityalerta.app.model.utils.AuthApiResponseParser
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthApiResponseParserTest {

    @Test
    fun parseErrorMessage_readsErrorField() {
        val message = AuthApiResponseParser.parseErrorMessage(
            body = """{"error":"Correo ya registrado"}""",
            statusCode = 400
        )

        assertEquals("Correo ya registrado", message)
    }

    @Test
    fun parseErrorMessage_readsMsgField() {
        val message = AuthApiResponseParser.parseErrorMessage(
            body = """{"msg":"Sesion invalida"}""",
            statusCode = 401
        )

        assertEquals("Sesion invalida", message)
    }

    @Test
    fun parseErrorMessage_fallsBackToStatusCode() {
        val message = AuthApiResponseParser.parseErrorMessage(
            body = "not-json",
            statusCode = 500
        )

        assertTrue(message.contains("500"))
    }
}
