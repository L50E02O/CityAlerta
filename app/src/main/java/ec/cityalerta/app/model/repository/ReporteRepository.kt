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
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import ec.cityalerta.app.model.local.ReporteDao
import ec.cityalerta.app.model.local.ReporteEntity

class ReporteRepository(
    private val reporteDao: ReporteDao? = null
) : CrudRepositoryContract<Reporte, ReporteCreateDto, ReporteUpdateDto> {

    private val tableName = "reporte"

    override suspend fun create(entity: ReporteCreateDto): Result<Reporte> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
            val response = SupabaseProvider.client.from(tableName)
                .insert(entity.toCreateJson()){
                    select()
                }
                .decodeList<JsonObject>()
                .firstOrNull()
            response?.toReporte() ?: throw Exception("Error al crear reporte")
        }
    }

    override suspend fun update(entity: ReporteUpdateDto, id: String): Result<Reporte> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
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
    }

    override suspend fun getAll(): Result<List<Reporte>> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
            SupabaseProvider.client.from(tableName)
                .select(Columns.ALL)
                .decodeList<JsonObject>()
                .map { it.toReporte() }
        }
    }

    override suspend fun getById(id: String): Result<Reporte?> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
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
    }

    override suspend fun delete(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
            SupabaseProvider.client.from(tableName).delete {
                filter {
                    eq("id", id)
                }
            }
            Unit
        }
    }

    suspend fun getReporteByUsuarioId(usuarioId: String): Result<List<Reporte>> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
            SupabaseProvider.client.from(tableName)
                .select(Columns.ALL) {
                    filter {
                        eq("usuario_id", usuarioId)
                    }
                }
                .decodeList<JsonObject>()
                .map { it.toReporte() }
        }
    }

    suspend fun getReporteByCiudadId(
        ciudadId: String,
        limit: Int = 10,
        offset: Int = 0
    ): Result<Pair<List<Reporte>, Long>> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
        val response = SupabaseProvider.client.from(tableName)
            .select(Columns.raw("*, reporte_imagen(url_path, storage_uuid), reporte_ubicacion(direccion_aproximada, lat, lng), barrio(nombre)")) {
                filter {
                    eq("ciudad_id", ciudadId)
                }
                order("created_at", Order.DESCENDING)
                range(offset.toLong(), (offset + limit - 1).toLong())
                count(Count.EXACT)
            }
        
        val reportes = response.decodeList<JsonObject>().map { it.toReporte() }
        val totalCount = response.countOrNull() ?: 0L
        
        // Sincronizar con Room
        reporteDao?.let { dao ->
            if (reportes.isNotEmpty()) {
                val entities = response.decodeList<JsonObject>().map { json ->
                    val r = json.toReporte()
                    val img = json["reporte_imagen"]?.let { if (it is kotlinx.serialization.json.JsonArray) it.firstOrNull() as? JsonObject else null }
                    val ubi = json["reporte_ubicacion"] as? JsonObject
                    val bar = json["barrio"] as? JsonObject
                    
                    ReporteEntity(
                        id = r.id,
                        usuario_id = r.usuario_id,
                        ciudad_id = r.ciudad_id,
                        ubicacion_id = r.ubicacion_id,
                        descripcion = r.descripcion,
                        estado = r.estado,
                        fecha_reporte = r.fecha_reporte,
                        categoria = r.categoria,
                        created_at = r.created_at,
                        updated_at = r.updated_at,
                        barrio_id = r.barrio_id,
                        barrio_nombre = bar?.stringOrEmpty("nombre"),
                        direccion_aproximada = ubi?.stringOrEmpty("direccion_aproximada"),
                        image_url = img?.stringOrEmpty("storage_uuid")
                    )
                }
                dao.refreshReportes(ciudadId, entities)
            }
        }
            
            Pair(reportes, totalCount)
        }
    }

    /**
     * SSOT: Método unificado que obtiene reportes por ciudad.
     * Internamente decide usar Room (cache) o Supabase (remoto).
     * Los ViewModels no necesitan saber la fuente de datos.
     */
    fun getReportesByCiudad(
        ciudadId: String,
        limit: Int = 10,
        offset: Int = 0,
        forceRefresh: Boolean = false
    ): kotlinx.coroutines.flow.Flow<List<Reporte>> {
        return kotlinx.coroutines.flow.flow {
            // Si hay Room disponible y no se fuerza refresh, usar cache local
            if (reporteDao != null && !forceRefresh) {
                val localCount = reporteDao.countReportesByCiudad(ciudadId)
                if (localCount > 0) {
                    // Emitir datos locales primero (respuesta rápida)
                    val localEntities = reporteDao.getReportesByCiudad(ciudadId, limit, offset)
                    emit(localEntities.map { it.toReporte() })
                }
            }
            
            // Siempre sincronizar con Supabase para tener datos frescos
            val remoteResult = getReporteByCiudadId(ciudadId, limit, offset)
            remoteResult.onSuccess { (reportes, _) ->
                emit(reportes)
            }
        }
    }

    suspend fun getReportesCount(ciudadId: String): Int = withContext(Dispatchers.IO) {
        // Priorizar conteo local si está disponible
        reporteDao?.countReportesByCiudad(ciudadId) ?: run {
            // Fallback a conteo remoto
            getReporteByCiudadId(ciudadId, 1, 0).getOrNull()?.second?.toInt() ?: 0
        }
    }

    fun getLocalReportesFlow(ciudadId: String, limit: Int, offset: Int): kotlinx.coroutines.flow.Flow<List<ReporteEntity>> {
        return reporteDao?.getReportesByCiudadFlow(ciudadId, limit, offset) ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    suspend fun getTotalLocalReportesCount(ciudadId: String): Int {
        return reporteDao?.countReportesByCiudad(ciudadId) ?: 0
    }

    suspend fun searchReportes(
        ciudadId: String,
        categoria: ReportType? = null,
        barrioNombreQuery: String? = null
    ): Result<List<ReporteSearchResult>> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
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

