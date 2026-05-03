package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.ReporteUbicacion
import ec.cityalerta.app.model.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class ReporteUbicacionRepository : ICrudRepository<ReporteUbicacion> {

    private val tableName = "reporte_ubicaciones"

    override suspend fun create(entity: ReporteUbicacion): Result<ReporteUbicacion> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName).insert(entity.toJson())
        entity
    }

    override suspend fun update(entity: ReporteUbicacion): Result<ReporteUbicacion> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName).update(entity.toJson()) {
            filter {
                eq("id", entity.id)
            }
        }
        entity
    }

    override suspend fun getAll(): Result<List<ReporteUbicacion>> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName)
            .select(Columns.ALL)
            .decodeList<JsonObject>()
            .map { it.toReporteUbicacion() }
    }

    override suspend fun getById(id: String): Result<ReporteUbicacion?> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName)
            .select(Columns.ALL) {
                filter {
                    eq("id", id)
                }
            }
            .decodeList<JsonObject>()
            .firstOrNull()
            ?.toReporteUbicacion()
    }

    override suspend fun delete(id: String): Result<Unit> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName).delete {
            filter {
                eq("id", id)
            }
        }
        Unit
    }

    private fun ReporteUbicacion.toJson(): JsonObject {
        return JsonObject(
            mapOf(
                "id" to JsonPrimitive(id),
                "lat" to JsonPrimitive(lat),
                "lng" to JsonPrimitive(lng),
                "direccion_aproximada" to JsonPrimitive(direccionAproximada),
                "created_at" to (createdAt?.let { JsonPrimitive(it) } ?: JsonNull),
                "updated_at" to (updatedAt?.let { JsonPrimitive(it) } ?: JsonNull)
            )
        )
    }

    private fun JsonObject.toReporteUbicacion(): ReporteUbicacion {
        return ReporteUbicacion(
            id = stringOrEmpty("id"),
            lat = doubleOrZero("lat"),
            lng = doubleOrZero("lng"),
            direccionAproximada = nullableString("direccion_aproximada") ?: stringOrEmpty("direccionAproximada"),
            createdAt = nullableString("created_at") ?: nullableString("createdAt"),
            updatedAt = nullableString("updated_at") ?: nullableString("updatedAt")
        )
    }
}

