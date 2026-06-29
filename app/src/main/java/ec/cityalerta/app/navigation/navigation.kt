package ec.cityalerta.app.navigation

import ec.cityalerta.app.model.repository.AuthRepository
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.session.SessionManager
import ec.cityalerta.app.model.session.SessionState
import ec.cityalerta.app.view.SplashScreen
import ec.cityalerta.app.view.authView.LoginScreen
import ec.cityalerta.app.view.authView.RecoverPasswordScreen
import ec.cityalerta.app.view.authView.RegisterScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import ec.cityalerta.app.view.map.MapScreen
import ec.cityalerta.app.viewmodel.AuthViewModel
import ec.cityalerta.app.viewmodel.MapViewModel
import ec.cityalerta.app.viewmodel.ExploreViewModel
import ec.cityalerta.app.viewmodel.PasswordRecoveryViewModel
import ec.cityalerta.app.viewmodel.ProfileViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import ec.cityalerta.app.viewmodel.AppViewModelFactory
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import ec.cityalerta.app.view.components.AppBottomBar
import ec.cityalerta.app.view.explore.ExploreScreen
import ec.cityalerta.app.view.explore.ReportDetailScreen
import ec.cityalerta.app.viewmodel.ReportDetailViewModel
import androidx.navigation.NavController
import ec.cityalerta.app.view.camera.PhotoScreen
import ec.cityalerta.app.view.reporte.ReporteScreen
import ec.cityalerta.app.viewmodel.ReporteViewModel
import ec.cityalerta.app.view.profile.AccessibilityScreen
import ec.cityalerta.app.view.profile.AppearanceScreen
import ec.cityalerta.app.view.profile.MyReportsScreen
import ec.cityalerta.app.view.profile.ProfileDashboardScreen
import ec.cityalerta.app.view.profile.SettingsScreen
import ec.cityalerta.app.view.search.SearchScreen
import ec.cityalerta.app.viewmodel.SearchReportViewModel
import androidx.compose.runtime.LaunchedEffect
import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import ec.cityalerta.app.model.utils.AuthDeepLinkParser
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.PermanentNavigationDrawer
import ec.cityalerta.app.view.components.AppNavigationRail
import ec.cityalerta.app.view.components.AppNavigationDrawer
import ec.cityalerta.app.view.utils.CityAlertaNavigationType
import androidx.compose.ui.unit.dp
import ec.cityalerta.app.view.profile.HelpScreen

