package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.utils.safeSupabaseCall
import io.github.jan.supabase.storage.storage

class ReporteStorageRepository {

    private val bucketName = "report_imagen"

    suspend fun uploadReportImage(
        bytes: ByteArray,
        objectName: String
    ): Result<String> = safeSupabaseCall {
        SupabaseProvider.client.storage[bucketName]
            .upload(objectName, bytes)
        objectName
    }
}
