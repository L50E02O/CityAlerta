package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.perfil.Perfil
import ec.cityalerta.app.model.data.perfil.PerfilCreateDto
import ec.cityalerta.app.model.data.perfil.PerfilUpdateDto
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.repository.interfaces.ICrudRepository
import ec.cityalerta.app.model.utils.booleanOrFalse
import ec.cityalerta.app.model.utils.nullableString
import ec.cityalerta.app.model.utils.safeSupabaseCall
import ec.cityalerta.app.model.utils.stringOrEmpty
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonElement

class PerfilRepository : ICrudRepository<Perfil, PerfilCreateDto, PerfilUpdateDto> {

    private val tableName = "perfil"

    override suspend fun create(entity: PerfilCreateDto): Result<Perfil> = safeSupabaseCall {
        val response = SupabaseProvider.client.from(tableName).insert(entity.toCreateJson())
            .decodeList<JsonObject>()
            .firstOrNull()
        response?.toPerfil() ?: throw Exception("Error al crear perfil")
    }

    override suspend fun update(entity: PerfilUpdateDto, id: String): Result<Perfil> = safeSupabaseCall {
        val response = SupabaseProvider.client.from(tableName).update(entity.toUpdateJson()) {
            filter {
                eq("id", id)
            }
        }
            .decodeList<JsonObject>()
            .firstOrNull()
        response?.toPerfil() ?: throw Exception("Error al actualizar perfil")
    }

    override suspend fun getAll(): Result<List<Perfil>> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName)
            .select(Columns.ALL)
            .decodeList<JsonObject>()
            .map { it.toPerfil() }
    }

    override suspend fun getById(id: String): Result<Perfil?> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName)
            .select(Columns.ALL) {
                filter {
                    eq("id", id)
                }
            }
            .decodeList<JsonObject>()
            .firstOrNull()
            ?.toPerfil()
    }

    override suspend fun delete(id: String): Result<Unit> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName).delete {
            filter {
                eq("id", id)
            }
        }
        Unit
    }

    private fun JsonObject.toPerfil(): Perfil {
        return Perfil(
            id = stringOrEmpty("id"),
            nombreCompleto = nullableString("nombre_completo") ?: stringOrEmpty("nombreCompleto"),
            rolSlug = nullableString("rol_slug") ?: stringOrEmpty("rolSlug"),
            activo = booleanOrFalse("activo"),
            createdAt = nullableString("created_at") ?: nullableString("createdAt"),
            updatedAt = nullableString("updated_at") ?: nullableString("updatedAt"),
            ciudadId = nullableString("ciudad_id") ?: stringOrEmpty("ciudadId")
        )
    }

    private fun PerfilCreateDto.toCreateJson(): JsonObject {
        return JsonObject(
            mapOf(
                "nombre_completo" to JsonPrimitive(nombreCompleto),
                "rol_slug" to JsonPrimitive(rolSlug),
                "activo" to JsonPrimitive(activo),
                "ciudad_id" to JsonPrimitive(ciudadId)
            )
        )
    }

    private fun PerfilUpdateDto.toUpdateJson(): JsonObject {
        val map = mutableMapOf<String, JsonElement>()
        nombreCompleto?.let { map["nombre_completo"] = JsonPrimitive(it) }
        rolSlug?.let { map["rol_slug"] = JsonPrimitive(it) }
        activo?.let { map["activo"] = JsonPrimitive(it) }
        ciudadId?.let { map["ciudad_id"] = JsonPrimitive(it) }
        return JsonObject(map)
    }
}

