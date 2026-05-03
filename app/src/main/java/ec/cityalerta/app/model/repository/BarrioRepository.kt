package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.Barrio
import ec.cityalerta.app.model.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class BarrioRepository : ICrudRepository<Barrio> {

    private val tableName = "barrio"

    override suspend fun create(entity: Barrio): Result<Barrio> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName).insert(entity.toJson())
        entity
    }

    override suspend fun update(entity: Barrio): Result<Barrio> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName).update(entity.toJson()) {
            filter {
                eq("id", entity.id)
            }
        }
        entity
    }

    override suspend fun getAll(): Result<List<Barrio>> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName)
            .select(Columns.ALL)
            .decodeList<JsonObject>()
            .map { it.toBarrio() }
    }

    override suspend fun getById(id: String): Result<Barrio?> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName)
            .select(Columns.ALL) {
                filter {
                    eq("id", id)
                }
            }
            .decodeList<JsonObject>()
            .firstOrNull()
            ?.toBarrio()
    }

    override suspend fun delete(id: String): Result<Unit> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName).delete {
            filter {
                eq("id", id)
            }
        }
        Unit
    }

    private fun Barrio.toJson(): JsonObject {
        return JsonObject(
            mapOf(
                "id" to JsonPrimitive(id),
                "ciudad_id" to JsonPrimitive(ciudadId),
                "nombre" to JsonPrimitive(nombre),
                "nivel_peligrosidad" to JsonPrimitive(nivelPeligrosidad),
                "perimetro" to geoJsonToJson(perimetro),
                "created_at" to (createdAt?.let { JsonPrimitive(it) } ?: JsonNull),
                "updated_at" to (updatedAt?.let { JsonPrimitive(it) } ?: JsonNull)
            )
        )
    }

    private fun JsonObject.toBarrio(): Barrio {
        return Barrio(
            id = stringOrEmpty("id"),
            ciudadId = nullableString("ciudad_id") ?: stringOrEmpty("ciudadId"),
            nombre = stringOrEmpty("nombre"),
            nivelPeligrosidad = nullableString("nivel_peligrosidad") ?: stringOrEmpty("nivelPeligrosidad"),
            perimetro = geoJsonFromJson(this["perimetro"] ?: this["geojson"]),
            createdAt = nullableString("created_at") ?: nullableString("createdAt"),
            updatedAt = nullableString("updated_at") ?: nullableString("updatedAt")
        )
    }
}

