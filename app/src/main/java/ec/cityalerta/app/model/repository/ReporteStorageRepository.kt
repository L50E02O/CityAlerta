package ec.cityalerta.app.model.repository

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

    suspend fun generateSignedImageUrl(
        imageUUID: String
    ): Result<String> = safeSupabaseCall {
        SupabaseProvider.client.storage[bucketName]
            .createSignedUrl(imageUUID, expirationDuration)
    }
}
