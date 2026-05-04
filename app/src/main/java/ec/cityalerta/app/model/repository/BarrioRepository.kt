package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.barrio.Barrio
import ec.cityalerta.app.model.data.barrio.BarrioCreateDto
import ec.cityalerta.app.model.data.barrio.BarrioUpdateDto
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.repository.interfaces.ICrudRepository
import ec.cityalerta.app.model.utils.geoJsonFromJson
import ec.cityalerta.app.model.utils.geoJsonToJson
import ec.cityalerta.app.model.utils.nullableString
import ec.cityalerta.app.model.utils.safeSupabaseCall
import ec.cityalerta.app.model.utils.stringOrEmpty
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonElement

class BarrioRepository : ICrudRepository<Barrio, BarrioCreateDto, BarrioUpdateDto> {

    private val tableName = "barrio"

    override suspend fun create(entity: BarrioCreateDto): Result<Barrio> = safeSupabaseCall {
        val response = SupabaseProvider.client.from(tableName).insert(entity.toCreateJson())
            .decodeList<JsonObject>()
            .firstOrNull()
        response?.toBarrio() ?: throw Exception("Error al crear barrio")
    }

    override suspend fun update(entity: BarrioUpdateDto, id: String): Result<Barrio> = safeSupabaseCall {
        val response = SupabaseProvider.client.from(tableName).update(entity.toUpdateJson()) {
            filter {
                eq("id", id)
            }
        }
            .decodeList<JsonObject>()
            .firstOrNull()
        response?.toBarrio() ?: throw Exception("Error al actualizar barrio")
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

    private fun BarrioCreateDto.toCreateJson(): JsonObject {
        return JsonObject(
            mapOf(
                "ciudad_id" to JsonPrimitive(ciudadId),
                "nombre" to JsonPrimitive(nombre),
                "nivel_peligrosidad" to JsonPrimitive(nivelPeligrosidad),
                "perimetro" to geoJsonToJson(perimetro)
            )
        )
    }

    private fun BarrioUpdateDto.toUpdateJson(): JsonObject {
        val map = mutableMapOf<String, JsonElement>()
        ciudadId?.let { map["ciudad_id"] = JsonPrimitive(it) }
        nombre?.let { map["nombre"] = JsonPrimitive(it) }
        nivelPeligrosidad?.let { map["nivel_peligrosidad"] = JsonPrimitive(it) }
        perimetro?.let { map["perimetro"] = geoJsonToJson(it) }
        return JsonObject(map)
    }
}

