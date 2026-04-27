package ec.cityalerta.app.view.authView

import android.util.Patterns
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import ec.cityalerta.app.viewmodel.AuthViewModel

@Composable
fun AuthScreenScaffold(
    title: String,
    primaryButtonText: String,
    secondaryActionText: String,
    viewModel: AuthViewModel,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit
) {
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
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        AuthFormComponent(
            viewModel = viewModel,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onPrimaryAction,
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid && !viewModel.uiState.isLoading
        ) {
            Text(text = if (viewModel.uiState.isLoading) "Cargando..." else primaryButtonText)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = secondaryActionText,
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable(onClick = onSecondaryAction)
        )
    }
}
