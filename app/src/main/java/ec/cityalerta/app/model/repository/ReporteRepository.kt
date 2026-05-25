package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporte.ReporteCreateDto
import ec.cityalerta.app.model.data.reporte.ReporteSearchResult
import ec.cityalerta.app.model.data.reporte.ReporteUpdateDto
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.data.contracts.crud.CrudRepositoryContract
import ec.cityalerta.app.model.utils.nullableString
import ec.cityalerta.app.model.utils.safeSupabaseCall
import ec.cityalerta.app.model.utils.stringOrEmpty
import ec.cityalerta.app.model.utils.toReportTypeOrDefault
import ec.cityalerta.app.model.utils.toReporteEstadoOrDefault
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonElement

class ReporteRepository : CrudRepositoryContract<Reporte, ReporteCreateDto, ReporteUpdateDto> {

    private val tableName = "reporte"

    override suspend fun create(entity: ReporteCreateDto): Result<Reporte> = safeSupabaseCall {
        val response = SupabaseProvider.client.from(tableName)
            .insert(entity.toCreateJson()){
                select()
            }
            .decodeList<JsonObject>()
            .firstOrNull()
        response?.toReporte() ?: throw Exception("Error al crear reporte")
    }

    override suspend fun update(entity: ReporteUpdateDto, id: String): Result<Reporte> = safeSupabaseCall {
        val response = SupabaseProvider.client.from(tableName).update(entity.toUpdateJson()) {
            filter {
                eq("id", id)
            }
            select()
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

    suspend fun getReporteByUsuarioId(usuarioId: String): Result<List<Reporte>> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName)
            .select(Columns.ALL) {
                filter {
                    eq("usuario_id", usuarioId)
                }
            }
            .decodeList<JsonObject>()
            .map { it.toReporte() }
    }

    suspend fun getReporteByCiudadId(ciudadId: String): Result<List<Reporte>> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName)
            .select(Columns.ALL) {
                filter {
                    eq("ciudad_id", ciudadId)
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<JsonObject>()
            .map { it.toReporte() }
    }

    suspend fun searchReportes(
        ciudadId: String,
        categoria: ReportType? = null,
        barrioNombreQuery: String? = null
    ): Result<List<ReporteSearchResult>> = safeSupabaseCall {
        SupabaseProvider.client.postgrest.rpc(
            "search_reportes",
            mapOf(
                "p_ciudad_id" to ciudadId,
                "p_categoria" to categoria?.name,
                "p_barrio_nombre" to barrioNombreQuery?.trim()?.takeIf { it.isNotEmpty() }
            )
        )
            .decodeList<JsonObject>()
            .map { it.toReporteSearchResult() }
    }

    private fun JsonObject.toReporteSearchResult(): ReporteSearchResult {
        return ReporteSearchResult(
            reporte = toReporte(),
            barrioNombre = stringOrEmpty("barrio_nombre"),
            direccionAproximada = nullableString("direccion_aproximada")
        )
    }

    private fun JsonObject.toReporte(): Reporte {
        return Reporte(
            id = stringOrEmpty("id"),
            usuario_id = nullableString("usuario_id") ?: stringOrEmpty("usuarioId"),
            ciudad_id = nullableString("ciudad_id") ?: stringOrEmpty("ciudadId"),
            ubicacion_id = nullableString("ubicacion_id") ?: stringOrEmpty("ubicacionId"),
            descripcion = stringOrEmpty("descripcion"),
            estado = toReporteEstadoOrDefault("estado")
                .takeIf { stringOrEmpty("estado").isNotBlank() }
                ?: toReporteEstadoOrDefault("estado_slug"),
            fecha_reporte = nullableString("fecha_reporte") ?: stringOrEmpty("fechaReporte"),
            categoria = try{
                ReportType.valueOf(stringOrEmpty("categoria"))
            }catch (e: Exception){
                ReportType.ZONA_DE_RIESGO
            },
            created_at = nullableString("created_at") ?: nullableString("createdAt"),
            updated_at = nullableString("updated_at") ?: nullableString("updatedAt"),
            barrio_id = nullableString("barrio_id") ?: stringOrEmpty("barrioId")
        )
    }

    private fun ReporteCreateDto.toCreateJson(): JsonObject {
        return JsonObject(
            mapOf(
                "usuario_id" to JsonPrimitive(usuario_id),
                "ciudad_id" to JsonPrimitive(ciudad_id),
                "ubicacion_id" to JsonPrimitive(ubicacion_id),
                "descripcion" to JsonPrimitive(descripcion),
                "estado" to JsonPrimitive(estado.name),
                "fecha_reporte" to JsonPrimitive(fecha_reporte),
                "categoria" to JsonPrimitive(categoria.name),
                "barrio_id" to JsonPrimitive(barrio_id)
            )
        )
    }

    private fun ReporteUpdateDto.toUpdateJson(): JsonObject {
        val map = mutableMapOf<String, JsonElement>()
        usuario_id?.let { map["usuario_id"] = JsonPrimitive(it) }
        ciudad_id?.let { map["ciudad_id"] = JsonPrimitive(it) }
        ubicacion_id?.let { map["ubicacion_id"] = JsonPrimitive(it) }
        descripcion?.let { map["descripcion"] = JsonPrimitive(it) }
        estado?.let { map["estado"] = JsonPrimitive(it.name) }
        fecha_reporte?.let { map["fecha_reporte"] = JsonPrimitive(it) }
        categoria?.let { map["categoria"] = JsonPrimitive(it.name) }
        barrio_id?.let { map["barrio_id"] = JsonPrimitive(it) }
        return JsonObject(map)
    }
}

