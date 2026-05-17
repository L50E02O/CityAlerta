package ec.cityalerta.app.view.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun rememberProfileImagePicker(
    onImageBytes: (ByteArray) -> Unit
): () -> Unit {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            readBytesFromUri(context, it)?.let(onImageBytes)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempUri?.let { uri ->
                readBytesFromUri(context, uri)?.let(onImageBytes)
            }
        }
    }

    fun launchCamera() {
        val imageFile = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
        tempUri = uri
        cameraLauncher.launch(uri)
    }

    if (showDialog) {
        ProfileImageSourceDialog(
            onDismiss = { showDialog = false },
            onTakePhoto = {
                showDialog = false
                launchCamera()
            },
            onPickGallery = {
                showDialog = false
                galleryLauncher.launch("image/*")
            }
        )
    }

    return { showDialog = true }
}

@Composable
private fun ProfileImageSourceDialog(
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickGallery: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar foto de perfil") },
        text = { Text("Elige como quieres actualizar tu foto.") },
        confirmButton = {
            TextButton(onClick = onTakePhoto) {
                Text("Tomar foto")
            }
        },
        dismissButton = {
            TextButton(onClick = onPickGallery) {
                Text("Galeria")
            }
        }
    )
}
