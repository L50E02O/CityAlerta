package ec.cityalerta.app.model.data.reporte

data class ReporteUpdateDto(
    val usuario_id: String? = null,
    val ciudad_id: String? = null,
    val ubicacion_id: String? = null,
    val descripcion: String? = null,
    val estado: ReporteEstado? = null,
    val fecha_reporte: String? = null,
    val categoria: ReportType? = null,
    val barrio_id: String? = null
)

