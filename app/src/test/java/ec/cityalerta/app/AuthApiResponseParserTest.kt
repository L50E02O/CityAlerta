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
    fun parseErrorMessage_prefersErrorOverMsg() {
        val message = AuthApiResponseParser.parseErrorMessage(
            body = """{"error":"Error principal","msg":"Mensaje secundario"}""",
            statusCode = 400
        )

        assertEquals("Error principal", message)
    }

    @Test
    fun parseErrorMessage_readsMsgFieldWhenErrorMissing() {
        val message = AuthApiResponseParser.parseErrorMessage(
            body = """{"msg":"Sesion invalida"}""",
            statusCode = 401
        )

        assertEquals("Sesion invalida", message)
    }

    @Test
    fun parseErrorMessage_readsErrorDescriptionWhenOthersMissing() {
        val message = AuthApiResponseParser.parseErrorMessage(
            body = """{"error_description":"Token expirado"}""",
            statusCode = 401
        )

        assertEquals("Token expirado", message)
    }

    @Test
    fun parseErrorMessage_ignoresBlankErrorField() {
        val message = AuthApiResponseParser.parseErrorMessage(
            body = """{"error":"   ","msg":"Mensaje valido"}""",
            statusCode = 422
        )

        assertEquals("Mensaje valido", message)
    }

    @Test
    fun parseErrorMessage_ignoresBlankMsgField() {
        val message = AuthApiResponseParser.parseErrorMessage(
            body = """{"msg":"  ","error_description":"Descripcion valida"}""",
            statusCode = 403
        )

        assertEquals("Descripcion valida", message)
    }

    @Test
    fun parseErrorMessage_fallsBackToStatusCodeForEmptyJson() {
        val message = AuthApiResponseParser.parseErrorMessage(
            body = "{}",
            statusCode = 500
        )

        assertEquals("No se pudo completar la operacion (500)", message)
    }

    @Test
    fun parseErrorMessage_fallsBackToStatusCodeForInvalidJson() {
        val message = AuthApiResponseParser.parseErrorMessage(
            body = "not-json",
            statusCode = 503
        )

        assertEquals("No se pudo completar la operacion (503)", message)
    }

    @Test
    fun parseErrorMessage_fallsBackWhenAllKnownFieldsAreBlank() {
        val message = AuthApiResponseParser.parseErrorMessage(
            body = """{"error":"","msg":" ","error_description":"\t"}""",
            statusCode = 409
        )

        assertTrue(message.contains("409"))
    }
}
