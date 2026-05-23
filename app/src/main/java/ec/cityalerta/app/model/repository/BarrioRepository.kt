package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.barrio.Barrio
import ec.cityalerta.app.model.data.barrio.BarrioCreateDto
import ec.cityalerta.app.model.data.barrio.BarrioUpdateDto
import ec.cityalerta.app.model.data.geoJson.Geometry
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.data.contracts.crud.CrudRepositoryContract
import ec.cityalerta.app.model.utils.nullableString
import ec.cityalerta.app.model.utils.safeSupabaseCall
import ec.cityalerta.app.model.utils.stringOrEmpty
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive


class BarrioRepository : CrudRepositoryContract<Barrio, BarrioCreateDto, BarrioUpdateDto> {

    override suspend fun create(entity: BarrioCreateDto): Result<Barrio> = safeSupabaseCall {
        val newId = SupabaseProvider.client.postgrest.rpc(
            "create_barrio",
            mapOf(
                "p_ciudad_id" to entity.ciudadId,
                "p_nombre" to entity.nombre,
                "p_nivel_peligrosidad" to entity.nivelPeligrosidad,
                "p_geojson" to entity.perimetro.toGeoJsonObject()
            )
        ).decodeAs<String>()

        getById(newId).getOrNull() ?: throw Exception("Error al recuperar barrio creado")
    }

    override suspend fun update(entity: BarrioUpdateDto, id: String): Result<Barrio> = safeSupabaseCall {
        SupabaseProvider.client.postgrest.rpc(
            "update_barrio",
            mapOf(
                "p_id" to id,
                "p_nombre" to entity.nombre,
                "p_nivel_peligrosidad" to entity.nivelPeligrosidad,
                "p_geojson" to entity.perimetro?.toGeoJsonObject()
            )
        )

        getById(id).getOrNull() ?: throw Exception("Error al recuperar barrio actualizado")
    }

    override suspend fun getAll(): Result<List<Barrio>> = safeSupabaseCall {
        SupabaseProvider.client.postgrest.rpc("get_barrios")
            .decodeList<JsonObject>()
            .map { it.toBarrio() }
    }

    override suspend fun getById(id: String): Result<Barrio?> = safeSupabaseCall {
        SupabaseProvider.client.postgrest.rpc(
            "get_barrio_by_id",
            mapOf("p_id" to id)
        )
            .decodeList<JsonObject>()
            .firstOrNull()
            ?.toBarrio()
    }

    override suspend fun delete(id: String): Result<Unit> = safeSupabaseCall {
        SupabaseProvider.client.postgrest.rpc(
            "delete_barrio",
            mapOf("p_id" to id)
        )
        Unit
    }

    private fun JsonObject.toBarrio(): Barrio {
        val perimetroElement = this["perimetro"]
        val geometry = if (perimetroElement != null && perimetroElement is JsonObject) {
            perimetroElement.toGeometry()
        } else {
            Geometry("Polygon", emptyList())
        }

        return Barrio(
            id = stringOrEmpty("id"),
            ciudadId = nullableString("ciudad_id") ?: stringOrEmpty("ciudadId"),
            nombre = stringOrEmpty("nombre"),
            nivelPeligrosidad = nullableString("nivel_peligrosidad") ?: stringOrEmpty("nivelPeligrosidad"),
            perimetro = geometry,
            createdAt = nullableString("created_at") ?: nullableString("createdAt"),
            updatedAt = nullableString("updated_at") ?: nullableString("updatedAt")
        )
    }

    private fun JsonObject.toGeometry(): Geometry {
        val type = this["type"]?.jsonPrimitive?.content ?: "Polygon"
        val coordinates = this["coordinates"]?.jsonArray?.map { ringElement ->
            ringElement.jsonArray.map { coordElement ->
                coordElement.jsonArray.map { valueElement ->
                    valueElement.jsonPrimitive.content.toDouble()
                }
            }
        } ?: emptyList()

        return Geometry(type = type, coordinates = coordinates)
    }

    private fun Geometry.toGeoJsonObject(): JsonObject {
        val effectiveType = if (type == "FeatureCollection" && coordinates.isNotEmpty()) "Polygon" else type

        val coordinatesJson = JsonArray(
            this.coordinates.map { ring ->
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
}



