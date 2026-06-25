package ec.cityalerta.app.model.repository

import android.graphics.BitmapFactory
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import ec.cityalerta.app.BuildConfig
import ec.cityalerta.app.model.data.contracts.moderation.ImageModerationContract
import ec.cityalerta.app.model.data.contracts.moderation.ModerationResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive

class GeminiImageModerationRepository : ImageModerationContract {

    private val jsonParser = Json { ignoreUnknownKeys = true }

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.1f
            topK = 1
            topP = 1f
            responseMimeType = "application/json"
        },
        safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.ONLY_HIGH),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.ONLY_HIGH),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.ONLY_HIGH)
        )
    )

    override suspend fun analyzeContent(imageBytes: ByteArray, description: String): Result<ModerationResult> {
        return runCatching {
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ?: throw Exception("No se pudo decodificar la imagen")

            val prompt = """
                ACTÚA COMO UN MODERADOR DE SEGURIDAD PARA REPORTES CIUDADANOS.
                
                CONTEXTO: El usuario reporta fallos en servicios públicos.
                REGLA CRÍTICA DE "APAGÓN": Si la imagen es totalmente oscura, negra o de baja calidad PERO el texto menciona "no tengo energía", "sin luz", "apagón", "oscuridad", "ayuda" o "no hay electricidad", DEBES marcar "image_flagged": false. Es una evidencia válida del problema.
                
                REGLAS DE BLOQUEO:
                - image_flagged: true solo para SEXUAL, SANGRE EXTREMA o TERRORISMO.
                - text_flagged: true solo para INSULTOS GRAVES o AMENAZAS.
                
                TEXTO DEL USUARIO: "$description"
                
                RESPUESTA EN JSON:
                {
                  "image_flagged": boolean,
                  "text_flagged": boolean,
                  "reason": "explicación"
                }
            """.trimIndent()

            val response = try {
                generativeModel.generateContent(content {
                    image(bitmap)
                    text(prompt)
                })
            } catch (e: Exception) {
                return@runCatching ModerationResult(isSafe = true)
            }

            val jsonResponse = response.text ?: return@runCatching ModerationResult(isSafe = true)
            
            val resultElement = jsonParser.parseToJsonElement(jsonResponse) as JsonObject
            val imageFlagged = resultElement["image_flagged"]?.jsonPrimitive?.boolean ?: false
            val descriptionFlagged = resultElement["text_flagged"]?.jsonPrimitive?.boolean ?: false
            
            ModerationResult(
                isSafe = !imageFlagged && !descriptionFlagged,
                imageFlagged = imageFlagged,
                descriptionFlagged = descriptionFlagged
            )
        }
    }
}
