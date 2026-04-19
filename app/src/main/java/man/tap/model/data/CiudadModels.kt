package man.tap.model.data

// Modelos principales
data class Ciudad(
    val id: String,
    val nombre: String,
    val pais: String,
    val geojson: GeoJson,
    val centro_lat: Double,
    val centro_lng: Double
)

// Modelos GeoJSON
data class GeoJson(
    val type: String,
    val features: List<Feature>
)

data class Feature(
    val type: String,
    val properties: Properties,
    val geometry: Geometry
)

data class Geometry(
    val type: String,
    val coordinates: List<List<List<Double>>>
)

data class Properties(
    val name: String,
    val country: String
)

// Modelo para marcadores en el mapa
data class MapMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val description: String? = null
)

