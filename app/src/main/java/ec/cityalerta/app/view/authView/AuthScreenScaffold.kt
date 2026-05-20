package ec.cityalerta.app.view.authView

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.viewmodel.AuthViewModel

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ec.cityalerta.app.R

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
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(viewModel.uiState.email).matches()
    val isPasswordValid = viewModel.uiState.password.isNotEmpty() && viewModel.uiState.password.length >= 8
    val isCiudadValid = !config.showCitySection ||
            (config.fixedCity != null || (config.ciudades.isNotEmpty() && viewModel.uiState.ciudadId.isNotEmpty()))
    val isFormValid = isEmailValid && isPasswordValid && isCiudadValid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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
            color = Color(0xFF1B2633),
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "REPORTE CIUDADANO",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6C757D),
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
                color = Color(0xFF1B2633)
            )

            config.subtitle?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6C757D)
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
                    viewModel = viewModel
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = config.onPrimaryAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isFormValid && !viewModel.uiState.isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935),
                    disabledContainerColor = Color(0xFFE53935).copy(alpha = 0.6f)
                )
            ) {
                Text(
                    text = if (viewModel.uiState.isLoading) "Cargando..." else "${config.primaryButtonText} →",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = config.secondaryActionText,
                    color = Color(0xFF6C757D),
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
            color = if (selected) Color(0xFF1B2633) else Color(0xFFADB5BD),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp
        )
        if (selected) {
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(0.4f),
                thickness = 2.dp,
                color = Color(0xFFE53935)
            )
        }
    }
}

@Composable
private fun AuthCitySection(
    config: AuthScreenConfig,
    viewModel: AuthViewModel
) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "PAÍS",
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFF6C757D),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    CountryField(country = "Ecuador")

    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "CIUDAD",
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFF6C757D),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    when {
        config.fixedCity != null -> FixedCityField(city = config.fixedCity)
        config.ciudades.isNotEmpty() -> CiudadDropdown(
            ciudades = config.ciudades,
            query = viewModel.uiState.ciudadNombre,
            selectedId = viewModel.uiState.ciudadId,
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
                unfocusedContainerColor = Color(0xFFF8F9FA),
                focusedContainerColor = Color(0xFFF8F9FA),
                unfocusedBorderColor = Color(0xFFE9ECEF),
                focusedBorderColor = Color(0xFF1B2633)
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
fun CountryField(
    country: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = country,
        onValueChange = {},
        readOnly = true,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFF8F9FA),
            focusedContainerColor = Color(0xFFF8F9FA),
            unfocusedBorderColor = Color(0xFFE9ECEF),
            focusedBorderColor = Color(0xFF1B2633)
        )
    )
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
            unfocusedContainerColor = Color(0xFFF8F9FA),
            focusedContainerColor = Color(0xFFF8F9FA),
            unfocusedBorderColor = Color(0xFFE9ECEF),
            focusedBorderColor = Color(0xFF1B2633)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CiudadDropdown(
    ciudades: List<Ciudad>,
    query: String,
    selectedId: String,
    onQueryChange: (String) -> Unit,
    onSelected: (Ciudad) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedNombre = ciudades.find { it.id == selectedId }?.nombre ?: ""
    val filteredCities = if (query.isBlank()) {
        ciudades
    } else {
        ciudades.filter { ciudad ->
            ciudad.nombre.contains(query, ignoreCase = true)
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = if (selectedId.isNotEmpty()) selectedNombre else query,
            onValueChange = {
                onQueryChange(it)
                if (!expanded) expanded = true
            },
            placeholder = { Text("Escribe o selecciona tu ciudad", color = Color(0xFFADB5BD)) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true).fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF8F9FA),
                focusedContainerColor = Color(0xFFF8F9FA),
                unfocusedBorderColor = Color(0xFFE9ECEF),
                focusedBorderColor = Color(0xFF1B2633)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            filteredCities.forEach { ciudad ->
                DropdownMenuItem(
                    text = { Text(ciudad.nombre) },
                    onClick = {
                        onSelected(ciudad)
                        expanded = false
                    }
                )
            }
        }
    }
}
