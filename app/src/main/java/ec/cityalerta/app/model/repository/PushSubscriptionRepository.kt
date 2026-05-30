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
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

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

    override suspend fun saveOrUpdate(entity: PushSubscriptionCreateDto): Result<PushSubscription> = safeSupabaseCall {
        try {
            insert(entity)
        } catch (e: Exception) {
            if (isDuplicateTokenError(e)) {
                updateByTokenInternal(entity.token, entity.toUpdateDto())
            } else {
                throw e
            }
        }
    }

    override suspend fun getByToken(token: String): Result<PushSubscription?> = safeSupabaseCall {
        fetchByToken(token)
    }

    override suspend fun disableByToken(token: String): Result<Unit> = safeSupabaseCall {
        updateByTokenInternal(
            token,
            PushSubscriptionUpdateDto(enabled = false)
        )
        Unit
    }

    override suspend fun deleteByToken(token: String): Result<Unit> = safeSupabaseCall {
        SupabaseProvider.client.from(tableName).delete {
            filter { eq(PushColumns.token, token) }
        }
        Unit
    }

    override suspend fun updateByToken(token: String, entity: PushSubscriptionUpdateDto): Result<PushSubscription> = safeSupabaseCall {
        updateByTokenInternal(token, entity)
    }

    private suspend fun fetchByToken(token: String): PushSubscription? {
        return SupabaseProvider.client.from(tableName)
            .select(Columns.ALL) {
                filter { eq(PushColumns.token, token) }
            }
            .decodeList<JsonObject>()
            .firstOrNull()
            ?.toPushSubscription()
    }

    private suspend fun insert(entity: PushSubscriptionCreateDto): PushSubscription {
        val response = SupabaseProvider.client.from(tableName)
            .insert(entity.toCreateJson()) {
                select()
            }
            .decodeList<JsonObject>()
            .firstOrNull()
        return response?.toPushSubscription() ?: throw Exception("Error al guardar suscripcion")
    }

    private suspend fun updateByTokenInternal(token: String, entity: PushSubscriptionUpdateDto): PushSubscription {
        val response = SupabaseProvider.client.from(tableName)
            .update(entity.toUpdateJson()) {
                filter { eq(PushColumns.token, token) }
                select()
            }
            .decodeList<JsonObject>()
            .firstOrNull()
        return response?.toPushSubscription() ?: throw Exception("Error al actualizar suscripcion")
    }

    private fun PushSubscriptionCreateDto.toUpdateDto(): PushSubscriptionUpdateDto {
        return PushSubscriptionUpdateDto(
            usuario_id = usuario_id,
            device_id = device_id,
            platform = platform,
            enabled = enabled
        )
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

    private fun PushSubscriptionUpdateDto.toUpdateJson(): JsonObject {
        val map = mutableMapOf<String, JsonElement>()
        usuario_id?.let { map[PushColumns.userId] = JsonPrimitive(it) }
        device_id?.let { map[PushColumns.deviceId] = JsonPrimitive(it) }
        platform?.let { map[PushColumns.platform] = JsonPrimitive(it) }
        enabled?.let { map[PushColumns.enabled] = JsonPrimitive(it) }
        return JsonObject(map)
    }

    private fun isDuplicateTokenError(error: Exception): Boolean {
        val message = error.message.orEmpty()
        return message.contains("duplicate key value violates unique constraint", ignoreCase = true)
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
