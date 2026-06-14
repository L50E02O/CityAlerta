package ec.cityalerta.app.model.data.geoJson

import kotlinx.serialization.Serializable

@Serializable
data class Geometry(
    val type: String,
    val coordinates: List<List<List<Double>>>
)
