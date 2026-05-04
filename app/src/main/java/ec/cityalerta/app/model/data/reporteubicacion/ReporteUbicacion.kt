package ec.cityalerta.app.model.data.reporteubicacion

data class ReporteUbicacion(
    val id: String,
    val lat: Double,
    val lng: Double,
    val direccionAproximada: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)