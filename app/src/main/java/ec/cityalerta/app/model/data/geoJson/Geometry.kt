package ec.cityalerta.app.model.data.geoJson

data class Geometry(
    val type: String,
    val coordinates: List<List<List<Double>>>
)
