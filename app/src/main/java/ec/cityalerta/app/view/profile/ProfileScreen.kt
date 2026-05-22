package ec.cityalerta.app.view.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.view.components.AppTopBar
import ec.cityalerta.app.view.components.ProfileAvatar
import ec.cityalerta.app.view.utils.rememberProfileImagePicker
import ec.cityalerta.app.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val state by viewModel.state.collectAsState()
    val launchProfileImagePicker = rememberProfileImagePicker(
        hasCustomImage = !state.profileImageId.isNullOrBlank(),
        onImageBytes = { bytes -> viewModel.updateProfileImage(bytes) },
        onDeleteImage = { viewModel.deleteProfileImage() }
    )

    LaunchedEffect(Unit) {
        viewModel.loadSummary()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Perfil",
                showBack = true,
                showProfile = false,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = Color(0xFFF6F7F9)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.fillMaxSize().padding(24.dp))
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(28.dp))
                        ProfileAvatar(
                            imageUrl = state.profileImageUrl,
                            size = 96.dp,
                            isLoading = state.isUploadingImage,
                            onClick = launchProfileImagePicker
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Toca la foto para cambiarla",
                            fontSize = 12.sp,
                            color = Color(0xFF6C757D)
                        )
                        state.errorMessage?.let { message ->
                            Text(message, fontSize = 12.sp, color = Color(0xFFE74C3C))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(state.fullName.ifBlank { "Usuario" }, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B2633))
                        Text(state.cityName.ifBlank { "Ciudad no disponible" }, fontSize = 14.sp, color = Color(0xFF6C757D))

                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { navController.navigate(Routes.MyReports.route) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(state.totalReports.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text("REPORTES", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(state.resolvedReports.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text("RESUELTOS", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { navController.navigate(Routes.MyReports.route) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Ver mis reportes")
                            }
                            Button(
                                onClick = {
                                    viewModel.logOut {
                                        navController.navigate(Routes.Login.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
                            ) {
                                Text("Cerrar sesion", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
