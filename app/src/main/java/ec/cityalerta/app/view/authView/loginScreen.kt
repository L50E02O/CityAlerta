package ec.cityalerta.app.view.authView

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.viewmodel.AuthViewModel
import ec.cityalerta.app.R


@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel,
    authInfoMessage: String? = null
) {
    LaunchedEffect(authInfoMessage) {
        if (!authInfoMessage.isNullOrBlank()) {
            viewModel.setAuthInfoMessage(authInfoMessage)
        }
    }

    AuthScreenScaffold(
        viewModel = viewModel,
        config = AuthScreenConfig(
            title = "Bienvenido de vuelta!",
            subtitle = "Ingrese sus credenciales para acceder al sistema.",
            isLogin = true,
            primaryButtonText = "Login",
            secondaryActionText = "¿Olvidaste tu contraseña?",
            onPrimaryAction = {
                viewModel.onLoginClick {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            },
            onSecondaryAction = {
                navController.navigate(Routes.RecoverPassword.route)
            },
            onTabSwitch = {
                navController.navigate(Routes.Register.route) {
                    popUpTo(Routes.Login.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    )
}