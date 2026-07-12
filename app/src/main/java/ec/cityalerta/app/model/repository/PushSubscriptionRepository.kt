package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.contracts.notifications.PushSubscriptionRepositoryContract
import ec.cityalerta.app.model.data.push.PushSubscription
import ec.cityalerta.app.model.data.push.PushSubscriptionCreateDto
import ec.cityalerta.app.model.data.push.PushSubscriptionUpdateDto
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.utils.booleanOrFalse
import ec.cityalerta.app.model.utils.nullableString
import ec.cityalerta.app.model.utils.safeSupabaseCall
import ec.cityalerta.app.model.utils.stringOrEmpty
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PushSubscriptionRepository(
    private val tableName: String = "push_subscription"
) : PushSubscriptionRepositoryContract {

    private object PushColumns {
        const val id = "id"
        const val userId = "usuario_id"
        const val token = "token"
        const val deviceId = "device_id"
        const val platform = "platform"
        const val enabled = "enabled"
        const val createdAt = "created_at"
        const val updatedAt = "updated_at"
    }

    override suspend fun saveOrUpdate(entity: PushSubscriptionCreateDto): Result<PushSubscription> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
            val response = SupabaseProvider.client.from(tableName)
                .upsert(entity.toCreateJson(), onConflict = PushColumns.deviceId) {
                    select()
                }
                .decodeList<JsonObject>()
                .firstOrNull()
            response?.toPushSubscription() ?: throw Exception("Error al guardar/actualizar suscripcion")
        }
    }

    override suspend fun deleteByToken(token: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
            SupabaseProvider.client.from(tableName).delete {
                filter { eq(PushColumns.token, token) }
            }
            Unit
        }
    }

    private fun PushSubscriptionCreateDto.toCreateJson(): JsonObject {
        val map = mutableMapOf<String, JsonElement>(
            PushColumns.userId to JsonPrimitive(usuario_id),
            PushColumns.token to JsonPrimitive(token),
            PushColumns.enabled to JsonPrimitive(enabled)
        )
        device_id?.let { map[PushColumns.deviceId] = JsonPrimitive(it) }
        platform?.let { map[PushColumns.platform] = JsonPrimitive(it) }
        return JsonObject(map)
    }

    private fun JsonObject.toPushSubscription(): PushSubscription {
        val userId = firstString(PushColumns.userId, "user_id", "userId").orEmpty()
        val token = firstString(PushColumns.token, "token").orEmpty()
        val deviceId = firstString(PushColumns.deviceId, "deviceId")
        val platform = firstString(PushColumns.platform, "plataforma")

        return PushSubscription(
            id = stringOrEmpty(PushColumns.id),
            usuario_id = userId,
            token = token,
            device_id = deviceId,
            platform = platform,
            enabled = booleanOrFalse(PushColumns.enabled),
            created_at = nullableString(PushColumns.createdAt),
            updated_at = nullableString(PushColumns.updatedAt)
        )
    }

    private fun JsonObject.firstString(vararg keys: String): String? {
        for (key in keys) {
            val value = nullableString(key)
            if (!value.isNullOrBlank()) return value
        }
        return null
    }
}
