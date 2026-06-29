package ec.cityalerta.app.view.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.theme.*
import ec.cityalerta.app.view.components.AppTopBar
import ec.cityalerta.app.viewmodel.ProfileViewModel
import ec.cityalerta.app.viewmodel.ReportDetailViewModel

@Composable
fun ReportDetailScreen(
    navController: NavController,
    reportId: String,
    viewModel: ReportDetailViewModel,
    profileViewModel: ProfileViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val profileState by profileViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(reportId) {
        viewModel.loadReportDetail(reportId)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Detalle del Reporte",
                showBack = true,
                showSearch = true,
                profileImageUrl = profileState.profileImageUrl,
                isProfileLoading = profileState.isUploadingImage,
                onBackClick = { navController.popBackStack() },
                onSearchClick = { navController.navigate(Routes.Search.route) },
                onProfileClick = { navController.navigate(Routes.Profile.route) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                state.error != null -> {
                    Text(
                        text = state.error ?: "Error desconocido",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                state.report != null -> {
                    val report = state.report!!
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 1100.dp)
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Image and Tags Section
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                            ) {
                                if (report.imageUrl != null) {
                                    AsyncImage(
                                        model = report.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.outlineVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Imagen no disponible",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .align(Alignment.TopStart),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val categoryColor = when (report.categoryType) {
                                        ReportType.ZONA_DE_RIESGO -> CategoryRisk
                                        ReportType.BACHE -> CategoryPothole
                                        ReportType.AGUA -> CategoryWater
                                        ReportType.LUZ -> CategoryLight
                                    }
                                    DetailTag(
                                        text = report.categoria.uppercase(),
                                        backgroundColor = categoryColor
                                    )
                                    DetailTag(
                                        text = report.timeAgo.uppercase(),
                                        backgroundColor = Color.Black.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            // Content Section
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = report.barrio,
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 32.sp
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = report.direccion.uppercase(),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        val statusColor = when (report.estado) {
                                            "Resuelto" -> SuccessGreen
                                            "Pendiente" -> MaterialTheme.colorScheme.primary
                                            "En Proceso" -> ActionBlue
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                        Text(
                                            text = report.estado,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = statusColor
                                        )
                                        Text(
                                            text = "ESTADO",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "DESCRIPCIÓN",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = 0.5.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = report.descripcion,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 24.sp
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                Text(
                                    text = "UBICACIÓN DEL INCIDENTE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = 0.5.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Small Map View
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        val position = LatLng(report.lat, report.lng)
                                        val cameraPositionState = rememberCameraPositionState {
                                            this.position =
                                                CameraPosition.fromLatLngZoom(position, 16f)
                                        }

                                        GoogleMap(
                                            modifier = Modifier.fillMaxSize(),
                                            cameraPositionState = cameraPositionState,
                                            uiSettings = MapUiSettings(
                                                zoomControlsEnabled = false,
                                                myLocationButtonEnabled = false,
                                                scrollGesturesEnabled = false,
                                                zoomGesturesEnabled = false,
                                                tiltGesturesEnabled = false,
                                                rotationGesturesEnabled = false,
                                                mapToolbarEnabled = false
                                            )
                                        ) {
                                            Marker(
                                                state = MarkerState(position = position),
                                                title = report.barrio
                                            )
                                        }

                                        // Botones de acción (Simulando el toolbar de Google Maps)
                                        Surface(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(8.dp),
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color.White,
                                            shadowElevation = 2.dp
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        val gmmIntentUri =
                                                            android.net.Uri.parse("google.navigation:q=${report.lat},${report.lng}")
                                                        val mapIntent = android.content.Intent(
                                                            android.content.Intent.ACTION_VIEW,
                                                            gmmIntentUri
                                                        )
                                                        mapIntent.setPackage("com.google.android.apps.maps")
                                                        context.startActivity(mapIntent)
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Directions,
                                                        contentDescription = "Cómo llegar",
                                                        tint = Color(0xFF4285F4),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }

                                                VerticalDivider(
                                                    modifier = Modifier.height(24.dp)
                                                        .align(Alignment.CenterVertically)
                                                )

                                                IconButton(
                                                    onClick = {
                                                        navController.navigate(
                                                            Routes.Map.route
                                                                .replace(
                                                                    "{ciudadId}",
                                                                    report.ciudadId
                                                                )
                                                                .replace("{reportId}", report.id)
                                                        )
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Map,
                                                        contentDescription = "Ver en mapa de la app",
                                                        tint = Color(0xFF34A853),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(40.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun DetailTag(text: String, backgroundColor: Color) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
