package ec.cityalerta.app.view.authView

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ec.cityalerta.app.R
import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.repository.CiudadRepository
import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.viewmodel.AuthUiEvent
import ec.cityalerta.app.viewmodel.AuthViewModel
import ec.cityalerta.app.viewmodel.RegisterPhase

@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var ciudades by remember { mutableStateOf<List<Ciudad>>(emptyList()) }
    var ciudadError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val cityLoadErrorMessage = stringResource(R.string.auth_city_load_error)

    LaunchedEffect(Unit) {
        val repo = CiudadRepository()
        viewModel.onCiudadSelected("", "")

        repo.getAllByCountry("Ecuador").fold(
            onSuccess = { result -> ciudades = result },
            onFailure = { error ->
                ciudadError = error.message ?: cityLoadErrorMessage
            }
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                AuthUiEvent.NavigateToHome -> {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Register.route) { inclusive = true }
                    }
                }
                AuthUiEvent.NavigateToLogin -> {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Register.route) { inclusive = true }
                    }
                }
                is AuthUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val phase = uiState.registerPhase) {
            is RegisterPhase.EmailVerificationPending -> {
                EmailVerificationContent(
                    email = phase.email,
                    isLoading = uiState.isLoading,
                    onCheckVerified = viewModel::checkEmailVerified,
                    onResendEmail = viewModel::resendActivationEmail
                )
            }
            RegisterPhase.Form -> {
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
                        title = "Registro",
                        subtitle = "Ingrese sus credenciales para acceder al sistema.",
                        isLogin = false,
                        primaryButtonText = "Register",
                        secondaryActionText = "¿Ya tienes cuenta? Inicia sesión",
                        showCitySection = true,
                        ciudades = ciudades,
                        fixedCity = null,
                        cityLoadError = ciudadError,
                        onPrimaryAction = viewModel::onRegisterClick,
                        onSecondaryAction = { navController.popBackStack() },
                        onTabSwitch = {
                            if (!navController.popBackStack()) {
                                navController.navigate(Routes.Login.route) {
                                    popUpTo(Routes.Register.route) { inclusive = true }
                                }
                            }
                        }
                    )
                )
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}
