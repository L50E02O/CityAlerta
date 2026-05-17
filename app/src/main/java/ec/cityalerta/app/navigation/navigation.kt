package ec.cityalerta.app.navigation

import ec.cityalerta.app.model.repository.AuthRepository
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
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Map
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import ec.cityalerta.app.view.explore.ExploreScreen
import androidx.navigation.NavController
import ec.cityalerta.app.view.camera.PhotoScreen
import ec.cityalerta.app.view.reporte.ReporteScreen
import ec.cityalerta.app.viewmodel.ReporteViewModel
import ec.cityalerta.app.view.profile.MyReportsScreen
import ec.cityalerta.app.view.profile.ProfileDashboardScreen

@Composable
fun AppNavigation(
    startDestination: String = Routes.Login.route,
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

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
        ) {
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
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        Routes.Home.route,
        Routes.Explore.route,
        Routes.Post.route,
        Routes.Map.route
    )

    if (currentRoute in bottomBarRoutes) {
        Surface(
            color = Color(0xFF1B2633),
            modifier = Modifier.fillMaxWidth()
        ) {
            NavigationBar(
                modifier = Modifier.height(84.dp),
                containerColor = Color.Transparent,
                contentColor = Color.White,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = currentRoute == Routes.Explore.route || currentRoute == Routes.Home.route,
                    onClick = {
                        navController.navigate(Routes.Explore.route) {
                            popUpTo(Routes.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.Explore,
                            contentDescription = "EXPLORE",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("EXPLORE", fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == Routes.Post.route,
                    onClick = {
                        navController.navigate(Routes.Post.route) {
                            popUpTo(Routes.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF3B5B7A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "REPORT",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    label = { Text("REPORT", fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == Routes.Map.route,
                    onClick = {
                        navController.navigate("map/manta") {
                            popUpTo(Routes.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = "MAP",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("MAP", fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}