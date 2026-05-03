package ec.cityalerta.app.model.utils

import ec.cityalerta.app.model.data.ReporteEstado
import ec.cityalerta.app.model.data.geoJson.Feature
import ec.cityalerta.app.model.data.geoJson.GeoJson
import ec.cityalerta.app.model.data.geoJson.Geometry
import ec.cityalerta.app.model.data.geoJson.Properties
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.cancellation.CancellationException

internal suspend inline fun <T> safeSupabaseCall(crossinline block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}

internal fun JsonObject.stringOrEmpty(key: String): String =
    this[key].asStringOrNull().orEmpty()

internal fun JsonObject.doubleOrZero(key: String): Double =
    this[key]?.jsonPrimitive?.doubleOrNull ?: 0.0

internal fun JsonObject.booleanOrFalse(key: String): Boolean =
    this[key]?.jsonPrimitive?.booleanOrNull ?: false

internal fun JsonObject.nullableString(key: String): String? =
    this[key].asStringOrNull()

internal fun JsonObject.toReporteEstadoOrDefault(key: String): ReporteEstado {
    val estado = stringOrEmpty(key)
    return ReporteEstado.entries.firstOrNull { it.name == estado } ?: ReporteEstado.PENDIENTE
}

private fun JsonElement?.asStringOrNull(): String? {
    return when (this) {
        null, JsonNull -> null
        is JsonPrimitive -> content
        else -> null
    }
}

internal fun geoJsonToJson(geoJson: GeoJson): JsonObject {
    val features = geoJson.features.map { feature ->
        JsonObject(
            mapOf(
                "type" to JsonPrimitive(feature.type),
                "properties" to JsonObject(
                    mapOf(
                        "name" to JsonPrimitive(feature.properties.name),
                        "country" to JsonPrimitive(feature.properties.country)
                    )
                ),
                "geometry" to JsonObject(
                    mapOf(
                        "type" to JsonPrimitive(feature.geometry.type),
                        "coordinates" to JsonArray(
                            feature.geometry.coordinates.map { ring ->
                                JsonArray(
                                    ring.map { coordinate ->
                                        JsonArray(coordinate.map { value -> JsonPrimitive(value) })
                                    }
                                )
                            }
                        )
                    )
                )
            )
        )
    }

    return JsonObject(
        mapOf(
            "type" to JsonPrimitive(geoJson.type),
            "features" to JsonArray(features)
        )
    )
}

internal fun geoJsonFromJson(element: JsonElement?): GeoJson {
    if (element == null || element is JsonNull) {
        return GeoJson(type = "FeatureCollection", features = emptyList())
    }

    val json = element.jsonObject
    val type = json["type"].asStringOrNull() ?: "FeatureCollection"
    val features = json["features"]?.jsonArray.orEmpty().map { featureElement ->
        val featureObject = featureElement.jsonObject
        val propertiesObject = featureObject["properties"]?.jsonObject
        val geometryObject = featureObject["geometry"]?.jsonObject

        val coordinates = geometryObject
            ?.get("coordinates")
            ?.jsonArray
            .orEmpty()
            .map { ringElement ->
                ringElement.jsonArray.map { coordElement ->
                    coordElement.jsonArray.map { valueElement ->
                        valueElement.jsonPrimitive.doubleOrNull ?: 0.0
                    }
                }
            }

        Feature(
            type = featureObject["type"].asStringOrNull() ?: "Feature",
            properties = Properties(
                name = propertiesObject?.get("name").asStringOrNull().orEmpty(),
                country = propertiesObject?.get("country").asStringOrNull().orEmpty()
            ),
            geometry = Geometry(
                type = geometryObject?.get("type").asStringOrNull() ?: "Polygon",
                coordinates = coordinates
            )
        )
    }

    return GeoJson(type = type, features = features)
}


