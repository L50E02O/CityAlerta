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
import ec.cityalerta.app.viewmodel.AppViewModelFactory
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import ec.cityalerta.app.view.components.AppBottomBar
import ec.cityalerta.app.view.explore.ExploreScreen
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

@Composable
fun AppNavigation(
    startDestination: String = Routes.Splash.route,
    authInfoMessage: String? = null
) {

    val navController = rememberNavController()
    val authRepository = remember { AuthRepository() }
    val context = LocalContext.current
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

    Scaffold(
        bottomBar = { AppBottomBar(navController) }
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
                        val nextRoute = if (hasSession) Routes.Home.route else Routes.Login.route
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
                ExploreScreen(navController, exploreViewModel, profileViewModel)
            }
            composable(Routes.Explore.route){
                ExploreScreen(navController, exploreViewModel, profileViewModel)
            }

            composable(Routes.Search.route) {
                SearchScreen(navController, searchReportViewModel, profileViewModel)
            }


            composable(Routes.Post.route) {
                PhotoScreen(
                    navController = navController,
                    viewModel = reporteViewModel,
                    profileViewModel = profileViewModel,
                    onPhotoCaptured = {
                        navController.navigate(Routes.ReporteForm.route)
                    }
                )
            }

            composable(Routes.ReporteForm.route) {
                ReporteScreen(
                    navController = navController,
                    viewModel = reporteViewModel,
                    profileViewModel = profileViewModel,
                    onReportSent = {
                        // Limpiar el back stack hasta Post y navegar a Explore
                        navController.navigate(Routes.Explore.route) {
                            popUpTo(Routes.Post.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.Map.route) { backStackEntry ->
                val ciudadId = backStackEntry.arguments?.getString("ciudadId") ?: "manta"
                MapScreen(navController, ciudadId, mapViewModel, profileViewModel)
            }
            composable(Routes.Profile.route) {
                ProfileDashboardScreen(navController, profileViewModel)
            }
            composable(Routes.MyReports.route) {
                MyReportsScreen(navController, profileViewModel)
            }
            composable(Routes.Settings.route) {
                SettingsScreen(navController, profileViewModel)
            }
            composable(Routes.Appearance.route) {
                AppearanceScreen(navController)
            }
            composable(Routes.Accessibility.route) {
                AccessibilityScreen(navController)
            }
        }
    }
}