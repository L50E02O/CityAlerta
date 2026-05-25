package ec.cityalerta.app.model.data.barrio

import ec.cityalerta.app.model.data.geoJson.Geometry

data class Barrio(
    val id: String,
    val ciudadId: String,
    val nombre: String,
    val perimetro: Geometry,
    val createdAt: String? = null,
    val updatedAt: String? = null
)