package ec.cityalerta.app.model.data.reporte

data class ReporteCreateDto(
    val usuarioId: String,
    val ciudadId: String,
    val ubicacionId: String,
    val descripcion: String,
    val estado: ReporteEstado,
    val fechaReporte: String,
    val categoria: ReportType
)

