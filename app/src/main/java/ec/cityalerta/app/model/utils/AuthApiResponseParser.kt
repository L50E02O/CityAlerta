package ec.cityalerta.app.model.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object AuthApiResponseParser {
    private val errorFieldPriority = listOf("error", "msg", "error_description")

    fun parseErrorMessage(body: String, statusCode: Int): String {
        val fromJson = runCatching {
            val jsonObject = Json.parseToJsonElement(body).jsonObject
            errorFieldPriority.firstNotNullOfOrNull { field ->
                jsonObject[field]
                    ?.jsonPrimitive
                    ?.content
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
            }
        }.getOrNull()

        return fromJson ?: "No se pudo completar la operacion ($statusCode)"
    }
}
