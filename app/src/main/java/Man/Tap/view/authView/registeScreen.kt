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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration

@Composable

fun RegisterScreen(navController: NavController, viewModel: AuthViewModel){
    //    Formulario de registarse
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(viewModel.email).matches()
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text( text = "Registrarse", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = { Text(text = "email") },
            singleLine = true,
            isError = viewModel.email.isNotEmpty() && !isEmailValid,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = {Text( text = "password")},
            singleLine = true,
            visualTransformation = if(passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Filled.VisibilityOff
                else Icons.Filled.Visibility

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.onRegisterClick {
                    navController.navigate(Routes.Home.route){
                        popUpTo(Routes.Register.route){ inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isLoading
        ){
            Text(text = if (viewModel.isLoading) "Cargando..." else "Registrarse")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "¿Ya tienes cuenta? Inicia sesión",
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable {
                navController.navigate(Routes.Login.route) {
                    popUpTo(Routes.Register.route) { inclusive = true }
                }
            }
        )
    }
}