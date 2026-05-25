package ec.cityalerta.app.model.data.barrio

import ec.cityalerta.app.model.data.geoJson.Geometry

data class BarrioUpdateDto(
    val ciudadId: String? = null,
    val nombre: String? = null,
    val perimetro: Geometry? = null
)