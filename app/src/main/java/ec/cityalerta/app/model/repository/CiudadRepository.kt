package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.data.ciudad.CiudadCreateDto
import ec.cityalerta.app.model.data.ciudad.CiudadUpdateDto
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
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

class CiudadRepository : ICrudRepository<Ciudad, CiudadCreateDto, CiudadUpdateDto> {

    private val tableName = "ciudad"

    override suspend fun create(entity: CiudadCreateDto): Result<Ciudad> = safeSupabaseCall {
        val response = SupabaseProvider.client.from(tableName).insert(entity.toCreateJson())
            .decodeList<JsonObject>()
            .firstOrNull()
        response?.toCiudad() ?: throw Exception("Error al crear ciudad")
    }

    override suspend fun update(entity: CiudadUpdateDto, id: String): Result<Ciudad> = safeSupabaseCall {
        val response = SupabaseProvider.client.from(tableName).update(entity.toUpdateJson()) {
            filter {
                eq("id", id)
            }
        }
            .decodeList<JsonObject>()
            .firstOrNull()
        response?.toCiudad() ?: throw Exception("Error al actualizar ciudad")
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

    private fun CiudadCreateDto.toCreateJson(): JsonObject {
        return JsonObject(
            mapOf(
                "nombre" to JsonPrimitive(nombre),
                "pais" to JsonPrimitive(pais),
                "geojson" to geoJsonToJson(geojson),
                "centro_lat" to JsonPrimitive(centroLat),
                "centro_lng" to JsonPrimitive(centroLng)
            )
        )
    }

    private fun CiudadUpdateDto.toUpdateJson(): JsonObject {
        val map = mutableMapOf<String, JsonElement>()
        nombre?.let { map["nombre"] = JsonPrimitive(it) }
        pais?.let { map["pais"] = JsonPrimitive(it) }
        geojson?.let { map["geojson"] = geoJsonToJson(it) }
        centroLat?.let { map["centro_lat"] = JsonPrimitive(it) }
        centroLng?.let { map["centro_lng"] = JsonPrimitive(it) }
        return JsonObject(map)
    }
}


