package ec.cityalerta.app.model.repository

import ec.cityalerta.app.BuildConfig
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.utils.safeSupabaseCall
import io.github.jan.supabase.storage.storage
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

class ReporteStorageRepository {

    private val bucketName = "report_imagen"
    private val expirationDuration: Duration = 2.days

    suspend fun uploadReportImage(
        bytes: ByteArray,
        objectName: String
    ): Result<String> = safeSupabaseCall {
        SupabaseProvider.client.storage[bucketName]
            .upload(objectName, bytes)
        objectName
    }

    /**
     * Genera una URL firmada lista para usar en Coil.
     * Supabase ya devuelve la URL absoluta (https://...); no requiere STORAGE_BASE_URL.
     */
    suspend fun generateSignedImageUrl(
        objectPath: String
    ): Result<String> = safeSupabaseCall {
        val signedUrl = SupabaseProvider.client.storage[bucketName]
            .createSignedUrl(objectPath, expirationDuration)
        normalizeSignedUrl(signedUrl)
    }

    private fun normalizeSignedUrl(url: String): String {
        val trimmed = url.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }
        val base = BuildConfig.SUPABASE_URL.trimEnd('/')
        return "$base/storage/v1/${trimmed.trimStart('/')}"
    }

    suspend fun deleteReportImage(
        imageUUID: String
    ): Result<Unit> = safeSupabaseCall {
        SupabaseProvider.client.storage[bucketName]
            .delete(imageUUID)
        Unit
    }
}
