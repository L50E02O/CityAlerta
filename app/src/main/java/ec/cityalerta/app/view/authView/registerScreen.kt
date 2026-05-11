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

@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel){
    var ciudades by remember { mutableStateOf<List<Ciudad>>(emptyList()) }
    var ciudadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val repo = CiudadRepository()
        repo.getAllByCountry("Ecuador").fold(
            onSuccess = { result ->
                ciudades = result
                ciudadError = if (result.isEmpty()) "No se encontraron ciudades" else null
            },
            onFailure = { error ->
                ciudadError = error.message ?: "Error al cargar ciudades"
            }
        )
    }
    AuthScreenScaffold(
        title = "Registrarse",
        primaryButtonText = "Registrarse",
        secondaryActionText = "¿Ya tienes cuenta? Inicia sesion",
        viewModel = viewModel,
        showCitySection = true,
        ciudades = ciudades,
        cityLoadError = ciudadError,
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