package ec.cityalerta.app.view.camera

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import ec.cityalerta.app.viewmodel.ReporteViewModel
import ec.cityalerta.app.view.style.ReportUiColors
import ec.cityalerta.app.view.style.ReportUiDimens
import ec.cityalerta.app.view.style.ReportUiShapes
import java.io.File

@Composable
fun PhotoScreen(
    viewModel: ReporteViewModel,
    onPhotoCaptured: () -> Unit
) {
    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    fun handleSelectedImage(uri: Uri) {
        photoUri = uri
        viewModel.setImagen(uri.toString())
        val bytes = readBytesFromUri(context, uri)
        if (bytes != null) {
            viewModel.setImagenData(bytes)
            onPhotoCaptured()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            handleSelectedImage(it)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempUri?.let {
                handleSelectedImage(it)
            }
        }
    }

    fun launchCamera() {
        val imageFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
        tempUri = uri
        cameraLauncher.launch(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReportUiColors.ScreenBackground)
            .padding(ReportUiDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(ReportUiDimens.SectionSpacing)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ReportUiDimens.FrameHeight)
                .background(ReportUiColors.FrameBackground, ReportUiShapes.Frame)
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Foto seleccionada",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ReportUiColors.FrameBackground, ReportUiShapes.Frame)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .border(2.dp, ReportUiColors.FrameBorder, ReportUiShapes.FrameInner)
            )

            Text(
                text = "Alinee el objeto con las guias",
                color = ReportUiColors.FrameBorder,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(Color(0x99000000), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.size(ReportUiDimens.SideButtonSize),
                shape = ReportUiShapes.SideButton
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
            }

            ElevatedButton(
                onClick = { launchCamera() },
                modifier = Modifier.size(ReportUiDimens.CaptureButtonSize),
                shape = ReportUiShapes.Circle,
                colors = ButtonDefaults.buttonColors(containerColor = ReportUiColors.AccentRed)
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            OutlinedButton(
                onClick = { launchCamera() },
                modifier = Modifier.size(ReportUiDimens.SideButtonSize),
                shape = ReportUiShapes.SideButton
            ) {
                Icon(Icons.Default.Cached, contentDescription = null)
            }
        }

    }
}

private fun readBytesFromUri(context: Context, uri: Uri): ByteArray? {
    return context.contentResolver.openInputStream(uri)?.use { input ->
        input.readBytes()
    }
}