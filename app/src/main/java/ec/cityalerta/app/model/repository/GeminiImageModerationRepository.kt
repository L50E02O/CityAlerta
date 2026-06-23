package ec.cityalerta.app.model.repository

import android.graphics.BitmapFactory
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import ec.cityalerta.app.BuildConfig
import ec.cityalerta.app.model.data.contracts.moderation.ImageModerationContract
import ec.cityalerta.app.model.data.contracts.moderation.ModerationResult

class GeminiImageModerationRepository : ImageModerationContract {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-latest",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    override suspend fun analyzeContent(imageBytes: ByteArray, description: String): Result<ModerationResult> {
        return runCatching {
            android.util.Log.d("GeminiModeration", "Iniciando análisis de IA para Imagen y Texto...")
            
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ?: throw Exception("No se pudo decodificar la imagen")

            val prompt = """
                Actúa como un moderador de contenido estricto.
                Analiza minuciosamente el siguiente contenido:
                
                DESCRIPCIÓN DEL USUARIO: "$description"
                
                REGLAS DE MODERACIÓN:
                1. IMAGEN: Marcar como FLAGGED si contiene sangre, heridas, violencia, accidentes gráficos, contenido sexual o material perturbador. De lo contrario, SAFE.
                2. DESCRIPCIÓN: Marcar como FLAGGED si contiene vulgaridades, insultos, lenguaje de odio o enlaces sospechosos. De lo contrario, SAFE.
                
                Responde ÚNICAMENTE en este formato:
                IMAGE:[SAFE o FLAGGED]
                TEXT:[SAFE o FLAGGED]
            """.trimIndent()

            val response = generativeModel.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )

            val textResponse = response.text?.trim()?.uppercase() ?: ""
            android.util.Log.d("GeminiModeration", "IA Response:\n$textResponse")

            val imageFlagged = textResponse.contains("IMAGE:FLAGGED")
            val descriptionFlagged = textResponse.contains("TEXT:FLAGGED")
            
            ModerationResult(
                isSafe = !imageFlagged && !descriptionFlagged,
                imageFlagged = imageFlagged,
                descriptionFlagged = descriptionFlagged
            )
        }
    }
}