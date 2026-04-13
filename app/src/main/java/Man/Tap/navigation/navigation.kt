package man.tap.navigation

import man.tap.model.repository.AuthRepository
import man.tap.view.authView.LoginScreen
import man.tap.view.authView.RegisterScreen
import androidx.compose.runtime.Composable
import man.tap.navigation.Routes
import man.tap.view.home.HomeScreen
import man.tap.viewmodel.AuthViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import man.tap.viewmodel.AuthViewModelFactory


// -----------------------------------//
@Composable
fun AppNavigation(){

    val navController = rememberNavController()


    val authRepository = AuthRepository()
    val factory = AuthViewModelFactory(authRepository)

    val viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = factory
    )

    NavHost(

        navController = navController,
        startDestination = Routes.Login.route
    ){
        composable(Routes.Login.route){
            LoginScreen(navController, viewModel)
        }
        composable(Routes.Register.route){
            RegisterScreen(navController, viewModel)
        }
        composable(Routes.Home.route){
            HomeScreen()
        }
    }

}