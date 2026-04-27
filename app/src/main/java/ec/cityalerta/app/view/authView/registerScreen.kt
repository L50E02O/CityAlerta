package ec.cityalerta.app.view.authView

import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.viewmodel.AuthViewModel
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel){
    AuthScreenScaffold(
        title = "Registrarse",
        primaryButtonText = "Registrarse",
        secondaryActionText = "¿Ya tienes cuenta? Inicia sesion",
        viewModel = viewModel,
        onPrimaryAction = {
            viewModel.onRegisterClick {
                navController.navigate(Routes.Home.route) {
                    popUpTo(Routes.Register.route) { inclusive = true }
                }
            }
        },
        onSecondaryAction = {
            navController.popBackStack()
        }
    )
}