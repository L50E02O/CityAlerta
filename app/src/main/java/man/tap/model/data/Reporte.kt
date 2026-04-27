package man.tap.model.data

data class Reporte(
    val id: String,
    val usuarioId: String,
    val ciudadId: String,
    val ubicacionId: String,
    val descripcion: String,
    val estado: ReporteEstado,
    val fechaReporte: String,
    val categoria: String,
    val updatedAt: String? = null
)
