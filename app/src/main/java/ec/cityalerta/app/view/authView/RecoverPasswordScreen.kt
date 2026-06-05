package ec.cityalerta.app.view.authView

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ec.cityalerta.app.theme.SuccessGreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.view.style.ReportUiColors
import ec.cityalerta.app.viewmodel.PasswordRecoveryViewModel
import ec.cityalerta.app.viewmodel.PasswordRecoveryState
import androidx.navigation.NavController

@Composable
fun RecoverPasswordScreen(
    navController: NavController,
    viewModel: PasswordRecoveryViewModel
) {
    val state = viewModel.uiState
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshSessionState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(state.email).matches()
    val canVerifyEmail = isEmailValid && !state.isLoading
    val canUpdatePassword = isEmailValid &&
        state.isEmailVerified &&
        state.newPassword.isNotBlank() &&
        state.newPassword.length >= 8 &&
        state.newPassword == state.confirmPassword &&
        !state.isLoading

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RecoverPasswordHeader(onBack = { navController.popBackStack() })

            EmailVerificationCard(
                state = state,
                canVerifyEmail = canVerifyEmail,
                onEmailChange = viewModel::onEmailChange,
                onVerifyEmail = viewModel::verifyEmail
            )

            PasswordResetCard(
                state = state,
                canUpdatePassword = canUpdatePassword,
                onNewPasswordChange = viewModel::onNewPasswordChange,
                onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                onResetPassword = {
                    viewModel.resetPassword {
                        navController.navigate(Routes.Login.route) {
                            popUpTo(Routes.RecoverPassword.route) { inclusive = true }
                        }
                    }
                }
            )

            Text(
                text = "Regresar al login",
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable {
                        navController.navigate(Routes.Login.route) {
                            popUpTo(Routes.RecoverPassword.route) { inclusive = true }
                        }
                    }
            )
        }
    }
}

@Composable
private fun RecoverPasswordHeader(onBack: () -> Unit) {
    Text(
        text = "ATRAS",
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clickable(onClick = onBack)
    )

    Column {
        Text(
            text = "Restablecer Contrasena",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .size(width = 38.dp, height = 4.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(999.dp))
        )
    }
}

@Composable
private fun EmailVerificationCard(
    state: PasswordRecoveryState,
    canVerifyEmail: Boolean,
    onEmailChange: (String) -> Unit,
    onVerifyEmail: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionLabel("PASO 01")
            Text(
                text = "Verifica tu correo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isLoading,
                label = { Text("Correo electronico") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = "La app validara que el correo exista y luego actualizara la contrasena con la edge function.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )

            Button(
                onClick = onVerifyEmail,
                enabled = canVerifyEmail,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(if (state.isLoading) "Verificando..." else "Verificar correo")
            }

            if (state.isEmailVerified) {
                Text(
                    text = "Correo verificado. Paso 02 habilitado.",
                    color = SuccessGreen,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun PasswordResetCard(
    state: PasswordRecoveryState,
    canUpdatePassword: Boolean,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onResetPassword: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (state.isEmailVerified) 1f else 0.55f)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionLabel("PASO 02")
            Text(
                text = "Restablecer Contrasena",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Escribe una nueva contrasena y confirmala. Si el correo existe, la edge function la guardara en Supabase Auth.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )

            PasswordField(
                value = state.newPassword,
                label = "Escribe tu contrasena nueva",
                onValueChange = onNewPasswordChange,
                enabled = state.isEmailVerified && !state.isLoading
            )

            PasswordField(
                value = state.confirmPassword,
                label = "Vuelve a escribir tu contrasena nueva",
                onValueChange = onConfirmPasswordChange,
                enabled = state.isEmailVerified && !state.isLoading
            )

            Button(
                onClick = onResetPassword,
                enabled = canUpdatePassword,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(if (state.isLoading) "Procesando..." else "Actualizar Contrasena")
            }

            RecoverPasswordStatusMessages(
                successMessage = state.successMessage,
                errorMessage = state.errorMessage
            )
        }
    }
}


@Composable
private fun RecoverPasswordStatusMessages(
    successMessage: String?,
    errorMessage: String?
) {
    successMessage?.let { message ->
        Text(
            text = message,
            color = SuccessGreen,
            style = MaterialTheme.typography.bodySmall
        )
    }

    errorMessage?.let { message ->
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun PasswordField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true
) {
    var isVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = enabled,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(
                onClick = { isVisible = !isVisible },
                enabled = enabled
            ) {
                Icon(
                    imageVector = if (isVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (isVisible) "Ocultar contrasena" else "Mostrar contrasena"
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}
