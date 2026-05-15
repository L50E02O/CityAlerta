package ec.cityalerta.app.model.data.reporte

data class ReporteCreateDto(
    val usuario_id: String,
    val ciudad_id: String,
    val ubicacion_id: String,
    val descripcion: String,
    val estado: ReporteEstado,
    val fecha_reporte: String,
    val categoria: ReportType,
    val barrio_id: String
)

