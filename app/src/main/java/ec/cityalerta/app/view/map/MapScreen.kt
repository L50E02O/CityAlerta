package ec.cityalerta.app.view.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.content.Context
import org.json.JSONArray
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import ec.cityalerta.app.view.components.ProfileAvatar
import ec.cityalerta.app.navigation.Routes
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.utils.GeoJsonConverter
import ec.cityalerta.app.view.map.components.CategoryFilter
import ec.cityalerta.app.view.map.components.ReportDetailCard
import ec.cityalerta.app.viewmodel.MapViewModel
import ec.cityalerta.app.viewmodel.MapUiState
import ec.cityalerta.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    ciudadId: String = "manta",
    viewModel: MapViewModel,
    profileViewModel: ProfileViewModel
) {
    val uiState = viewModel.uiState
    val profileState = profileViewModel.state.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionGranted = rememberLocationPermission()

    val defaultLocation = LatLng(-0.95, -80.73)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }

    LaunchedEffect(ciudadId) {
        viewModel.loadCiudad(ciudadId)
    }

    LaunchedEffect(Unit) {
        profileViewModel.loadSummary()
    }

    LaunchedEffect(uiState.ciudad, uiState.cameraZoom) {
        uiState.ciudad?.let { ciudad ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(ciudad.centroLat, ciudad.centroLng),
                uiState.cameraZoom
            )
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = uiState.errorMessage,
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
                            contentDescription = "Atras"
                        )
                    }
                },
                actions = {
                    ProfileAvatar(initials = profileState.initials, onClick = { navController.navigate(Routes.Profile.route) })
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
            MapScreenBody(
                uiState = uiState,
                ciudadId = ciudadId,
                dependencies = MapScreenDependencies(
                    context = context,
                    scope = scope,
                    fusedLocationClient = fusedLocationClient,
                    locationPermissionGranted = locationPermissionGranted,
                    cameraPositionState = cameraPositionState,
                    viewModel = viewModel
                )
            )
        }
    }
}

@Composable
private fun rememberLocationPermission(): Boolean {
    val context = LocalContext.current
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

    return locationPermissionGranted
}

private data class MapScreenDependencies(
    val context: Context,
    val scope: CoroutineScope,
    val fusedLocationClient: FusedLocationProviderClient,
    val locationPermissionGranted: Boolean,
    val cameraPositionState: CameraPositionState,
    val viewModel: MapViewModel
)

@Composable
private fun MapScreenBody(
    uiState: MapUiState,
    ciudadId: String,
    dependencies: MapScreenDependencies
) {
    Box(modifier = Modifier.fillMaxSize()) {
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
                MapCityContent(
                    uiState = uiState,
                    ciudad = uiState.ciudad,
                    ciudadId = ciudadId,
                    dependencies = dependencies
                )
            }
        }
    }
}

@Composable
private fun BoxScope.MapCityContent(
    uiState: MapUiState,
    ciudad: Ciudad,
    ciudadId: String,
    dependencies: MapScreenDependencies
) {
    val polygonPoints = remember(ciudad) {
        GeoJsonConverter.extractPolygonPoints(ciudad.geojson)
    }
    val assetBounds = remember(ciudadId) { loadCityBboxFromAssets(dependencies.context, ciudadId) }

    val cityBounds = remember(polygonPoints, assetBounds) {
        assetBounds ?: (buildCityBounds(polygonPoints) ?: LatLngBounds(
            LatLng(ciudad.centroLat - 0.12, ciudad.centroLng - 0.12),
            LatLng(ciudad.centroLat + 0.12, ciudad.centroLng + 0.12)
        ))
    }

    val filteredReports = remember(uiState.reports, uiState.selectedCategory) {
        uiState.reports.filter {
            uiState.selectedCategory == null || it.categoria == uiState.selectedCategory
        }
    }

    val visibleReportIds = remember(filteredReports) {
        filteredReports.map { it.id }.toSet()
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = dependencies.cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = dependencies.locationPermissionGranted,
            latLngBoundsForCameraTarget = cityBounds,
            minZoomPreference = 12f,
            maxZoomPreference = 18f
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        ),
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

        uiState.reportMarkers
            .filter { it.id in visibleReportIds }
            .forEach { marker ->
                key(marker.id) {
                    Marker(
                        state = rememberMarkerState(
                            position = LatLng(marker.latitude, marker.longitude)
                        ),
                        title = marker.title,
                        snippet = marker.description ?: "",
                        onClick = {
                            dependencies.viewModel.onReportClicked(marker.id)
                            true
                        }
                    )
                }
            }
    }

    CategoryFilter(
        categories = uiState.categories,
        selectedCategory = uiState.selectedCategory,
        onCategoryClick = { dependencies.viewModel.onCategorySelected(it) },
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 8.dp)
    )

    MapControlButton(
        icon = Icons.Default.MyLocation,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 124.dp, end = 16.dp),
        containerColor = Color(0xFF051C3F),
        contentColor = Color.White,
        onClick = { centerMapOnUserLocation(dependencies) }
    )

    Column(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(bottom = 180.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MapControlButton(
            icon = Icons.Default.Add,
            onClick = {
                dependencies.scope.launch {
                    dependencies.cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                }
            }
        )
        MapControlButton(
            icon = Icons.Default.Remove,
            onClick = {
                dependencies.scope.launch {
                    dependencies.cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                }
            }
        )
    }

    if (uiState.selectedReport != null) {
        ReportDetailCard(
            report = uiState.selectedReport,
            onDetailClick = {},
            onCloseClick = { dependencies.viewModel.onDismissReport() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

private fun centerMapOnUserLocation(dependencies: MapScreenDependencies) {
    if (!dependencies.locationPermissionGranted) return

    try {
        dependencies.fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                dependencies.scope.launch {
                    dependencies.cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.latitude, it.longitude),
                            15f
                        )
                    )
                }
            }
        }
    } catch (_: SecurityException) {
        // Sin permiso de ubicacion
    }
}

@Composable
fun MapControlButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFE3E9F0),
    contentColor: Color = Color(0xFF051C3F)
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
        }
    }
}

private fun buildCityBounds(points: List<LatLng>): LatLngBounds? {
    if (points.isEmpty()) return null

    var minLat = points.first().latitude
    var maxLat = points.first().latitude
    var minLng = points.first().longitude
    var maxLng = points.first().longitude

    points.forEach { point ->
        minLat = minOf(minLat, point.latitude)
        maxLat = maxOf(maxLat, point.latitude)
        minLng = minOf(minLng, point.longitude)
        maxLng = maxOf(maxLng, point.longitude)
    }

    return LatLngBounds(
        LatLng(minLat, minLng),
        LatLng(maxLat, maxLng)
    )
}

private fun loadCityBboxFromAssets(context: Context, ciudadId: String): LatLngBounds? {
    return try {
        val input = context.assets.open("cities.json")
        val json = input.bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val id = obj.optString("id", obj.optString("name", "")).lowercase()
            if (id == ciudadId.lowercase()) {
                val bbox = obj.getJSONObject("bbox")
                val minLat = bbox.getDouble("minLat")
                val maxLat = bbox.getDouble("maxLat")
                val minLng = bbox.getDouble("minLng")
                val maxLng = bbox.getDouble("maxLng")
                return LatLngBounds(LatLng(minLat, minLng), LatLng(maxLat, maxLng))
            }
        }
        null
    } catch (_: Exception) {
        null
    }
}
