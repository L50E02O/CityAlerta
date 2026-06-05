package ec.cityalerta.app.view.authView

import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.viewmodel.AuthViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.repository.CiudadRepository
import androidx.compose.ui.res.stringResource
import ec.cityalerta.app.R

@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel){
    var ciudades by remember { mutableStateOf<List<Ciudad>>(emptyList()) }
    var ciudadError by remember { mutableStateOf<String?>(null) }
    val cityLoadErrorMessage = stringResource(R.string.auth_city_load_error)

    LaunchedEffect(Unit) {
        val repo = CiudadRepository()
        // Limpiamos selección previa si existe para forzar elección manual
        viewModel.onCiudadSelected("", "")
        
        repo.getAllByCountry("Ecuador").fold(
            onSuccess = { result ->
                ciudades = result
            },
            onFailure = { error ->
                ciudadError = error.message ?: cityLoadErrorMessage
            }
        )
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
            fixedCity = null, // Cambiado de mantaCiudad a null para habilitar el dropdown
            cityLoadError = ciudadError,
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