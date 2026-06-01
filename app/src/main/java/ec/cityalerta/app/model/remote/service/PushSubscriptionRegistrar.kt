package ec.cityalerta.app.model.remote.service

import android.content.Context
import android.provider.Settings
import com.google.firebase.messaging.FirebaseMessaging
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.data.contracts.notifications.PushSubscriptionRepositoryContract
import ec.cityalerta.app.model.data.push.PushSubscription
import ec.cityalerta.app.model.data.push.PushSubscriptionCreateDto
import ec.cityalerta.app.model.repository.AuthRepository
import ec.cityalerta.app.model.repository.PushSubscriptionRepository
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface PushTokenProvider {
    suspend fun getToken(): Result<String>
}

class FirebasePushTokenProvider : PushTokenProvider {
    override suspend fun getToken(): Result<String> = suspendCoroutine { cont ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token -> cont.resume(Result.success(token)) }
            .addOnFailureListener { error -> cont.resume(Result.failure(error)) }
    }
}

interface DeviceInfoProvider {
    fun getDeviceId(): String
    fun getPlatform(): String
}

interface PushPreferences {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
    fun setNotificationsEnabled(enabled: Boolean)
    fun isNotificationsEnabled(): Boolean
}

class AndroidPushPreferences(context: Context) : PushPreferences {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    override fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    override fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    override fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    override fun isNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)

    companion object {
        private const val PREFS_NAME = "cityalerta_prefs"
        private const val KEY_TOKEN = "push_token"
        private const val KEY_NOTIFICATIONS_ENABLED = "push_enabled"
    }
}

class AndroidDeviceInfoProvider(private val context: Context) : DeviceInfoProvider {
    override fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID).orEmpty()
    }

    override fun getPlatform(): String = "android"
}

class PushSubscriptionRegistrar(
    private val authRepository: AuthRepositoryContract,
    private val subscriptionRepository: PushSubscriptionRepositoryContract,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val tokenProvider: PushTokenProvider,
    private val pushPreferences: PushPreferences
) {
    fun cacheToken(token: String) {
        if (token.isBlank()) return
        pushPreferences.saveToken(token)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        pushPreferences.setNotificationsEnabled(enabled)
    }

    fun isNotificationsEnabled(): Boolean = pushPreferences.isNotificationsEnabled()

    suspend fun registerTokenIfAllowed(): Result<PushSubscription> {
        if (!pushPreferences.isNotificationsEnabled()) {
            return Result.failure(PushSubscriptionDisabledException("Notificaciones desactivadas"))
        }

        // Simplificación: Intentamos obtener el token de la fuente de verdad (Firebase) 
        // o de nuestra caché si Firebase falla por alguna razón.
        val tokenResult = tokenProvider.getToken()
        val token = tokenResult.getOrElse { 
            pushPreferences.getToken() ?: return Result.failure(it) 
        }
        
        cacheToken(token)
        return registerToken(token)
    }

    suspend fun unregisterToken(): Result<Unit> {
        pushPreferences.setNotificationsEnabled(false)
        val cachedToken = pushPreferences.getToken()
        if (cachedToken.isNullOrBlank()) {
            pushPreferences.clearToken()
            return Result.success(Unit)
        }
        val result = subscriptionRepository.deleteByToken(cachedToken)
        result.onSuccess {
            pushPreferences.clearToken()
        }
        return result
    }

    suspend fun registerToken(token: String): Result<PushSubscription> {
        val userIdResult = authRepository.getUserId()
        val userId = userIdResult.getOrElse { error ->
            return Result.failure(PushSubscriptionAuthRequiredException(error.message ?: "No hay usuario logueado"))
        }
        val deviceId = deviceInfoProvider.getDeviceId().ifBlank { null }
        val platform = deviceInfoProvider.getPlatform().ifBlank { null }
        val result = subscriptionRepository.saveOrUpdate(
            PushSubscriptionCreateDto(
                usuario_id = userId,
                token = token,
                device_id = deviceId,
                platform = platform,
                enabled = true
            )
        )
        result.onSuccess {
        }.onFailure { error ->
        }
        return result
    }

    companion object {
        fun createDefault(context: Context): PushSubscriptionRegistrar {
            return PushSubscriptionRegistrar(
                authRepository = AuthRepository(),
                subscriptionRepository = PushSubscriptionRepository(),
                deviceInfoProvider = AndroidDeviceInfoProvider(context),
                tokenProvider = FirebasePushTokenProvider(),
                pushPreferences = AndroidPushPreferences(context)
            )
        }
    }
}

class PushSubscriptionAuthRequiredException(message: String) : Exception(message)
class PushSubscriptionDisabledException(message: String) : Exception(message)
class PushSubscriptionTokenMissingException(message: String) : Exception(message)
