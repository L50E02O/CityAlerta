package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporte.ReporteCreateDto
import ec.cityalerta.app.model.data.reporte.ReporteUpdateDto
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.repository.interfaces.ICrudRepository
import ec.cityalerta.app.model.utils.nullableString
import ec.cityalerta.app.model.utils.safeSupabaseCall
import ec.cityalerta.app.model.utils.stringOrEmpty
import ec.cityalerta.app.model.utils.toReportTypeOrDefault
import ec.cityalerta.app.model.utils.toReporteEstadoOrDefault
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonElement

class ReporteRepository : ICrudRepository<Reporte, ReporteCreateDto, ReporteUpdateDto> {

    private val tableName = "reporte"

    override suspend fun create(entity: ReporteCreateDto): Result<Reporte> = safeSupabaseCall {
        val response = SupabaseProvider.client.from(tableName).insert(entity.toCreateJson())
            .decodeList<JsonObject>()
            .firstOrNull()
        response?.toReporte() ?: throw Exception("Error al crear reporte")
    }

    override suspend fun update(entity: ReporteUpdateDto, id: String): Result<Reporte> = safeSupabaseCall {
        val response = SupabaseProvider.client.from(tableName).update(entity.toUpdateJson()) {
            filter {
                eq("id", id)
            }
        }
            .decodeList<JsonObject>()
            .firstOrNull()
        response?.toReporte() ?: throw Exception("Error al actualizar reporte")
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

    private fun JsonObject.toReporte(): Reporte {
        return Reporte(
            id = stringOrEmpty("id"),
            usuarioId = nullableString("usuario_id") ?: stringOrEmpty("usuarioId"),
            ciudadId = nullableString("ciudad_id") ?: stringOrEmpty("ciudadId"),
            ubicacionId = nullableString("ubicacion_id") ?: stringOrEmpty("ubicacionId"),
            descripcion = stringOrEmpty("descripcion"),
            estado = toReporteEstadoOrDefault("estado_slug"),
            fechaReporte = nullableString("fecha_reporte") ?: stringOrEmpty("fechaReporte"),
            categoria = toReportTypeOrDefault("categoria"),
            updatedAt = nullableString("updated_at") ?: nullableString("updatedAt")
        )
    }

    private fun ReporteCreateDto.toCreateJson(): JsonObject {
        return JsonObject(
            mapOf(
                "usuario_id" to JsonPrimitive(usuarioId),
                "ciudad_id" to JsonPrimitive(ciudadId),
                "ubicacion_id" to JsonPrimitive(ubicacionId),
                "descripcion" to JsonPrimitive(descripcion),
                "estado_slug" to JsonPrimitive(estado.name),
                "fecha_reporte" to JsonPrimitive(fechaReporte),
                "categoria" to JsonPrimitive(categoria.name)
            )
        )
    }

    private fun ReporteUpdateDto.toUpdateJson(): JsonObject {
        val map = mutableMapOf<String, JsonElement>()
        usuarioId?.let { map["usuario_id"] = JsonPrimitive(it) }
        ciudadId?.let { map["ciudad_id"] = JsonPrimitive(it) }
        ubicacionId?.let { map["ubicacion_id"] = JsonPrimitive(it) }
        descripcion?.let { map["descripcion"] = JsonPrimitive(it) }
        estado?.let { map["estado_slug"] = JsonPrimitive(it.name) }
        fechaReporte?.let { map["fecha_reporte"] = JsonPrimitive(it) }
        categoria?.let { map["categoria"] = JsonPrimitive(it.name) }
        return JsonObject(map)
    }
}

