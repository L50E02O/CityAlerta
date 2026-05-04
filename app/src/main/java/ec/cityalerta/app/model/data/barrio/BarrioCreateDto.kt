package ec.cityalerta.app.model.data.barrio

import ec.cityalerta.app.model.data.geoJson.GeoJson

data class BarrioCreateDto (
    val ciudadId: String,
    val nombre: String,
    val nivelPeligrosidad: String,
    val perimetro: GeoJson
)