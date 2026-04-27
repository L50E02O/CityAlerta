package ec.cityalerta.app.model.data

data class MapMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val description: String? = null
)
