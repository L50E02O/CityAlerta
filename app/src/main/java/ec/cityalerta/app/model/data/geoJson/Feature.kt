package ec.cityalerta.app.model.data.geoJson

data class Feature(
    val type: String,
    val properties: Properties,
    val geometry: Geometry
)
