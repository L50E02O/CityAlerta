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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ec.cityalerta.app.R
import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.view.components.ProfileAvatar
import ec.cityalerta.app.view.profile.components.SettingsNavigationRow
import ec.cityalerta.app.view.utils.rememberProfileImagePicker
import ec.cityalerta.app.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDashboardScreen(
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
        viewModel.loadDashboard()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atras")
                    }
                }
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
                            .verticalScroll(rememberScrollState())
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
                            stringResource(R.string.profile_tap_photo),
                            fontSize = 12.sp,
                            color = Color(0xFF6C757D)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            state.fullName.ifBlank { stringResource(R.string.profile_user_fallback) },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B2633)
                        )
                        Text(
                            state.cityName.ifBlank { stringResource(R.string.profile_city_unavailable) },
                            fontSize = 14.sp,
                            color = Color(0xFF6C757D)
                        )

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
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(state.totalReports.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text(stringResource(R.string.profile_reports), fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(state.resolvedReports.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text(stringResource(R.string.profile_resolved), fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SettingsNavigationRow(
                                icon = Icons.Default.Settings,
                                title = stringResource(R.string.profile_settings_config),
                                subtitle = stringResource(R.string.profile_settings_config_sub),
                                onClick = { navController.navigate(Routes.Settings.route) }
                            )
                            SettingsNavigationRow(
                                icon = Icons.Default.Palette,
                                title = stringResource(R.string.profile_settings_appearance),
                                subtitle = stringResource(R.string.profile_settings_appearance_sub),
                                onClick = { navController.navigate(Routes.Appearance.route) }
                            )
                            SettingsNavigationRow(
                                icon = Icons.Default.AccessibilityNew,
                                title = stringResource(R.string.profile_settings_accessibility),
                                subtitle = stringResource(R.string.profile_settings_accessibility_sub),
                                onClick = { navController.navigate(Routes.Accessibility.route) }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { navController.navigate(Routes.MyReports.route) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.profile_view_reports))
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
                                Text(stringResource(R.string.profile_logout), color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
