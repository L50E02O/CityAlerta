package ec.cityalerta.app.model.remote.service

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.data.contracts.notifications.PushSubscriptionRepositoryContract
import ec.cityalerta.app.model.data.push.PushSubscription
import ec.cityalerta.app.model.data.push.PushSubscriptionCreateDto
import ec.cityalerta.app.model.repository.AuthRepository
import ec.cityalerta.app.model.repository.PushSubscriptionRepository
import java.util.UUID
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
    fun getDeviceId(): String
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

    override fun getDeviceId(): String {
        var id = prefs.getString(KEY_DEVICE_ID, null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, id).apply()
        }
        return id
    }

    companion object {
        private const val PREFS_NAME = "cityalerta_prefs"
        private const val KEY_TOKEN = "push_token"
        private const val KEY_NOTIFICATIONS_ENABLED = "push_enabled"
        private const val KEY_DEVICE_ID = "device_id"
    }
}

class AndroidDeviceInfoProvider(private val pushPreferences: PushPreferences) : DeviceInfoProvider {
    override fun getDeviceId(): String = pushPreferences.getDeviceId()

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

        val tokenResult = tokenProvider.getToken()
        val token = tokenResult.getOrElse {
            pushPreferences.getToken() ?: return Result.failure(it)
        }

        val userIdResult = authRepository.getUserId()
        val userId = userIdResult.getOrElse { error ->
            return Result.failure(PushSubscriptionAuthRequiredException(error.message ?: "No hay usuario logueado"))
        }

        // Evitar re-registro si el token y el usuario no han cambiado
        val cachedToken = pushPreferences.getToken()
        // Aquí podrías guardar también el userId en preferencias si fuera necesario, 
        // por ahora confiamos en que si el token es el mismo, ya está registrado para este dispositivo.
        // Pero para ser más seguros, si ya tenemos el token cacheado, asumimos que ya se registró.
        if (token == cachedToken) {
            android.util.Log.d("PushRegistrar", "Token ya registrado localmente, omitiendo llamada a red.")
            // Retornamos un objeto dummy o null si el contrato lo permite, o simplemente éxito.
            // Dado que registerToken devuelve Result<PushSubscription>, podríamos intentar obtenerlo de la DB
            // o simplemente proceder si queremos estar 100% seguros.
            // Para optimizar recursos:
            return Result.success(PushSubscription(id="", usuario_id=userId, token=token, enabled=true))
        }

        cacheToken(token)
        return registerToken(token)
    }

    suspend fun unregisterToken(): Result<Unit> {
        pushPreferences.setNotificationsEnabled(false)
        val cachedToken = pushPreferences.getToken()
        val tokensToDelete = mutableSetOf<String>()

        if (!cachedToken.isNullOrBlank()) {
            tokensToDelete.add(cachedToken)
        }

        // Intentamos obtener el token actual de Firebase por si la cache esta vacia o desactualizada.
        val currentToken = tokenProvider.getToken().getOrNull()
        if (!currentToken.isNullOrBlank()) {
            tokensToDelete.add(currentToken)
        }

        if (tokensToDelete.isEmpty()) {
            pushPreferences.clearToken()
            return Result.success(Unit)
        }

        var firstFailure: Throwable? = null
        for (token in tokensToDelete) {
            val result = subscriptionRepository.deleteByToken(token)
            result.onFailure { error ->
                if (firstFailure == null) {
                    firstFailure = error
                }
            }
        }

        pushPreferences.clearToken()
        return firstFailure?.let { Result.failure(it) } ?: Result.success(Unit)
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
            val pushPrefs = AndroidPushPreferences(context)
            return PushSubscriptionRegistrar(
                authRepository = AuthRepository(),
                subscriptionRepository = PushSubscriptionRepository(),
                deviceInfoProvider = AndroidDeviceInfoProvider(pushPrefs),
                tokenProvider = FirebasePushTokenProvider(),
                pushPreferences = pushPrefs
            )
        }
    }
}

class PushSubscriptionAuthRequiredException(message: String) : Exception(message)
class PushSubscriptionDisabledException(message: String) : Exception(message)
class PushSubscriptionTokenMissingException(message: String) : Exception(message)
