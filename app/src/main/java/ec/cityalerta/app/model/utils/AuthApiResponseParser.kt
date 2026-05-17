package ec.cityalerta.app.model.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object AuthApiResponseParser {
    fun parseErrorMessage(body: String, statusCode: Int): String {
        val fromJson = runCatching {
            Json.parseToJsonElement(body)
                .jsonObject["error"]
                ?.jsonPrimitive
                ?.content
                ?: Json.parseToJsonElement(body)
                    .jsonObject["msg"]
                    ?.jsonPrimitive
                    ?.content
                ?: Json.parseToJsonElement(body)
                    .jsonObject["error_description"]
                    ?.jsonPrimitive
                    ?.content
        }.getOrNull()

        return fromJson?.takeIf { it.isNotBlank() }
            ?: "No se pudo completar la operacion ($statusCode)"
    }
}
