package ec.cityalerta.app.view.authView

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ec.cityalerta.app.R
import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.view.components.CityPickerSheet
import ec.cityalerta.app.viewmodel.AuthViewModel

data class AuthScreenConfig(
    val title: String,
    val subtitle: String? = null,
    val primaryButtonText: String,
    val secondaryActionText: String,
    val isLogin: Boolean = true,
    val showCitySection: Boolean = false,
    val ciudades: List<Ciudad> = emptyList(),
    val fixedCity: Ciudad? = null,
    val cityLoadError: String? = null,
    val onPrimaryAction: () -> Unit,
    val onSecondaryAction: () -> Unit,
    val onTabSwitch: () -> Unit,
    val bottomContent: (@Composable () -> Unit)? = null
)

@Composable
fun AuthScreenScaffold(
    viewModel: AuthViewModel,
    config: AuthScreenConfig
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(uiState.email).matches()
    val isPasswordValid = uiState.password.isNotEmpty() && uiState.password.length >= 8
    val isCiudadValid = !config.showCitySection ||
            (config.fixedCity != null || (config.ciudades.isNotEmpty() && uiState.ciudadId.isNotEmpty()))
    val isFormValid = isEmailValid && isPasswordValid && isCiudadValid

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo Section
            Image(
                painter = painterResource(id = R.drawable.cityalerta_logo),
                contentDescription = "CityAlerta Logo",
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "CityAlerta",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "REPORTE CIUDADANO",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Tabs Section
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AuthTab(
                    text = "Login",
                    selected = config.isLogin,
                    onClick = { if (!config.isLogin) config.onTabSwitch() }
                )
                AuthTab(
                    text = "Registro",
                    selected = !config.isLogin,
                    onClick = { if (config.isLogin) config.onTabSwitch() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Text(
                    text = config.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                config.subtitle?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AuthFormComponent(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxWidth()
                )

                if (config.showCitySection) {
                    AuthCitySection(
                        config = config,
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = config.onPrimaryAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = isFormValid && !uiState.isLoading,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        text = if (uiState.isLoading) "Cargando..." else "${config.primaryButtonText} →",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = config.secondaryActionText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable(onClick = config.onSecondaryAction)
                    )
                }

                config.bottomContent?.let { content ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        content()
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun AuthTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp
        )
        if (selected) {
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(0.4f),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AuthCitySection(
    config: AuthScreenConfig,
    viewModel: AuthViewModel,
    uiState: ec.cityalerta.app.viewmodel.AuthState
) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "CIUDAD",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    when {
        config.fixedCity != null -> FixedCityField(city = config.fixedCity)
        config.ciudades.isNotEmpty() -> CiudadPickerField(
            ciudades = config.ciudades,
            query = uiState.ciudadNombre,
            selectedId = uiState.ciudadId,
            onQueryChange = viewModel::onCiudadChange,
            onSelected = { ciudad ->
                viewModel.onCiudadSelected(ciudad.nombre, ciudad.id)
            }
        )
        else -> OutlinedTextField(
            value = "",
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("No hay ciudades disponibles") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }

    if (config.cityLoadError != null) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = config.cityLoadError,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun FixedCityField(
    city: Ciudad,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = city.nombre,
        onValueChange = {},
        readOnly = true,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedBorderColor = MaterialTheme.colorScheme.secondary,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            disabledTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun CiudadPickerField(
    ciudades: List<Ciudad>,
    query: String,
    selectedId: String,
    onQueryChange: (String) -> Unit,
    onSelected: (Ciudad) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    val selectedNombre = ciudades.find { it.id == selectedId }?.nombre ?: ""

    Box(modifier = Modifier.fillMaxWidth().clickable { showSheet = true }) {
        OutlinedTextField(
            value = selectedNombre,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Selecciona tu ciudad", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }

    CityPickerSheet(
        visible = showSheet,
        cities = ciudades,
        query = query,
        onQueryChange = onQueryChange,
        onDismiss = { showSheet = false },
        onCitySelected = { ciudad ->
            onSelected(ciudad)
            showSheet = false
        }
    )
}
