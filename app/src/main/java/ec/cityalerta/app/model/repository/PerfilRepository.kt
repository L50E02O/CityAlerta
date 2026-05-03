package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.Perfil
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.repository.interfaces.ICrudRepository
import ec.cityalerta.app.model.utils.booleanOrFalse
import ec.cityalerta.app.model.utils.nullableString
import ec.cityalerta.app.model.utils.safeSupabaseCall
import ec.cityalerta.app.model.utils.stringOrEmpty
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class PerfilRepository : ICrudRepository<Perfil> {

    private val tableName = "perfil"

    override suspend fun create(entity: Perfil): Result<Perfil> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName).insert(entity.toJson())
        entity
    }

    override suspend fun update(entity: Perfil): Result<Perfil> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName).update(entity.toJson()) {
            filter {
                eq("id", entity.id)
            }
        }
        entity
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

    private fun Perfil.toJson(): JsonObject {
        return JsonObject(
            mapOf(
                "id" to JsonPrimitive(id),
                "nombre_completo" to JsonPrimitive(nombreCompleto),
                "rol_slug" to JsonPrimitive(rolSlug),
                "activo" to JsonPrimitive(activo),
                "created_at" to (createdAt?.let { JsonPrimitive(it) } ?: JsonNull),
                "updated_at" to (updatedAt?.let { JsonPrimitive(it) } ?: JsonNull)
            )
        )
    }

    private fun JsonObject.toPerfil(): Perfil {
        return Perfil(
            id = stringOrEmpty("id"),
            nombreCompleto = nullableString("nombre_completo") ?: stringOrEmpty("nombreCompleto"),
            rolSlug = nullableString("rol_slug") ?: stringOrEmpty("rolSlug"),
            activo = booleanOrFalse("activo"),
            createdAt = nullableString("created_at") ?: nullableString("createdAt"),
            updatedAt = nullableString("updated_at") ?: nullableString("updatedAt")
        )
    }
}

