package ec.cityalerta.app.view.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.view.components.ProfileAvatar
import ec.cityalerta.app.view.profile.components.ProfileSettingsScaffold
import ec.cityalerta.app.view.profile.components.SettingsEditIcon
import ec.cityalerta.app.view.profile.components.SettingsInfoField
import ec.cityalerta.app.view.profile.components.SettingsNavigationRow
import ec.cityalerta.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    var userEmail by remember { mutableStateOf("") }
    var showEmailDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("Espanol") }

    LaunchedEffect(Unit) {
        viewModel.loadSummaryIfNeeded()
        userEmail = viewModel.getUserEmail().orEmpty()
    }

    ProfileSettingsScaffold(navController = navController, title = "Configuracion") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                "Configuracion",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B2633)
            )
            Text(
                "Gestiona tu perfil y las preferencias de seguridad de tu cuenta.",
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
                fontSize = 14.sp,
                color = Color(0xFF6C757D),
                lineHeight = 20.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ProfileAvatar(
                        imageUrl = state.profileImageUrl,
                        size = 72.dp,
                        isLoading = state.isUploadingImage
                    )
                    Column {
                        Text(
                            state.fullName.ifBlank { "Usuario" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF1B2633)
                        )
                        Text(
                            "MIEMBRO ACTIVO",
                            modifier = Modifier.padding(top = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3B5B7A),
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SettingsInfoField(
                label = "Correo actual",
                value = userEmail.ifBlank { "No disponible" },
                trailing = { SettingsEditIcon { showEmailDialog = true } }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsInfoField(
                label = "Ubicacion",
                value = state.cityName.ifBlank { "Ciudad no disponible" },
                trailing = {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF3B5B7A),
                        modifier = Modifier.size(22.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsNavigationRow(
                icon = Icons.Default.Language,
                title = "Cambiar idioma",
                subtitle = selectedLanguage,
                onClick = { showLanguageDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8E8E8))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Eliminar datos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1B2633)
                    )
                    Text(
                        "Esta accion es permanente y eliminara todo tu historial de reportes y configuracion.",
                        modifier = Modifier.padding(vertical = 8.dp),
                        fontSize = 13.sp,
                        color = Color(0xFF6C757D),
                        lineHeight = 18.sp
                    )
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
                    ) {
                        Text("Borrar la cuenta", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showEmailDialog) {
        AlertDialog(
            onDismissRequest = { showEmailDialog = false },
            title = { Text("Correo de la cuenta") },
            text = { Text(userEmail.ifBlank { "No hay correo asociado a esta sesion." }) },
            confirmButton = {
                TextButton(onClick = { showEmailDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Idioma") },
            text = {
                Column {
                    listOf("Espanol", "English").forEach { language ->
                        TextButton(
                            onClick = {
                                selectedLanguage = language
                                showLanguageDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                language,
                                fontWeight = if (selectedLanguage == language) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Borrar cuenta") },
            text = {
                Text("Se cerrara tu sesion y deberas contactar soporte para eliminar permanentemente los datos del servidor.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            viewModel.logOut {
                                navController.navigate(Routes.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    }
                ) {
                    Text("Confirmar", color = Color(0xFFE74C3C))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
