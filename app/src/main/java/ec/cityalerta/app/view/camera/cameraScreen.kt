package ec.cityalerta.app.view.camera

import android.Manifest
import android.net.Uri
import android.content.pm.PackageManager
import android.view.View
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.view.components.AppTopBar
import ec.cityalerta.app.view.style.ReportUiColors
import ec.cityalerta.app.view.style.ReportUiDimens
import ec.cityalerta.app.view.style.ReportUiShapes
import ec.cityalerta.app.view.utils.readBytesFromUri
import ec.cityalerta.app.viewmodel.ReporteViewModel
import ec.cityalerta.app.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoScreen(
    navController: NavController,
    viewModel: ReporteViewModel,
    profileViewModel: ProfileViewModel,
    onPhotoCaptured: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraController: CameraController = remember(context) { CameraXController(context) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val profileState = profileViewModel.state.collectAsState().value

    LaunchedEffect(Unit) {
        profileViewModel.loadSummaryIfNeeded()
    }

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

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            cameraError = "Permiso de camara requerido"
        }
    }

    fun capturePhoto() {
        cameraController.capturePhoto(
            onSuccess = { uri ->
                cameraError = null
                handleSelectedImage(uri)
            },
            onError = { message ->
                cameraError = message
            }
        )
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            runCatching {
                cameraController.bind(lifecycleOwner)
                cameraError = null
            }.onFailure {
                cameraError = "No se pudo iniciar la camara"
            }
        }
    }

    DisposableEffect(hasCameraPermission) {
        onDispose {
            if (hasCameraPermission) {
                cameraController.release()
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Foto",
                showBack = true,
                profileImageUrl = profileState.profileImageUrl,
                isProfileLoading = profileState.isUploadingImage,
                onBackClick = { navController.popBackStack() },
                onProfileClick = { navController.navigate(Routes.Profile.route) }
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(ReportUiDimens.ScreenPadding)
                .padding(padding)
                .padding(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(ReportUiDimens.SectionSpacing)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            CameraPreviewFrame(
                photoUri = photoUri,
                previewView = cameraController.previewView,
                hasCameraPermission = hasCameraPermission,
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) }
            )

            Spacer(modifier = Modifier.weight(1f))

            CameraControls(
                hasCameraPermission = hasCameraPermission,
                onOpenGallery = { galleryLauncher.launch("image/*") },
                onCapture = ::capturePhoto,
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) }
            )

            if (!cameraError.isNullOrBlank()) {
                Text(
                    text = cameraError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun CameraPreviewFrame(
    photoUri: Uri?,
    previewView: View,
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ReportUiDimens.FrameHeight)
            .background(Color.Black, ReportUiShapes.Frame)
            .clip(ReportUiShapes.Frame)
    ) {
        if (photoUri != null) {
            AsyncImage(
                model = photoUri,
                contentDescription = "Foto seleccionada",
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black, ReportUiShapes.Frame)
            )
        } else {
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxSize()
                    .clip(ReportUiShapes.Frame)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .border(2.dp, Color.White.copy(alpha = 0.8f), ReportUiShapes.FrameInner)
        )

        Text(
            text = "Alinee el objeto con las guias",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )

        if (!hasCameraPermission) {
            CameraPermissionOverlay(onRequestPermission = onRequestPermission)
        }
    }
}

@Composable
private fun CameraPermissionOverlay(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3000000))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Permiso de camara requerido",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        ElevatedButton(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text("Permitir camara", color = Color(0xFF1B1B1B))
        }
    }
}

@Composable
private fun CameraControls(
    hasCameraPermission: Boolean,
    onOpenGallery: () -> Unit,
    onCapture: () -> Unit,
    onRequestPermission: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onOpenGallery,
            modifier = Modifier.size(ReportUiDimens.SideButtonSize),
            shape = ReportUiShapes.SideButton,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }

        ElevatedButton(
            onClick = {
                if (hasCameraPermission) {
                    onCapture()
                } else {
                    onRequestPermission()
                }
            },
            modifier = Modifier.size(ReportUiDimens.CaptureButtonSize),
            shape = ReportUiShapes.Circle,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }

        OutlinedButton(
            onClick = {
                if (hasCameraPermission) {
                    onCapture()
                } else {
                    onRequestPermission()
                }
            },
            modifier = Modifier.size(ReportUiDimens.SideButtonSize),
            shape = ReportUiShapes.SideButton,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
                imageVector = Icons.Default.Cached,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
