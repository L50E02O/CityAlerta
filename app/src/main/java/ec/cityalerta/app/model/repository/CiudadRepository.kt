package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.Ciudad
import ec.cityalerta.app.model.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

class CiudadRepository : ICrudRepository<Ciudad> {

    private val tableName = "ciudad"

    override suspend fun create(entity: Ciudad): Result<Ciudad> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName).insert(entity.toJson())
        entity
    }

    override suspend fun update(entity: Ciudad): Result<Ciudad> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName).update(entity.toJson()) {
            filter {
                eq("id", entity.id)
            }
        }
        entity
    }

    override suspend fun getAll(): Result<List<Ciudad>> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName)
            .select(Columns.ALL)
            .decodeList<JsonObject>()
            .map { it.toCiudad() }
    }

    override suspend fun getById(id: String): Result<Ciudad?> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName)
            .select(Columns.ALL) {
                filter {
                    eq("id", id)
                }
            }
            .decodeList<JsonObject>()
            .firstOrNull()
            ?.toCiudad()
    }

    override suspend fun delete(id: String): Result<Unit> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName).delete {
            filter {
                eq("id", id)
            }
        }
        Unit
    }

    private fun Ciudad.toJson(): JsonObject {
        return JsonObject(
            mapOf(
                "id" to JsonPrimitive(id),
                "nombre" to JsonPrimitive(nombre),
                "pais" to JsonPrimitive(pais),
                "geojson" to geoJsonToJson(geojson),
                "centro_lat" to JsonPrimitive(centroLat),
                "centro_lng" to JsonPrimitive(centroLng),
                "created_at" to (createdAt?.let { JsonPrimitive(it) } ?: JsonNull),
                "updated_at" to (updatedAt?.let { JsonPrimitive(it) } ?: JsonNull)
            )
        )
    }

    private fun JsonObject.toCiudad(): Ciudad {
        return Ciudad(
            id = stringOrEmpty("id"),
            nombre = stringOrEmpty("nombre"),
            pais = stringOrEmpty("pais"),
            geojson = geoJsonFromJson(this["geojson"]),
            centroLat = this["centro_lat"]?.jsonPrimitive?.doubleOrNull
                ?: this["centroLat"]?.jsonPrimitive?.doubleOrNull
                ?: 0.0,
            centroLng = this["centro_lng"]?.jsonPrimitive?.doubleOrNull
                ?: this["centroLng"]?.jsonPrimitive?.doubleOrNull
                ?: 0.0,
            createdAt = nullableString("created_at") ?: nullableString("createdAt"),
            updatedAt = nullableString("updated_at") ?: nullableString("updatedAt")
        )
    }
}


