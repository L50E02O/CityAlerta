package man.tap.view.authView

import man.tap.navigation.Routes
import man.tap.viewmodel.AuthViewModel
import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextDecoration


@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel){
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(viewModel.uiState.email).matches()
    val isPasswordValid = viewModel.uiState.password.isNotEmpty() && viewModel.uiState.password.length >= 8
    val isFormValid = isEmailValid && isPasswordValid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Iniciar sesión", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        AuthFormComponent(
            viewModel = viewModel,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.onLoginClick {
                    navController.navigate(Routes.Home.route){
                        popUpTo(Routes.Login.route){ inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled =  isFormValid && !viewModel.uiState.isLoading
        ) {
            Text(text = if (viewModel.uiState.isLoading) "Cargando..." else "Entrar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "¿No tienes cuenta? Regístrate",
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable {
                navController.navigate(Routes.Register.route)
            }
        )
    }
}