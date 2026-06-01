package ec.cityalerta.app.navigation

import ec.cityalerta.app.model.repository.AuthRepository
import ec.cityalerta.app.view.SplashScreen
import ec.cityalerta.app.view.authView.LoginScreen
import ec.cityalerta.app.view.authView.RecoverPasswordScreen
import ec.cityalerta.app.view.authView.RegisterScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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
import ec.cityalerta.app.model.utils.AuthDeepLinkParser
import androidx.compose.runtime.collectAsState

@Composable
fun AppNavigation(
    startDestination: String = Routes.Splash.route,
    authInfoMessage: String? = null
) {

    val navController = rememberNavController()
    val authRepository = remember { AuthRepository() }
    val context = LocalContext.current
    val activity = context as? Activity
    val currentIntent = activity?.intent
    val factory = remember { AppViewModelFactory(authRepository, context) }

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

    LaunchedEffect(currentIntent) {
        profileViewModel.loadSummaryIfNeeded()
        val reporteId = currentIntent?.getStringExtra("reporte_id")
        if (!reporteId.isNullOrBlank()) {
            navController.navigate(Routes.ReportDetail.route.replace("{reportId}", reporteId))
            currentIntent.removeExtra("reporte_id")
        }
    }



    val profileState = profileViewModel.state.collectAsState().value

    Scaffold(
        bottomBar = { AppBottomBar(navController, ciudadId = if (profileState.ciudadId.isBlank()) "Sin ciudad" else profileState.ciudadId) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
        ) {
            composable(Routes.Splash.route) {
                SplashScreen(
                    onTimeout = {
                        val hasSession = authRepository.getCurrentSession() != null
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
                RequireAuth(navController, authRepository) {
                    ExploreScreen(navController, authViewModel, exploreViewModel, profileViewModel)
                }
            }
            composable(Routes.Explore.route){
                RequireAuth(navController, authRepository) {
                    ExploreScreen(navController, authViewModel, exploreViewModel, profileViewModel)
                }
            }

            composable(Routes.Search.route) {
                RequireAuth(navController, authRepository) {
                    SearchScreen(navController, searchReportViewModel, profileViewModel)
                }
            }


            composable(Routes.Post.route) {
                RequireAuth(navController, authRepository) {
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
                RequireAuth(navController, authRepository) {
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
                RequireAuth(navController, authRepository) {
                    ProfileDashboardScreen(navController, profileViewModel)
                }
            }
            composable(Routes.MyReports.route) {
                RequireAuth(navController, authRepository) {
                    MyReportsScreen(navController, profileViewModel)
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
                RequireAuth(navController, authRepository) {
                    ReportDetailScreen(navController, reportId, detailViewModel, profileViewModel)
                }
            }
            composable(Routes.Settings.route) {
                RequireAuth(navController, authRepository) {
                    SettingsScreen(navController, profileViewModel, authViewModel)
                }
            }
            composable(Routes.Appearance.route) {
                RequireAuth(navController, authRepository) {
                    AppearanceScreen(navController)
                }
            }
            composable(Routes.Accessibility.route) {
                RequireAuth(navController, authRepository) {
                    AccessibilityScreen(navController)
                }
            }
        }
    }
}

@Composable
private fun RequireAuth(
    navController: NavController,
    authRepository: AuthRepository,
    content: @Composable () -> Unit
) {
    val hasSession = authRepository.getCurrentSession() != null
    if (hasSession) {
        content()
    } else {
        LaunchedEffect(Unit) {
            navController.navigate(Routes.Login.route) {
                popUpTo(Routes.Login.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}