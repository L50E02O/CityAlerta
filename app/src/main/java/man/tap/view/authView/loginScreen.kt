package man.tap.view.authView

import man.tap.navigation.Routes
import man.tap.viewmodel.AuthViewModel
import androidx.compose.runtime.Composable
import androidx.navigation.NavController


@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel){
    AuthScreenScaffold(
        title = "Iniciar sesión",
        primaryButtonText = "Entrar",
        secondaryActionText = "¿No tienes cuenta? Regístrate",
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