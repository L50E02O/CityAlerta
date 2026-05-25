package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacion
import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacionCreateDto
import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacionUpdateDto
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.data.contracts.crud.CrudRepositoryContract
import ec.cityalerta.app.model.utils.doubleOrZero
import ec.cityalerta.app.model.utils.nullableString
import ec.cityalerta.app.model.utils.safeSupabaseCall
import ec.cityalerta.app.model.utils.stringOrEmpty
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonElement

class ReporteUbicacionRepository : CrudRepositoryContract<ReporteUbicacion, ReporteUbicacionCreateDto, ReporteUbicacionUpdateDto> {

    private val tableName = "reporte_ubicacion"

    override suspend fun create(entity: ReporteUbicacionCreateDto): Result<ReporteUbicacion> =
        safeSupabaseCall {
            val response = SupabaseProvider.client.from(tableName)
                .insert(entity.toCreateJson()){
                select()
                }
                .decodeList<JsonObject>()
                .firstOrNull()
            response?.toReporteUbicacion() ?: throw Exception("Error al crear reporte ubicacion")
        }

    override suspend fun update(entity: ReporteUbicacionUpdateDto, id: String): Result<ReporteUbicacion> =
        safeSupabaseCall {
            val response = SupabaseProvider.client.from(tableName).update(entity.toUpdateJson()) {
                filter {
                    eq("id", id)
                }
                select()
            }
                .decodeList<JsonObject>()
                .firstOrNull()
            response?.toReporteUbicacion() ?: throw Exception("Error al actualizar reporte ubicacion")
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

    private fun JsonObject.toReporteUbicacion(): ReporteUbicacion {
        return ReporteUbicacion(
            id = stringOrEmpty("id"),
            lat = doubleOrZero("lat"),
            lng = doubleOrZero("lng"),
            direccion_aproximada = nullableString("direccion_aproximada") ?: stringOrEmpty("direccionAproximada"),
            created_at = nullableString("created_at") ?: nullableString("createdAt"),
            updated_at = nullableString("updated_at") ?: nullableString("updatedAt")
        )
    }

    private fun ReporteUbicacionCreateDto.toCreateJson(): JsonObject {
        return JsonObject(
            mapOf(
                "lat" to JsonPrimitive(lat),
                "lng" to JsonPrimitive(lng),
                "direccion_aproximada" to JsonPrimitive(direccion_aproximada)
            )
        )
    }

    private fun ReporteUbicacionUpdateDto.toUpdateJson(): JsonObject {
        val map = mutableMapOf<String, JsonElement>()
        lat?.let { map["lat"] = JsonPrimitive(it) }
        lng?.let { map["lng"] = JsonPrimitive(it) }
        direccion_aproximada?.let { map["direccion_aproximada"] = JsonPrimitive(it) }
        return JsonObject(map)
    }
}

