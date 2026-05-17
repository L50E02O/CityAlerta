package ec.cityalerta.app.view.utils

import android.content.Context
import android.net.Uri

fun readBytesFromUri(context: Context, uri: Uri): ByteArray? {
    return context.contentResolver.openInputStream(uri)?.use { input ->
        input.readBytes()
    }
}
