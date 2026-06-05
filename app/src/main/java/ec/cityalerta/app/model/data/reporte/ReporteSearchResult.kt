package ec.cityalerta.app.model.data.reporte

data class ReporteSearchResult(
    val reporte: Reporte,
    val barrioNombre: String,
    val direccionAproximada: String?
)
