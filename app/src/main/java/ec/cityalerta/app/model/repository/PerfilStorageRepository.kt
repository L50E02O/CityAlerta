package ec.cityalerta.app.model.repository

import ec.cityalerta.app.BuildConfig
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.utils.safeSupabaseCall
import io.github.jan.supabase.storage.storage
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PerfilStorageRepository {

    private val bucketName = "perfil_imagen"
    private val expirationDuration: Duration = 2.days

    suspend fun uploadProfileImage(
        bytes: ByteArray,
        objectName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
            SupabaseProvider.client.storage[bucketName]
                .upload(objectName, bytes)
            objectName
        }
    }

    suspend fun generateSignedImageUrl(
        objectPath: String
    ): Result<String> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
            val signedUrl = SupabaseProvider.client.storage[bucketName]
                .createSignedUrl(objectPath, expirationDuration)
            normalizeSignedUrl(signedUrl)
        }
    }

    private fun normalizeSignedUrl(url: String): String {
        val trimmed = url.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }
        val base = BuildConfig.SUPABASE_URL.trimEnd('/')
        return "$base/storage/v1/${trimmed.trimStart('/')}"
    }

    suspend fun deleteProfileImage(
        imageUUID: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
            SupabaseProvider.client.storage[bucketName]
                .delete(imageUUID)
            Unit
        }
    }
}
