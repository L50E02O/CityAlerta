package man.tap.model.data

data class GeoJson(
    val type: String,
    val features: List<Feature>
)
