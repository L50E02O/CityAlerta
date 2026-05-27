package ec.cityalerta.app.view.reporte.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import ec.cityalerta.app.view.style.ReportUiDimens
import ec.cityalerta.app.view.style.ReportUiShapes

@Composable
fun ReporteMapPicker(
    selectedLocation: LatLng?,
    onLocationSelected: (LatLng) -> Unit,
    onRequestCurrentLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation ?: LatLng(-0.95, -80.73), 15f)
    }

    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showMyLocation by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationPermissionGranted = isGranted
        if (isGranted) {
            showMyLocation = true
            onRequestCurrentLocation()
        }
    }

    // Centrar el mapa cuando cambia la ubicación (ej: al presionar "Usar mi ubicación")
    LaunchedEffect(selectedLocation) {
        selectedLocation?.let {
            if (cameraPositionState.position.target != it) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 16f)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ReportUiDimens.MapHeight)
            .background(MaterialTheme.colorScheme.outlineVariant, ReportUiShapes.Card)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                onLocationSelected(latLng)
            },
            properties = MapProperties(
                isMyLocationEnabled = locationPermissionGranted && showMyLocation
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false
            )
        ) {
            selectedLocation?.let {
                Marker(state = MarkerState(it))
            }
        }

        Button(
            onClick = {
                if (!locationPermissionGranted) {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                } else {
                    showMyLocation = !showMyLocation
                    if (showMyLocation) {
                        onRequestCurrentLocation()
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (showMyLocation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                contentColor = if (showMyLocation) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(if (showMyLocation) "Desactivar mi ubicación" else "Usar mi ubicación actual")
        }
    }
}
