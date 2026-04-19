package man.tap.navigation

import android.content.Context
import man.tap.model.repository.AuthRepository
import man.tap.view.authView.LoginScreen
import man.tap.view.authView.RegisterScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import man.tap.navigation.Routes
import man.tap.view.home.HomeScreen
import man.tap.view.map.MapScreen
import man.tap.viewmodel.AuthViewModel
import man.tap.viewmodel.MapViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import man.tap.viewmodel.AppViewModelFactory


@Composable
fun AppNavigation(context: Context) {

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