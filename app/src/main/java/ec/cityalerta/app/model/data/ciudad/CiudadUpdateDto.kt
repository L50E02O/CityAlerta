package ec.cityalerta.app.model.data.ciudad

import ec.cityalerta.app.model.data.geoJson.Geometry

data class CiudadUpdateDto(
    val nombre: String? = null,
    val pais: String? = null,
    val geojson: Geometry? = null,
    val centroLat: Double? = null,
    val centroLng: Double? = null
)

