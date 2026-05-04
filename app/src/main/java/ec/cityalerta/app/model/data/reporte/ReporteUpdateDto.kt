package ec.cityalerta.app.model.data.reporte

data class ReporteUpdateDto(
    val usuarioId: String? = null,
    val ciudadId: String? = null,
    val ubicacionId: String? = null,
    val descripcion: String? = null,
    val estado: ReporteEstado? = null,
    val fechaReporte: String? = null,
    val categoria: String? = null
)

