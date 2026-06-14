package ec.cityalerta.app.view.authView

import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.viewmodel.AuthViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import ec.cityalerta.app.R

@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel){
    val ciudades by viewModel.ciudades.collectAsStateWithLifecycle()
    val ciudadError by viewModel.ciudadLoadError.collectAsStateWithLifecycle()
    val mantaCiudad = ciudades.find { it.nombre.equals("Uleam", ignoreCase = true) }

    LaunchedEffect(Unit) {
        viewModel.loadCiudades()
    }

    LaunchedEffect(ciudades) {
        if (mantaCiudad != null) {
            viewModel.onCiudadSelected(mantaCiudad.nombre, mantaCiudad.id)
        }
    }
    AuthScreenScaffold(
        viewModel = viewModel,
        config = AuthScreenConfig(
            title = "Registro",
            subtitle = "Ingrese sus credenciales para acceder al sistema.",
            isLogin = false,
            primaryButtonText = "Register",
            secondaryActionText = "¿Ya tienes cuenta? Inicia sesión",
            showCitySection = true,
            ciudades = ciudades,
            fixedCity = mantaCiudad, // Uleam como ciudad fija no editable
            cityLoadError = ciudadError ?: stringResource(R.string.auth_city_load_error),
            onPrimaryAction = {
                viewModel.onRegisterClick {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Register.route) { inclusive = true }
                    }
                }
            },
            onSecondaryAction = {
                navController.popBackStack()
            },
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