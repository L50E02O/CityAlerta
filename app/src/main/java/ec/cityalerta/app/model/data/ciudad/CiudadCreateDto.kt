package ec.cityalerta.app.model.data.ciudad

import ec.cityalerta.app.model.data.geoJson.Geometry

data class CiudadCreateDto(
    val nombre: String,
    val pais: String,
    val geojson: Geometry,
    val centroLat: Double,
    val centroLng: Double
)

