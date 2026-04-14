package man.tap.view.authView

import man.tap.navigation.Routes
import man.tap.viewmodel.AuthViewModel
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel){
    AuthScreenScaffold(
        title = "Registrarse",
        primaryButtonText = "Registrarse",
        secondaryActionText = "¿Ya tienes cuenta? Inicia sesión",
        viewModel = viewModel,
        onPrimaryAction = {
            viewModel.onRegisterClick {
                navController.navigate(Routes.Home.route) {
                    popUpTo(Routes.Register.route) { inclusive = true }
                }
            }
        },
        onSecondaryAction = {
            navController.navigate(Routes.Login.route) {
                popUpTo(Routes.Register.route) { inclusive = true }
            }
        }
    )
}
