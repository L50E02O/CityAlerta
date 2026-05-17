package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.reporteimagen.ReporteImagen
import ec.cityalerta.app.model.data.reporteimagen.ReporteImagenCreateDto
import ec.cityalerta.app.model.data.reporteimagen.ReporteImagenUpdateDto
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.data.contracts.crud.CrudRepositoryContract
import ec.cityalerta.app.model.utils.nullableString
import ec.cityalerta.app.model.utils.safeSupabaseCall
import ec.cityalerta.app.model.utils.stringOrEmpty
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonElement

class ReporteImagenRepository : CrudRepositoryContract<ReporteImagen, ReporteImagenCreateDto, ReporteImagenUpdateDto> {

    private val tableName = "reporte_imagen"

    override suspend fun create(entity: ReporteImagenCreateDto): Result<ReporteImagen> = safeSupabaseCall {
        val response = SupabaseProvider.client.from(tableName)
            .insert(entity.toCreateJson()){
                select()
            }
            .decodeList<JsonObject>()
            .firstOrNull()
        response?.toReporteImagen() ?: throw Exception("Error al crear reporte imagen")
    }

    override suspend fun update(entity: ReporteImagenUpdateDto, id: String): Result<ReporteImagen> = safeSupabaseCall {
        val response = SupabaseProvider.client.from(tableName).update(entity.toUpdateJson()) {
            filter {
                eq("id", id)
            }
            select()
        }
            .decodeList<JsonObject>()
            .firstOrNull()
        response?.toReporteImagen() ?: throw Exception("Error al actualizar reporte imagen")
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

    suspend fun getFirstImagenByReporteId(reporteId: String): Result<ReporteImagen?> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName)
            .select(Columns.ALL) {
                filter {
                    eq("reporte_id", reporteId)
                }
            }
            .decodeList<JsonObject>()
            .firstOrNull()
            ?.toReporteImagen()
    }

    suspend fun getAllImagenesByReporteId(reporteId: String): Result<List<ReporteImagen>> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName)
            .select(Columns.ALL) {
                filter {
                    eq("reporte_id", reporteId)
                }
            }
            .decodeList<JsonObject>()
            .map { it.toReporteImagen() }
    }

    private fun JsonObject.toReporteImagen(): ReporteImagen {
        return ReporteImagen(
            id = stringOrEmpty("id"),
            reporte_id = nullableString("reporte_id") ?: stringOrEmpty("reporteId"),
            storage_uuid = nullableString("storage_uuid") ?: stringOrEmpty("storageUuid"),
            url_path = nullableString("url_path") ?: stringOrEmpty("urlPath"),
            created_at = nullableString("created_at") ?: nullableString("createdAt"),
            updated_at = nullableString("updated_at") ?: nullableString("updatedAt")
        )
    }

    private fun ReporteImagenCreateDto.toCreateJson(): JsonObject {
        return JsonObject(
            mapOf(
                "reporte_id" to JsonPrimitive(reporte_id),
                "storage_uuid" to JsonPrimitive(storage_uuid),
                "url_path" to JsonPrimitive(url_path)
            )
        )
    }

    private fun ReporteImagenUpdateDto.toUpdateJson(): JsonObject {
        val map = mutableMapOf<String, JsonElement>()
        reporte_id?.let { map["reporte_id"] = JsonPrimitive(it) }
        storage_uuid?.let { map["storage_uuid"] = JsonPrimitive(it) }
        url_path?.let { map["url_path"] = JsonPrimitive(it) }
        return JsonObject(map)
    }
}

