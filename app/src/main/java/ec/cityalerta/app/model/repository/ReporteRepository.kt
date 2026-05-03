package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.Reporte
import ec.cityalerta.app.model.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class ReporteRepository : ICrudRepository<Reporte> {

    private val tableName = "reporte"

    override suspend fun create(entity: Reporte): Result<Reporte> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName).insert(entity.toJson())
        entity
    }

    override suspend fun update(entity: Reporte): Result<Reporte> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName).update(entity.toJson()) {
            filter {
                eq("id", entity.id)
            }
        }
        entity
    }

    override suspend fun getAll(): Result<List<Reporte>> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName)
            .select(Columns.ALL)
            .decodeList<JsonObject>()
            .map { it.toReporte() }
    }

    override suspend fun getById(id: String): Result<Reporte?> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName)
            .select(Columns.ALL) {
                filter {
                    eq("id", id)
                }
            }
            .decodeList<JsonObject>()
            .firstOrNull()
            ?.toReporte()
    }

    override suspend fun delete(id: String): Result<Unit> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName).delete {
            filter {
                eq("id", id)
            }
        }
        Unit
    }

    private fun Reporte.toJson(): JsonObject {
        return JsonObject(
            mapOf(
                "id" to JsonPrimitive(id),
                "usuario_id" to JsonPrimitive(usuarioId),
                "ciudad_id" to JsonPrimitive(ciudadId),
                "ubicacion_id" to JsonPrimitive(ubicacionId),
                "descripcion" to JsonPrimitive(descripcion),
                "estado_slug" to JsonPrimitive(estado.name),
                "fecha_reporte" to JsonPrimitive(fechaReporte),
                "categoria" to JsonPrimitive(categoria),
                "updated_at" to (updatedAt?.let { JsonPrimitive(it) } ?: JsonNull)
            )
        )
    }

    private fun JsonObject.toReporte(): Reporte {
        return Reporte(
            id = stringOrEmpty("id"),
            usuarioId = nullableString("usuario_id") ?: stringOrEmpty("usuarioId"),
            ciudadId = nullableString("ciudad_id") ?: stringOrEmpty("ciudadId"),
            ubicacionId = nullableString("ubicacion_id") ?: stringOrEmpty("ubicacionId"),
            descripcion = stringOrEmpty("descripcion"),
            estado = toReporteEstadoOrDefault("estado_slug"),
            fechaReporte = nullableString("fecha_reporte") ?: stringOrEmpty("fechaReporte"),
            categoria = stringOrEmpty("categoria"),
            updatedAt = nullableString("updated_at") ?: nullableString("updatedAt")
        )
    }
}

