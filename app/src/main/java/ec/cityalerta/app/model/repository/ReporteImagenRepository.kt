package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.ReporteImagen
import ec.cityalerta.app.model.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class ReporteImagenRepository : ICrudRepository<ReporteImagen> {

    private val tableName = "reporte_imagen"

    override suspend fun create(entity: ReporteImagen): Result<ReporteImagen> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName).insert(entity.toJson())
        entity
    }

    override suspend fun update(entity: ReporteImagen): Result<ReporteImagen> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName).update(entity.toJson()) {
            filter {
                eq("id", entity.id)
            }
        }
        entity
    }

    override suspend fun getAll(): Result<List<ReporteImagen>> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName)
            .select(Columns.ALL)
            .decodeList<JsonObject>()
            .map { it.toReporteImagen() }
    }

    override suspend fun getById(id: String): Result<ReporteImagen?> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName)
            .select(Columns.ALL) {
                filter {
                    eq("id", id)
                }
            }
            .decodeList<JsonObject>()
            .firstOrNull()
            ?.toReporteImagen()
    }

    override suspend fun delete(id: String): Result<Unit> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName).delete {
            filter {
                eq("id", id)
            }
        }
        Unit
    }

    private fun ReporteImagen.toJson(): JsonObject {
        return JsonObject(
            mapOf(
                "id" to JsonPrimitive(id),
                "reporte_id" to JsonPrimitive(reporteId),
                "storage_uuid" to JsonPrimitive(storageUuid),
                "url_path" to JsonPrimitive(urlPath),
                "created_at" to (createdAt?.let { JsonPrimitive(it) } ?: JsonNull),
                "updated_at" to (updatedAt?.let { JsonPrimitive(it) } ?: JsonNull)
            )
        )
    }

    private fun JsonObject.toReporteImagen(): ReporteImagen {
        return ReporteImagen(
            id = stringOrEmpty("id"),
            reporteId = nullableString("reporte_id") ?: stringOrEmpty("reporteId"),
            storageUuid = nullableString("storage_uuid") ?: stringOrEmpty("storageUuid"),
            urlPath = nullableString("url_path") ?: stringOrEmpty("urlPath"),
            createdAt = nullableString("created_at") ?: nullableString("createdAt"),
            updatedAt = nullableString("updated_at") ?: nullableString("updatedAt")
        )
    }
}

