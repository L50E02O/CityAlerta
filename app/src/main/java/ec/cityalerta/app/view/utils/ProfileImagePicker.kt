package ec.cityalerta.app.view.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun rememberProfileImagePicker(
    hasCustomImage: Boolean,
    onImageBytes: (ByteArray) -> Unit,
    onDeleteImage: () -> Unit
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
            hasCustomImage = hasCustomImage,
            onDismiss = { showDialog = false },
            onTakePhoto = {
                showDialog = false
                launchCamera()
            },
            onPickGallery = {
                showDialog = false
                galleryLauncher.launch("image/*")
            },
            onDelete = {
                showDialog = false
                onDeleteImage()
            }
        )
    }

    return { showDialog = true }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileImageSourceDialog(
    hasCustomImage: Boolean,
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickGallery: () -> Unit,
    onDelete: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Foto de perfil",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Elige una opcion para tu foto de perfil.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                TextButton(onClick = onTakePhoto, modifier = Modifier.fillMaxWidth()) {
                    Text("Tomar foto")
                }
                TextButton(onClick = onPickGallery, modifier = Modifier.fillMaxWidth()) {
                    Text("Elegir de galeria")
                }
                if (hasCustomImage) {
                    TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                        Text("Eliminar foto", color = Color(0xFFE74C3C))
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancelar")
                }
            }
        }
    }
}
