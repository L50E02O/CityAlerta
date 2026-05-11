package ec.cityalerta.app.model.data.reporte

data class Reporte(
    val id: String,
    val usuario_id: String,
    val ciudad_id: String,
    val ubicacion_id: String,
    val descripcion: String,
    val estado: ReporteEstado,
    val fecha_reporte: String,
    val categoria: ReportType,
    val created_at: String? = null,
    val updated_at: String? = null,
    val barrio_id: String
)