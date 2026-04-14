package man.tap.view.authView

import android.util.Patterns
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import man.tap.viewmodel.AuthViewModel

@Composable
fun AuthFormComponent(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val email = viewModel.uiState.email
    val password = viewModel.uiState.password
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isPasswordValid = password.isNotEmpty() && password.length >= 8

    Column(modifier = modifier) {
        EmailField(
            email = email,
            isEmailValid = isEmailValid,
            onEmailChange = viewModel::onEmailChange
        )

        if (email.isNotEmpty() && !isEmailValid) {
            Text(
                text = "Correo electronico invalido",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        PasswordField(
            password = password,
            passwordVisible = passwordVisible,
            onPasswordChange = viewModel::onPasswordChange,
            onToggleVisibility = { passwordVisible = !passwordVisible }
        )

        if (password.isNotEmpty() && !isPasswordValid) {
            Text(
                text = "La contrasena debe tener al menos 8 caracteres",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (viewModel.uiState.errorMessage != null) {
            Text(
                text = viewModel.uiState.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun EmailField(
    email: String,
    isEmailValid: Boolean,
    onEmailChange: (String) -> Unit
) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text(text = "Correo electronico") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            autoCorrectEnabled = false
        ),
        isError = email.isNotEmpty() && !isEmailValid,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PasswordField(
    password: String,
    passwordVisible: Boolean,
    onPasswordChange: (String) -> Unit,
    onToggleVisibility: () -> Unit
) {
    val icon = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
    val description = if (passwordVisible) "Ocultar contrasena" else "Mostrar contrasena"

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(text = "Contrasena") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            autoCorrectEnabled = false
        ),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = icon,
                    contentDescription = description
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}