package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.perfil.PerfilResumen
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.utils.booleanOrFalse
import ec.cityalerta.app.model.utils.nullableString
import ec.cityalerta.app.model.utils.safeSupabaseCall
import ec.cityalerta.app.model.utils.stringOrEmpty
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PerfilResumenRepository {

    private val tableName = "perfil_resumen"

    suspend fun getCurrentResumen(): Result<PerfilResumen?> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
            SupabaseProvider.client.from(tableName)
                .select(Columns.ALL)
                .decodeList<JsonObject>()
                .firstOrNull()
                ?.toPerfilResumen()
        }
    }

    private fun JsonObject.toPerfilResumen(): PerfilResumen {
        return PerfilResumen(
            id = stringOrEmpty("id"),
            nombreCompleto = nullableString("nombre_completo") ?: stringOrEmpty("nombreCompleto"),
            rolSlug = nullableString("rol_slug") ?: stringOrEmpty("rolSlug"),
            activo = booleanOrFalse("activo"),
            ciudadId = nullableString("ciudad_id") ?: stringOrEmpty("ciudadId"),
            totalReportes = stringOrEmpty("total_reportes").toIntOrNull() ?: 0,
            reportesResueltos = stringOrEmpty("reportes_resueltos").toIntOrNull() ?: 0
        )
    }
}