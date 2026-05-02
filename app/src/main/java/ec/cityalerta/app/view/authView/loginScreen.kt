package ec.cityalerta.app.view.authView

import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.viewmodel.AuthViewModel
import androidx.compose.runtime.Composable
import androidx.navigation.NavController


@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel){
    AuthScreenScaffold(
        title = "Iniciar sesion",
        primaryButtonText = "Entrar",
        secondaryActionText = "¿No tienes cuenta? Registrate",
        viewModel = viewModel,
        onPrimaryAction = {
            viewModel.onLoginClick {
                navController.navigate(Routes.Home.route) {
                    popUpTo(Routes.Login.route) { inclusive = true }
                }
            }
        },
        onSecondaryAction = {
            navController.navigate(Routes.Register.route)
        }
    )
}