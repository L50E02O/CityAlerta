package ec.cityalerta.app.model.repository

import android.util.LruCache
import ec.cityalerta.app.BuildConfig
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.utils.safeSupabaseCall
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

class ReporteStorageRepository {

    private val bucketName = "report_imagen"
    private val expirationDuration: Duration = 2.days

    // Cache temporal para evitar peticiones N+1 de URLs firmadas en una misma sesión
    private val urlCache = LruCache<String, String>(100)

    suspend fun uploadReportImage(
        bytes: ByteArray,
        objectName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
            SupabaseProvider.client.storage[bucketName]
                .upload(objectName, bytes)
            // Limpiar cache si se sobreescribe
            urlCache.remove(objectName)
            objectName
        }
    }

    /**
     * Genera una URL firmada lista para usar en Coil.
     * Utiliza cache local si la URL ya fue generada previamente.
     */
    suspend fun generateSignedImageUrl(
        objectPath: String
    ): Result<String> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
            urlCache.get(objectPath)?.let { return@safeSupabaseCall it }

            val signedUrl = SupabaseProvider.client.storage[bucketName]
                .createSignedUrl(objectPath, expirationDuration)
            val normalized = normalizeSignedUrl(signedUrl)
            
            urlCache.put(objectPath, normalized)
            normalized
        }
    }

    suspend fun generateSignedImageUrls(objectPaths: List<String>): Result<Map<String, String>> =
        withContext(Dispatchers.IO) {
            safeSupabaseCall {
                if (objectPaths.isEmpty()) {
                    return@safeSupabaseCall emptyMap()
                }
                coroutineScope {
                    objectPaths.distinct().map { path ->
                        async {
                            path to generateSignedImageUrl(path).getOrNull()
                        }
                    }.awaitAll()
                        .mapNotNull { (path, url) -> url?.let { path to it } }
                        .toMap()
                }
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

    suspend fun deleteReportImage(
        imageUUID: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        safeSupabaseCall {
            SupabaseProvider.client.storage[bucketName]
                .delete(imageUUID)
            Unit
        }
    }
}
