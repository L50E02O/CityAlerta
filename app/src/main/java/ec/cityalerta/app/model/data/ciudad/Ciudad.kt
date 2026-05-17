package ec.cityalerta.app.model.data.ciudad

import ec.cityalerta.app.model.data.geoJson.Geometry

data class Ciudad(
    val id: String,
    val nombre: String,
    val pais: String,
    val geojson: Geometry,
    val centroLat: Double,
    val centroLng: Double,
    val createdAt: String? = null,
    val updatedAt: String? = null
)