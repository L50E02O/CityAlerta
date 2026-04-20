package man.tap.view.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import man.tap.model.utils.GeoJsonConverter
import man.tap.viewmodel.MapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    ciudadId: String = "manta",
    viewModel: MapViewModel
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }

    // Inicializar cameraPositionState fuera del when para evitar recomposiciones innecesarias
    val defaultLocation = LatLng(-0.95, -80.73)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }

    LaunchedEffect(ciudadId) {
        viewModel.loadCiudad(ciudadId)
    }

    // Actualizar posicion de camara cuando cambia la ciudad
    LaunchedEffect(uiState.ciudad) {
        uiState.ciudad?.let { ciudad ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(ciudad.centro_lat, ciudad.centro_lng),
                uiState.cameraZoom
            )
        }
    }

    // Mostrar Snackbar si punto esta fuera del poligono
    LaunchedEffect(uiState.isPointValid) {
        if (uiState.isPointValid == false) {
            snackbarHostState.showSnackbar(
                message = "El punto debe estar dentro del area permitida",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.ciudad?.nombre ?: "Mapa") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atras")
                    }
                },
                actions = {
                    if (uiState.marcadores.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearMarkers() }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Limpiar marcadores")
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
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                uiState.ciudad != null -> {
                    val ciudad = uiState.ciudad

                    // Extraer puntos del poligono
                    val polygonPoints = GeoJsonConverter.extractPolygonPoints(
                        ciudad.geojson.features.firstOrNull()?.geometry
                            ?: return@Box
                    )

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            viewModel.onMapClicked(latLng)
                        }
                    ) {
                        // Renderizar poligono del area permitida
                        if (polygonPoints.isNotEmpty()) {
                            Polygon(
                                points = polygonPoints,
                                fillColor = Color(0x4287F5CC),
                                strokeColor = Color(0xFF287FCC),
                                strokeWidth = 2f
                            )
                        }

                        // Renderizar marcadores agregados por el usuario
                        uiState.marcadores.forEach { marker ->
                            val markerState = rememberMarkerState(
                                position = LatLng(marker.latitude, marker.longitude)
                            )
                            Marker(
                                state = markerState,
                                title = marker.title,
                                snippet = marker.description
                            )
                        }
                    }
                }
            }
        }
    }
}
