package ec.cityalerta.app.view.authView

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.viewmodel.AuthUiEvent
import ec.cityalerta.app.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel,
    authInfoMessage: String? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(authInfoMessage) {
        if (!authInfoMessage.isNullOrBlank()) {
            viewModel.setAuthInfoMessage(authInfoMessage)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                AuthUiEvent.NavigateToHome -> {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
                AuthUiEvent.NavigateToLogin -> Unit
                is AuthUiEvent.ShowSnackbar -> Unit
            }
        }
    }

    AuthScreenScaffold(
        email = uiState.email,
        password = uiState.password,
        ciudadNombre = uiState.ciudadNombre,
        ciudadId = uiState.ciudadId,
        isLoading = uiState.isLoading,
        infoMessage = uiState.infoMessage,
        errorMessage = uiState.errorMessage,
        isEmailUnconfirmed = uiState.isEmailUnconfirmed,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onCiudadChange = viewModel::onCiudadChange,
        onCiudadSelected = viewModel::onCiudadSelected,
        onResendActivationEmail = viewModel::resendActivationEmail,
        config = AuthScreenConfig(
            title = "Bienvenido de vuelta!",
            subtitle = "Ingrese sus credenciales para acceder al sistema.",
            isLogin = true,
            primaryButtonText = "Login",
            secondaryActionText = "¿Olvidaste tu contraseña?",
            onPrimaryAction = viewModel::onLoginClick,
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
