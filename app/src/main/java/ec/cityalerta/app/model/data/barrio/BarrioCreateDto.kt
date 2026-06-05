package ec.cityalerta.app.model.data.barrio

import ec.cityalerta.app.model.data.geoJson.Geometry

data class BarrioCreateDto (
    val ciudadId: String,
    val nombre: String,
    val perimetro: Geometry
)