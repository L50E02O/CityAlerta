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
import androidx.compose.material3.Text
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

@Composable
fun AuthScreenScaffold(
    title: String,
    primaryButtonText: String,
    secondaryActionText: String,
    viewModel: AuthViewModel,
    showCitySection: Boolean = false,
    ciudades: List<Ciudad> = emptyList(),
    cityLoadError: String? = null,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit
) {
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(viewModel.uiState.email).matches()
    val isPasswordValid = viewModel.uiState.password.isNotEmpty() && viewModel.uiState.password.length >= 8
    val isCiudadValid = !showCitySection || (ciudades.isNotEmpty() && viewModel.uiState.ciudadId.isNotEmpty())
    val isFormValid = isEmailValid && isPasswordValid && isCiudadValid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        AuthFormComponent(
            viewModel = viewModel,
            modifier = Modifier.fillMaxWidth()
        )

        if (showCitySection) {
            Spacer(modifier = Modifier.height(8.dp))
            CountryField(country = "Ecuador")
            Spacer(modifier = Modifier.height(8.dp))
            if (ciudades.isNotEmpty()) {
                CiudadDropdown(
                    ciudades = ciudades,
                    query = viewModel.uiState.ciudadNombre,
                    selectedId = viewModel.uiState.ciudadId,
                    onQueryChange = viewModel::onCiudadChange,
                    onSelected = { ciudad ->
                        viewModel.onCiudadSelected(ciudad.nombre, ciudad.id)
                    }
                )
            } else {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ciudad") },
                    placeholder = { Text("No hay ciudades disponibles") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (cityLoadError != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = cityLoadError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onPrimaryAction,
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid && !viewModel.uiState.isLoading
        ) {
            Text(text = if (viewModel.uiState.isLoading) "Cargando..." else primaryButtonText)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = secondaryActionText,
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable(onClick = onSecondaryAction)
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
        label = { Text("Pais") },
        modifier = modifier.fillMaxWidth()
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
            label = { Text("Ciudad") },
            placeholder = { Text("Escribe o selecciona tu ciudad") },
            modifier = Modifier.menuAnchor().fillMaxWidth()
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
