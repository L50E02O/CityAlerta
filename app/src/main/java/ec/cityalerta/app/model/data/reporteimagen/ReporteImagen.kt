package ec.cityalerta.app.model.data.reporteimagen

data class ReporteImagen(
    val id: String,
    val reporteId: String,
    val storageUuid: String,
    val urlPath: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)