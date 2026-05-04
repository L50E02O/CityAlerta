package ec.cityalerta.app.model.data.ciudad

import ec.cityalerta.app.model.data.geoJson.GeoJson

data class CiudadCreateDto(
    val nombre: String,
    val pais: String,
    val geojson: GeoJson,
    val centroLat: Double,
    val centroLng: Double
)

