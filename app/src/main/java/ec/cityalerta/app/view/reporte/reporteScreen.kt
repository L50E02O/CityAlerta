package ec.cityalerta.app.view.reporte

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.viewmodel.ReporteViewModel
import ec.cityalerta.app.view.style.ReportUiColors
import ec.cityalerta.app.view.style.ReportUiDimens
import ec.cityalerta.app.view.style.ReportUiShapes

@Composable()
fun ReporteScreen(
    viewModel: ReporteViewModel,
    onReportSent: () ->Unit
    ) {
    val descripcion by viewModel.descripcion.collectAsState()
    val categoria by viewModel.categoria.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val currentLocation by viewModel.currentLocation.collectAsState()
    val userLocation = currentLocation?.let { LatLng(it.latitude, it.longitude) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationPermissionGranted = isGranted
    }

    val defaultLocation = LatLng(-0.95, -80.73)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }

    Column(
        modifier = Modifier
            .background(ReportUiColors.ScreenBackground)
            .padding(ReportUiDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(ReportUiDimens.SectionSpacing)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Seleccione su ubicacion",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Confirme el punto exacto para el despliegue de seguridad.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF7D818C)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ReportUiDimens.MapHeight)
                .background(ReportUiColors.MapPlaceholder, ReportUiShapes.Card)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = locationPermissionGranted
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false
                )
            ) {
                userLocation?.let { location ->
                    Marker(state = MarkerState(location))
                }
            }

            Button(
                onClick = {
                    if (!locationPermissionGranted) {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    } else {
                        viewModel.requestCurrentLocation()
                    }
                },
                modifier = Modifier.align(androidx.compose.ui.Alignment.Center),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Usar mi ubicacion actual", color = Color(0xFF1B1B1B))
            }
        }

        Text("CATEGORIA", style = MaterialTheme.typography.labelSmall, color = ReportUiColors.HintText)

        CategoryDropDown(
            selected = categoria,
            onSelected = { viewModel.onCategoriaChange(it) }
        )

        Text("DESCRIPCION", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8A8D99))

        OutlinedTextField(
            value = descripcion,
            onValueChange = { viewModel.onDescriptionChange(it) },
            placeholder = { Text("Describa brevemente la situacion...") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                viewModel.sendReport(onSuccess = onReportSent)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = ReportUiShapes.Button,
            colors = ButtonDefaults.buttonColors(containerColor = ReportUiColors.AccentRed)
        ) {
            Text(
                text = "Enviar reporte",
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Text(
            text = "Al enviar este reporte, su ubicacion y datos de perfil seran compartidos con las autoridades locales de forma segura.",
            style = MaterialTheme.typography.bodySmall,
            color = ReportUiColors.HintText
        )

        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    androidx.compose.runtime.LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 16f)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropDown(
    selected: ReportType?,
    onSelected: (ReportType) -> Unit
){
    var expanded by remember { mutableStateOf(false) }
    val options = ReportType.values()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected?.toDisplayName() ?: "",
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Ubique la categoria del reporte") },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ){
            options.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.toDisplayName()) },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}