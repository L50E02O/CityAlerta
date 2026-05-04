package ec.cityalerta.app.model.data.ciudad

import ec.cityalerta.app.model.data.geoJson.GeoJson

data class Ciudad(
    val id: String,
    val nombre: String,
    val pais: String,
    val geojson: GeoJson,
    val centroLat: Double,
    val centroLng: Double,
    val createdAt: String? = null,
    val updatedAt: String? = null
)