package ec.cityalerta.app.navigation

import ec.cityalerta.app.model.repository.AuthRepository
import ec.cityalerta.app.view.authView.LoginScreen
import ec.cityalerta.app.view.authView.RegisterScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.view.home.HomeScreen
import ec.cityalerta.app.view.map.MapScreen
import ec.cityalerta.app.viewmodel.AuthViewModel
import ec.cityalerta.app.viewmodel.MapViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ec.cityalerta.app.viewmodel.AppViewModelFactory


@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val authRepository = remember { AuthRepository() }
    val factory = remember { AppViewModelFactory(authRepository) }

    val authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = factory
    )

    val mapViewModel: MapViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = factory
    )

    NavHost(
        navController = navController,
        startDestination = Routes.Login.route
    ){
        composable(Routes.Login.route){
            LoginScreen(navController, authViewModel)
        }
        composable(Routes.Register.route){
            RegisterScreen(navController, authViewModel)
        }
        composable(Routes.Home.route){
            HomeScreen(navController)
        }
        composable(Routes.Map.route){ backStackEntry ->
            val ciudadId = backStackEntry.arguments?.getString("ciudadId") ?: "manta"
            MapScreen(navController, ciudadId, mapViewModel)
        }
    }

}