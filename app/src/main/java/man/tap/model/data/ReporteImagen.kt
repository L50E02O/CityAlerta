package man.tap.model.data

data class ReporteImagen(
    val id: String,
    val reporteId: String,
    val storageUuid: String,
    val urlPath: String,
    val createdAt: String? = null
)
