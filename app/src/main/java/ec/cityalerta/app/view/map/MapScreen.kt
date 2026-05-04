package ec.cityalerta.app.view.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import ec.cityalerta.app.model.utils.GeoJsonConverter
import ec.cityalerta.app.view.map.components.CategoryFilter
import ec.cityalerta.app.view.map.components.ReportDetailCard
import ec.cityalerta.app.viewmodel.MapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    ciudadId: String = "manta",
    viewModel: MapViewModel
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Gestión de permisos de ubicación
    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationPermissionGranted = isGranted
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionGranted) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val defaultLocation = LatLng(-0.95, -80.73)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }

    LaunchedEffect(ciudadId) {
        viewModel.loadCiudad(ciudadId)
    }

    LaunchedEffect(uiState.ciudad, uiState.cameraZoom) {
        uiState.ciudad?.let { ciudad ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(ciudad.centroLat, ciudad.centroLng),
                uiState.cameraZoom
            )
        }
    }

    LaunchedEffect(uiState.isPointValid) {
        if (uiState.isPointValid == false) {
            snackbarHostState.showSnackbar(
                message = "El punto debe estar dentro del área permitida",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(56.dp),
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Box(modifier = Modifier.padding(top = 10.dp)) {
                        Text(uiState.ciudad?.nombre ?: "Mapa")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                },
                actions = {
                    if (uiState.marcadores.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearMarkers() }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Limpiar marcadores"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                uiState.ciudad != null -> {
                    val ciudad = uiState.ciudad
                    val polygonPoints = remember(ciudad) {
                        GeoJsonConverter.extractPolygonPoints(
                            ciudad.geojson
                        )
                    }

                    val filteredReports = remember(uiState.reports, uiState.selectedCategory) {
                        uiState.reports.filter {
                            uiState.selectedCategory == null || it.type == uiState.selectedCategory
                        }
                    }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = locationPermissionGranted),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            myLocationButtonEnabled = locationPermissionGranted,
                            mapToolbarEnabled = false
                        ),
                        onMapClick = { latLng -> viewModel.onMapClicked(latLng) },
                        contentPadding = PaddingValues(top = 110.dp, bottom = 165.dp)
                    ) {
                        if (polygonPoints.isNotEmpty()) {
                            Polygon(
                                points = polygonPoints,
                                fillColor = Color(0x4287F5CC),
                                strokeColor = Color(0xFF287FCC),
                                strokeWidth = 2f
                            )
                        }

                        uiState.marcadores.forEach { marker ->
                            key(marker.id) {
                                Marker(
                                    state = rememberMarkerState(position = LatLng(marker.latitude, marker.longitude)),
                                    title = marker.title,
                                    snippet = marker.description ?: ""
                                )
                            }
                        }

                        filteredReports.forEach { report ->
                            key(report.id) {
                                Marker(
                                    state = rememberMarkerState(position = LatLng(report.latitude, report.longitude)),
                                    title = report.title,
                                    snippet = report.description,
                                    onClick = {
                                        viewModel.onReportClicked(report)
                                        true
                                    }
                                )
                            }
                        }
                    }

                    CategoryFilter(
                        categories = uiState.categories,
                        selectedCategory = uiState.selectedCategory,
                        onCategoryClick = { viewModel.onCategorySelected(it) },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    )

                    if (uiState.selectedReport != null) {
                        ReportDetailCard(
                            report = uiState.selectedReport,
                            onDetailClick = {},
                            onCloseClick = { viewModel.onDismissReport() },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 32.dp)
                        )
                    }
                }
            }
        }
    }
}