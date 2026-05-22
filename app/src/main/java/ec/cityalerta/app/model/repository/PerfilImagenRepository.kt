package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.contracts.crud.CrudRepositoryContract
import ec.cityalerta.app.model.data.perfilimagen.PerfilImagen
import ec.cityalerta.app.model.data.perfilimagen.PerfilImagenCreateDto
import ec.cityalerta.app.model.data.perfilimagen.PerfilImagenUpdateDto
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.utils.nullableString
import ec.cityalerta.app.model.utils.safeSupabaseCall
import ec.cityalerta.app.model.utils.stringOrEmpty
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class PerfilImagenRepository : CrudRepositoryContract<PerfilImagen, PerfilImagenCreateDto, PerfilImagenUpdateDto> {

    private val tableName = "perfil_imagen"

    override suspend fun create(entity: PerfilImagenCreateDto): Result<PerfilImagen> = safeSupabaseCall {
        val response = SupabaseProvider.client.from(tableName)
            .insert(entity.toCreateJson()) {
                select()
            }
            .decodeList<JsonObject>()
            .firstOrNull()
        response?.toPerfilImagen() ?: throw Exception("Error al crear perfil imagen")
    }

    override suspend fun update(entity: PerfilImagenUpdateDto, id: String): Result<PerfilImagen> = safeSupabaseCall {
        val response = SupabaseProvider.client.from(tableName).update(entity.toUpdateJson()) {
            filter {
                eq("id", id)
            }
            select()
        }
            .decodeList<JsonObject>()
            .firstOrNull()
        response?.toPerfilImagen() ?: throw Exception("Error al actualizar perfil imagen")
    }

    override suspend fun getAll(): Result<List<PerfilImagen>> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName)
            .select(Columns.ALL)
            .decodeList<JsonObject>()
            .map { it.toPerfilImagen() }
    }

    override suspend fun getById(id: String): Result<PerfilImagen?> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName)
            .select(Columns.ALL) {
                filter {
                    eq("id", id)
                }
            }
            .decodeList<JsonObject>()
            .firstOrNull()
            ?.toPerfilImagen()
    }

    override suspend fun delete(id: String): Result<Unit> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName).delete {
            filter {
                eq("id", id)
            }
        }
        Unit
    }

    suspend fun getImagenByPerfilId(perfilId: String): Result<PerfilImagen?> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName)
            .select(Columns.ALL) {
                filter {
                    eq("perfil_id", perfilId)
                }
            }
            .decodeList<JsonObject>()
            .firstOrNull()
            ?.toPerfilImagen()
    }

    private fun JsonObject.toPerfilImagen(): PerfilImagen {
        return PerfilImagen(
            id = stringOrEmpty("id"),
            perfil_id = nullableString("perfil_id") ?: stringOrEmpty("perfilId"),
            storage_uuid = nullableString("storage_uuid") ?: stringOrEmpty("storageUuid"),
            url_path = nullableString("url_path") ?: stringOrEmpty("urlPath"),
            created_at = nullableString("created_at") ?: nullableString("createdAt"),
            updated_at = nullableString("updated_at") ?: nullableString("updatedAt")
        )
    }

    private fun PerfilImagenCreateDto.toCreateJson(): JsonObject {
        return JsonObject(
            mapOf(
                "perfil_id" to JsonPrimitive(perfil_id),
                "storage_uuid" to JsonPrimitive(storage_uuid),
                "url_path" to JsonPrimitive(url_path)
            )
        )
    }

    private fun PerfilImagenUpdateDto.toUpdateJson(): JsonObject {
        val map = mutableMapOf<String, JsonElement>()
        perfil_id?.let { map["perfil_id"] = JsonPrimitive(it) }
        storage_uuid?.let { map["storage_uuid"] = JsonPrimitive(it) }
        url_path?.let { map["url_path"] = JsonPrimitive(it) }
        return JsonObject(map)
    }
}
