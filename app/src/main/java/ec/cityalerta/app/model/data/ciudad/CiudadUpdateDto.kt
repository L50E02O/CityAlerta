package ec.cityalerta.app.model.data.ciudad

import ec.cityalerta.app.model.data.geoJson.GeoJson

data class CiudadUpdateDto(
    val nombre: String? = null,
    val pais: String? = null,
    val geojson: GeoJson? = null,
    val centroLat: Double? = null,
    val centroLng: Double? = null
)

