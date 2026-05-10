package ec.cityalerta.app.model.data.reporteubicacion

data class ReporteUbicacion(
    val id: String,
    val lat: Double,
    val lng: Double,
    val direccion_aproximada: String,
    val created_at: String? = null,
    val updated_at: String? = null
)