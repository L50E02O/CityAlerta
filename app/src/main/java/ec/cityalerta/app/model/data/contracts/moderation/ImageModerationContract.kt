package ec.cityalerta.app.model.data.contracts.moderation

data class ModerationResult(
    val isSafe: Boolean,
    val imageFlagged: Boolean = false,
    val descriptionFlagged: Boolean = false
)

interface ImageModerationContract {
    /**
     * Valida si el contenido de un reporte es apto para ser publicado.
     * @param imageBytes Los bytes de la imagen a analizar.
     * @param description El texto descriptivo a analizar.
     * @return Result con el detalle de la moderación, o falla si hay error técnico.
     */
    suspend fun analyzeContent(imageBytes: ByteArray, description: String): Result<ModerationResult>
}