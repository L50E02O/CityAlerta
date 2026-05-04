package ec.cityalerta.app.model.data.reporteubicacion

data class ReporteUbicacionCreateDto(
    val lat: Double,
    val lng: Double,
    val direccionAproximada: String
)

