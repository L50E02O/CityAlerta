package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.data.ciudad.CiudadCreateDto
import ec.cityalerta.app.model.data.ciudad.CiudadUpdateDto
import ec.cityalerta.app.model.data.geoJson.Geometry
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.data.contracts.crud.CrudRepositoryContract
import ec.cityalerta.app.model.utils.nullableString
import ec.cityalerta.app.model.utils.safeSupabaseCall
import ec.cityalerta.app.model.utils.stringOrEmpty
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CiudadRepository : CrudRepositoryContract<Ciudad, CiudadCreateDto, CiudadUpdateDto> {

    private val tableName = "ciudad"

    override suspend fun create(entity: CiudadCreateDto): Result<Ciudad> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
            val newId = SupabaseProvider.client.postgrest.rpc(
                "create_ciudad",
                mapOf(
                    "p_nombre" to entity.nombre,
                    "p_pais" to entity.pais,
                    "p_geojson" to entity.geojson.toGeoJsonObject(),
                    "p_centro_lat" to entity.centroLat,
                    "p_centro_lng" to entity.centroLng
                )
            ).decodeAs<String>()

            getById(newId).getOrNull() ?: throw Exception("Error al recuperar ciudad creada")
        }
    }

    override suspend fun update(entity: CiudadUpdateDto, id: String): Result<Ciudad> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
            SupabaseProvider.client.postgrest.rpc(
                "update_ciudad",
                mapOf(
                    "p_id" to id,
                    "p_nombre" to entity.nombre,
                    "p_pais" to entity.pais,
                    "p_geojson" to entity.geojson?.toGeoJsonObject(),
                    "p_centro_lat" to entity.centroLat,
                    "p_centro_lng" to entity.centroLng
                )
            )

            getById(id).getOrNull() ?: throw Exception("Error al recuperar ciudad actualizada")
        }
    }

    override suspend fun getAll(): Result<List<Ciudad>> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
            SupabaseProvider.client.postgrest.rpc("get_ciudades")
                .decodeList<JsonObject>()
                .map { it.toCiudad() }
        }
    }

    suspend fun getAllByCountry(country: String): Result<List<Ciudad>> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
            SupabaseProvider.client.from(tableName)
                .select(Columns.ALL) {
                    filter {
                        eq("pais", country)
                    }
                }
                .decodeList<JsonObject>()
                .map { it.toCiudad() }
        }
    }

    override suspend fun getById(id: String): Result<Ciudad?> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
            SupabaseProvider.client.postgrest.rpc(
                "get_ciudad_by_id",
                mapOf("p_id" to id)
            )
                .decodeList<JsonObject>()
                .firstOrNull()
                ?.toCiudad()
        }
    }

    override suspend fun delete(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
            SupabaseProvider.client.postgrest.rpc(
                "delete_ciudad",
                mapOf("p_id" to id)
            ).let { }
        }
    }

    private fun JsonObject.toCiudad(): Ciudad {
        val geojsonElement = this["geojson"]
        val geometry = if (geojsonElement != null && geojsonElement is JsonObject) {
            geojsonElement.toGeometry()
        } else {
            // Default a un Polígono vacío si es NULL o no es un objeto
            Geometry("Polygon", emptyList())
        }

        return Ciudad(
            id = stringOrEmpty("id"),
            nombre = stringOrEmpty("nombre"),
            pais = stringOrEmpty("pais"),
            geojson = geometry,
            centroLat = this["centro_lat"]?.jsonPrimitive?.content?.toDoubleOrNull()
                ?: this["centroLat"]?.jsonPrimitive?.content?.toDoubleOrNull()
                ?: 0.0,
            centroLng = this["centro_lng"]?.jsonPrimitive?.content?.toDoubleOrNull()
                ?: this["centroLng"]?.jsonPrimitive?.content?.toDoubleOrNull()
                ?: 0.0,
            createdAt = nullableString("created_at") ?: nullableString("createdAt"),
            updatedAt = nullableString("updated_at") ?: nullableString("updatedAt")
        )
    }

    private fun Geometry.toGeoJsonObject(): JsonObject {
        // Asegurar que si tenemos coordenadas, el tipo sea Polygon o similar, no FeatureCollection
        val effectiveType = if (type == "FeatureCollection" && coordinates.isNotEmpty()) "Polygon" else type
        
        val coordinatesJson = JsonArray(
            coordinates.map { ring ->
                JsonArray(
                    ring.map { coord ->
                        JsonArray(coord.map { value -> JsonPrimitive(value) })
                    }
                )
            }
        )

        return JsonObject(
            mapOf(
                "type" to JsonPrimitive(effectiveType),
                "coordinates" to coordinatesJson
            )
        )
    }

    private fun JsonObject.toGeometry(): Geometry {
        val type = this["type"]?.jsonPrimitive?.content ?: "Polygon"
        val coordinatesElement = this["coordinates"]?.jsonArray

        val coordinates = if (type == "MultiPolygon") {
            coordinatesElement?.get(0)?.jsonArray?.map { ringElement ->
                ringElement.jsonArray.map { coordElement ->
                    coordElement.jsonArray.map { valueElement ->
                        valueElement.jsonPrimitive.content.toDouble()
                    }
                }
            } ?: emptyList()
        } else {
            coordinatesElement?.map { ringElement ->
                ringElement.jsonArray.map { coordElement ->
                    coordElement.jsonArray.map { valueElement ->
                        valueElement.jsonPrimitive.content.toDouble()
                    }
                }
            } ?: emptyList()
        }

        return Geometry(type = "Polygon", coordinates = coordinates)
    }
}


