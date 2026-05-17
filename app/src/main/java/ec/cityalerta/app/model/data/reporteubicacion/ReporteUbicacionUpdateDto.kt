package ec.cityalerta.app.model.data.reporteubicacion

data class ReporteUbicacionUpdateDto(
    val lat: Double? = null,
    val lng: Double? = null,
    val direccion_aproximada: String? = null
)

