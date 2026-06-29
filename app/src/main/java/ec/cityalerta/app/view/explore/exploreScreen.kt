package ec.cityalerta.app.view.explore

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ec.cityalerta.app.R
import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.view.components.ActionFeedbackHandler
import ec.cityalerta.app.view.components.AppTopBar
import ec.cityalerta.app.viewmodel.AuthViewModel
import ec.cityalerta.app.viewmodel.ExploreViewModel
import ec.cityalerta.app.viewmodel.ProfileViewModel
import ec.cityalerta.app.viewmodel.ReporteViewModel
import androidx.core.content.ContextCompat

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton

@Composable
fun ExploreScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    viewModel: ExploreViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
    reporteViewModel: ReporteViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val profileState by profileViewModel.state.collectAsStateWithLifecycle()
    val reportInfoMessage by reporteViewModel.infoMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ActionFeedbackHandler(
        infoMessage = reportInfoMessage,
        errorMessage = null,
        snackbarHostState = snackbarHostState,
        onDismissInfo = { reporteViewModel.clearInfoMessage() }
    )

    rememberNotificationPermission(
        onGranted = { authViewModel.onNotificationsPermissionGranted() },
        onDenied = { authViewModel.onNotificationsPermissionDenied() }
    )

    LaunchedEffect(Unit) {
        viewModel.loadData()
        profileViewModel.loadSummaryIfNeeded()
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = state.ciudadNombre,
                label = stringResource(R.string.explore_location_label),
                showSearch = true,
                profileImageUrl = profileState.profileImageUrl,
                isProfileLoading = profileState.isUploadingImage,
                onSearchClick = { navController.navigate(Routes.Search.route) },
                onProfileClick = { navController.navigate(Routes.Profile.route) }
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 1100.dp)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.explore_title),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = stringResource(R.string.explore_subtitle),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    if (state.reportes.isEmpty() && state.isLoading) {
                        // 1. Carga inicial: No hay nada en Room y estamos pidiendo a Supabase
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    } else if (state.reportes.isEmpty() && !state.isLoading) {
                        // 2. Estado vacío: Ni en Room ni en Supabase hay nada
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No hay reportes en esta zona",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // 3. Mostrar la lista (tenga o no carga en fondo)
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                contentPadding = PaddingValues(bottom = 20.dp)
                            ) {
                                items(
                                    items = state.reportes,
                                    key = { it.id }
                                ) { reporte ->
                                    ReporteCard(
                                        reporte = reporte,
                                        onClick = { id ->
                                            navController.navigate(
                                                Routes.ReportDetail.route.replace(
                                                    "{reportId}",
                                                    id
                                                )
                                            )
                                        }
                                    )
                                }
                            }

                            // Controles de Paginación siempre visibles al final si hay páginas
                            if (state.reportes.isNotEmpty() || state.currentPage > 1) {
                                PaginationControls(
                                    currentPage = state.currentPage,
                                    hasNextPage = state.hasNextPage,
                                    onNext = { viewModel.nextPage() },
                                    onPrevious = { viewModel.previousPage() }
                                )
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                        
                        // 4. Indicador sutil de actualización en la parte superior
                        if (state.isSyncing) {
                            androidx.compose.material3.LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaginationControls(
    currentPage: Int,
    hasNextPage: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onPrevious,
            enabled = currentPage > 1,
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Spacer(Modifier.widthIn(8.dp))
            Text("Anterior")
        }

        Text(
            text = "Página $currentPage",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Button(
            onClick = onNext,
            enabled = hasNextPage,
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Siguiente")
            Spacer(Modifier.widthIn(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}
@Composable
private fun rememberNotificationPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit
): Boolean {
    val context = LocalContext.current
    val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    var isGranted by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(
            !needsPermission || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isGranted = granted
        if (granted) onGranted() else onDenied()
    }

    LaunchedEffect(Unit) {
        if (needsPermission) {
            if (!isGranted) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                onGranted()
            }
        } else {
            onGranted()
        }
    }

    return isGranted
}
