package ec.cityalerta.app.model.data.reporteimagen

data class ReporteImagen(
    val id: String,
    val reporte_id: String,
    val storage_uuid: String,
    val url_path: String,
    val created_at: String? = null,
    val updated_at: String? = null
)