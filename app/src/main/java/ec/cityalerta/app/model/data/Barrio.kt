package ec.cityalerta.app.model.data

import ec.cityalerta.app.model.data.geoJson.GeoJson

data class Barrio(
    val id: String,
    val ciudadId: String,
    val nombre: String,
    val nivelPeligrosidad: String,
    val perimetro: GeoJson,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
