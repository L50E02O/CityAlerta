package ec.cityalerta.app.model.data.barrio

import ec.cityalerta.app.model.data.geoJson.GeoJson

data class BarrioUpdateDto(
    val ciudadId: String? = null,
    val nombre: String? = null,
    val nivelPeligrosidad: String? = null,
    val perimetro: GeoJson? = null
)