@Composable
fun AppNavigation(
    startDestination: String = Routes.Splash.route,
    authInfoMessage: String? = null,
    navigationType: CityAlertaNavigationType = CityAlertaNavigationType.BOTTOM_NAVIGATION
) {

    val navController = rememberNavController()
    val authRepository = remember { AuthRepository() }
    val context = LocalContext.current
    val activity = context as? Activity
    val currentIntent = activity?.intent
    val factory = remember { AppViewModelFactory(authRepository, context) }
    val sessionManager = remember { SessionManager(SupabaseProvider.client.auth) }
    val sessionState by sessionManager.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = factory
    )
    val mapViewModel: MapViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = factory
    )

    val exploreViewModel: ExploreViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = factory
    )

    val profileViewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = factory
    )
    
    val reporteViewModel: ReporteViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = factory
    )
    val recoveryViewModel: PasswordRecoveryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = factory
    )
    val searchReportViewModel: SearchReportViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = factory
    )

    LaunchedEffect(Unit) {
        profileViewModel.loadSummaryIfNeeded()
    }

    DisposableEffect(sessionManager) {
        sessionManager.start()
        onDispose {
            sessionManager.stop()
        }
    }

    DisposableEffect(lifecycleOwner, sessionManager) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                scope.launch {
                    sessionManager.refreshIfNeeded()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val profileState by profileViewModel.state.collectAsStateWithLifecycle()

    val reportIdFromIntent = remember(currentIntent) {
        currentIntent?.getStringExtra("reporte_id")
    }

    LaunchedEffect(reportIdFromIntent) {
        if (!reportIdFromIntent.isNullOrBlank() && !AuthDeepLinkParser.isAppAuthDeepLink(currentIntent)) {
            if (sessionState is SessionState.Authenticated) {
                navController.navigate(Routes.ReportDetail.route.replace("{reportId}", reportIdFromIntent)) {
                    launchSingleTop = true
                }
            }
            currentIntent?.removeExtra("reporte_id")
        }
    }

    val ciudadIdToUse = if (profileState.ciudadId.isBlank()) "Sin ciudad" else profileState.ciudadId

    val navHostContent: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit = { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding()).fillMaxSize()
        ) {
            composable(Routes.Splash.route) {
                SplashScreen(
                    onTimeout = {
                        val currentState = sessionState
                        android.util.Log.d("SessionDebug", "Splash timeout - Estado actual: $currentState")
                        val hasSession = currentState !is SessionState.Unauthenticated && 
                                       !(currentState is SessionState.Error && !currentState.isTransient)
                        val linkType = AuthDeepLinkParser.parseType(currentIntent)
                        val isDeepLink = AuthDeepLinkParser.isAppAuthDeepLink(currentIntent)
                        val nextRoute = if (!isDeepLink) {
                            if (hasSession) Routes.Home.route else Routes.Login.route
                        } else {
                            when {
                                linkType == AuthDeepLinkParser.AuthLinkType.RECOVERY -> Routes.RecoverPassword.route
                                hasSession -> Routes.Home.route
                                else -> Routes.Login.route
                            }
                        }
                        navController.navigate(nextRoute) {
                            popUpTo(Routes.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.Login.route) {
                LoginScreen(
                    navController = navController,
                    viewModel = authViewModel,
                    authInfoMessage = authInfoMessage
                )
            }
            composable(Routes.Register.route) {
                RegisterScreen(navController, authViewModel)
            }
            composable(Routes.RecoverPassword.route) {
                RecoverPasswordScreen(navController, recoveryViewModel)
            }
            composable(Routes.Home.route) {
                RequireAuth(navController, sessionState) {
                    ExploreScreen(navController, authViewModel, exploreViewModel, profileViewModel, reporteViewModel)
                }
            }
            composable(Routes.Explore.route){
                RequireAuth(navController, sessionState) {
                    ExploreScreen(navController, authViewModel, exploreViewModel, profileViewModel, reporteViewModel)
                }
            }

            composable(Routes.Search.route) {
                RequireAuth(navController, sessionState) {
                    SearchScreen(navController, searchReportViewModel, profileViewModel)
                }
            }


            composable(Routes.Post.route) {
                RequireAuth(navController, sessionState) {
                    PhotoScreen(
                        navController = navController,
                        viewModel = reporteViewModel,
                        profileViewModel = profileViewModel,
                        onPhotoCaptured = {
                            navController.navigate(Routes.ReporteForm.route)
                        }
                    )
                }
            }

            composable(Routes.ReporteForm.route) {
                RequireAuth(navController, sessionState) {
                    ReporteScreen(
                        navController = navController,
                        viewModel = reporteViewModel,
                        profileViewModel = profileViewModel,
                        onReportSent = {
                            navController.navigate(Routes.Explore.route) {
                                popUpTo(Routes.Post.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            composable(
                route = Routes.Map.route,
                arguments = listOf(
                    navArgument("ciudadId") { type = NavType.StringType },
                    navArgument("reportId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val ciudadId = backStackEntry.arguments?.getString("ciudadId") ?: "Sin ciudad"
                val reportId = backStackEntry.arguments?.getString("reportId")
                MapScreen(navController, ciudadId, reportId, mapViewModel, profileViewModel)
            }
            composable(Routes.Profile.route) {
                RequireAuth(navController, sessionState) {
                    ProfileDashboardScreen(navController, profileViewModel)
                }
            }
            composable(
                route = Routes.MyReports.route,
                arguments = listOf(
                    navArgument("resolved") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val resolved = backStackEntry.arguments?.getString("resolved") == "true"
                RequireAuth(navController, sessionState) {
                    MyReportsScreen(navController, profileViewModel, filterResolved = resolved)
                }
            }
            composable(
                route = Routes.ReportDetail.route,
                arguments = listOf(navArgument("reportId") { type = NavType.StringType })
            ) { backStackEntry ->
                val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
                val detailViewModel: ReportDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = factory
                )
                RequireAuth(navController, sessionState) {
                    ReportDetailScreen(navController, reportId, detailViewModel, profileViewModel)
                }
            }
            composable(Routes.Settings.route) {
                RequireAuth(navController, sessionState) {
                    SettingsScreen(navController, profileViewModel, authViewModel)
                }
            }
            composable(Routes.Appearance.route) {
                RequireAuth(navController, sessionState) {
                    AppearanceScreen(navController)
                }
            }
            composable(Routes.Accessibility.route) {
                RequireAuth(navController, sessionState) {
                    AccessibilityScreen(navController)
                }
            }
            composable(Routes.Help.route) {
                RequireAuth(navController, sessionState) {
                    HelpScreen(navController)
                }
            }
        }
    }

    when (navigationType) {
        CityAlertaNavigationType.BOTTOM_NAVIGATION -> {
            Scaffold(
                bottomBar = { AppBottomBar(navController, ciudadId = ciudadIdToUse) }
            ) { innerPadding ->
                navHostContent(innerPadding)
            }
        }
        CityAlertaNavigationType.NAVIGATION_RAIL -> {
            Scaffold(
                bottomBar = {}
            ) { innerPadding ->
                Row(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                    AppNavigationRail(navController, ciudadId = ciudadIdToUse)
                    navHostContent(androidx.compose.foundation.layout.PaddingValues(0.dp))
                }
            }
        }
        CityAlertaNavigationType.PERMANENT_NAVIGATION_DRAWER -> {
            PermanentNavigationDrawer(
                drawerContent = { AppNavigationDrawer(navController, ciudadId = ciudadIdToUse) }
            ) {
                Scaffold { innerPadding ->
                    navHostContent(innerPadding)
                }
            }
        }
    }
}

@Composable
private fun RequireAuth(
    navController: NavController,
    sessionState: SessionState,
    content: @Composable () -> Unit
) {
    when (sessionState) {
        is SessionState.Authenticated -> content()
        is SessionState.Refreshing -> Unit
        is SessionState.Error -> {
            if (sessionState.isTransient) {
                content()
            } else {
                LaunchedEffect(sessionState) {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
        else -> {
            LaunchedEffect(sessionState) {
                navController.navigate(Routes.Login.route) {
                    popUpTo(Routes.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }
}