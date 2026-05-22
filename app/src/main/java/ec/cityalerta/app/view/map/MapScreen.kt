package ec.cityalerta.app.view.map

import android.Manifest
import android.content.Context
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import ec.cityalerta.app.model.utils.GeoJsonConverter
import ec.cityalerta.app.view.map.components.CategoryFilter
import ec.cityalerta.app.view.map.components.ReportClusterMarker
import ec.cityalerta.app.view.map.components.ReportDetailCard
import ec.cityalerta.app.view.map.components.ReportMarkerDot
import ec.cityalerta.app.viewmodel.MapViewModel
import kotlinx.coroutines.launch
import org.json.JSONArray
import kotlin.math.*

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
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3
        val phi1 = lat1 * PI / 180
        val phi2 = lat2 * PI / 180
        val deltaPhi = (lat2 - lat1) * PI / 180
        val deltaLambda = (lon2 - lon1) * PI / 180

        val a = sin(deltaPhi / 2) * sin(deltaPhi / 2) +
                cos(phi1) * cos(phi2) *
                sin(deltaLambda / 2) * sin(deltaLambda / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }

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
                        GeoJsonConverter.extractPolygonPoints(ciudad.geojson)
                    }
                    val assetBounds = remember(ciudadId) { loadCityBboxFromAssets(context, ciudadId) }

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
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            isMyLocationEnabled = locationPermissionGranted,
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

                        uiState.barrioRisks.forEach { risk ->
                            key(risk.barrioId) {
                                Circle(
                                    center = risk.center,
                                    radius = risk.radius,
                                    fillColor = Color(risk.fillColor),
                                    strokeColor = Color(risk.strokeColor),
                                    strokeWidth = 2f
                                )
                            }
                        }

                        // Lógica de clustering optimizada
                        val zoom = cameraPositionState.position.zoom
                        
                        // Escalamos el radio de búsqueda según el zoom para que sea útil en niveles bajos
                        // Pero mantenemos los 170m como base en zoom alto (15+)
                        val baseRadius = 170.0
                        val clusterRadius = if (zoom < 14f) {
                            baseRadius * (14f - zoom + 1).toDouble().pow(1.5)
                        } else {
                            baseRadius
                        }

                        val processedMarkerIds = mutableSetOf<String>()
                        
                        // Agrupamos por categoría para que los clusters sean específicos por tipo de reporte
                        val reportsByCategory = uiState.reports
                            .filter { it.id in visibleReportIds }
                            .groupBy { it.categoria }

                        reportsByCategory.forEach { (category, categoryReports) ->
                            val categoryMarkerIds = categoryReports.map { it.id }.toSet()
                            val categoryMarkers = uiState.reportMarkers
                                .filter { it.id in categoryMarkerIds }

                            categoryMarkers.forEach { marker ->
                                if (marker.id !in processedMarkerIds) {
                                    val nearbyMarkers = categoryMarkers
                                        .filter { it.id !in processedMarkerIds }
                                        .filter { other ->
                                            calculateDistance(
                                                marker.latitude, marker.longitude,
                                                other.latitude, other.longitude
                                            ) <= clusterRadius
                                        }

                                    // Si hay 5 o más (incluyendo el actual) y no estamos en zoom máximo, agrupamos
                                    if (nearbyMarkers.size >= 5 && zoom < 17.5f) {
                                        val avgLat = nearbyMarkers.map { it.latitude }.average()
                                        val avgLng = nearbyMarkers.map { it.longitude }.average()
                                        
                                        key("cluster_${category.name}_${marker.id}") {
                                            ReportClusterMarker(
                                                position = LatLng(avgLat, avgLng),
                                                count = nearbyMarkers.size,
                                                reportType = category,
                                                onClick = {
                                                    viewModel.onClusterClicked(category, nearbyMarkers.size)
                                                }
                                            )
                                        }
                                        processedMarkerIds.addAll(nearbyMarkers.map { it.id })
                                    } else {
                                        // Renderizamos como punto individual
                                        key(marker.id) {
                                            ReportMarkerDot(
                                                marker = marker,
                                                reportType = category,
                                                onClick = {
                                                    viewModel.onReportClicked(marker.id)
                                                }
                                            )
                                        }
                                        processedMarkerIds.add(marker.id)
                                    }
                                }
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

                    MapControlButton(
                        icon = Icons.Default.MyLocation,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 124.dp, end = 16.dp),
                        containerColor = Color(0xFF051C3F),
                        contentColor = Color.White,
                        onClick = {
                            if (locationPermissionGranted) {
                                try {
                                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                        location?.let {
                                            scope.launch {
                                                cameraPositionState.animate(
                                                    CameraUpdateFactory.newLatLngZoom(
                                                        LatLng(it.latitude, it.longitude),
                                                        15f
                                                    )
                                                )
                                            }
                                        }
                                    }
                                } catch (_: SecurityException) {
                                }
                            }
                        }
                    )

                    // Zoom Controls
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 180.dp, end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MapControlButton(
                            icon = Icons.Default.Add,
                            onClick = {
                                scope.launch {
                                    cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                                }
                            }
                        )
                        MapControlButton(
                            icon = Icons.Default.Remove,
                            onClick = {
                                scope.launch {
                                    cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                                }
                            }
                        )
                    }

                    if (uiState.selectedReport != null) {
                        ReportDetailCard(
                            report = uiState.selectedReport,
                            isMultiReport = uiState.isMultiReport,
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
