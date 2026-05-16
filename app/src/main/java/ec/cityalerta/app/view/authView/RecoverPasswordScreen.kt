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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.view.style.ReportUiColors
import ec.cityalerta.app.viewmodel.PasswordRecoveryViewModel
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
    val canSendRecovery = isEmailValid && !state.isLoading && !state.isRecoveryBlocked
    val canUpdatePassword = state.isStepTwoUnlocked &&
        state.isPasswordResetReady &&
        state.newPassword.isNotBlank() &&
        state.newPassword.length >= 8 &&
        state.newPassword == state.confirmPassword &&
        !state.isLoading

    Surface(color = ReportUiColors.ScreenBackground) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "ATRAS",
                color = Color(0xFF1B2633),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { navController.popBackStack() }
            )

            Column {
                Text(
                    text = "Recuperar Contrasena",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF1B2633),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(width = 38.dp, height = 4.dp)
                        .background(ReportUiColors.AccentRed, RoundedCornerShape(999.dp))
                )
            }

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SectionLabel("PASO 01")
                    Text(
                        text = "Ingresa tu correo",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1B2633),
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !state.isLoading,
                        label = { Text("Correo electronico") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    Button(
                        onClick = viewModel::sendRecoveryEmail,
                        enabled = canSendRecovery,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (state.isLoading) "Enviando..." else "Enviar enlace")
                    }

                    if (state.isRecoveryBlocked && !state.recoveryEmailSent) {
                        Text(
                            text = "Ya solicitaste recuperacion hoy. Podras volver a intentarlo manana.",
                            color = ReportUiColors.HintText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (state.successMessage != null) {
                        Text(
                            text = state.successMessage!!,
                            color = Color(0xFF1B5E20),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (state.errorMessage != null && !state.isStepTwoUnlocked) {
                        Text(
                            text = state.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Text(
                        text = "Si no lo ves, revisa spam o vuelve a enviar el enlace.",
                        color = ReportUiColors.HintText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (state.isStepTwoUnlocked) 1f else 0.55f)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SectionLabel("PASO 02")
                    Text(
                        text = "Restablecer Contrasena",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1B2633),
                        fontWeight = FontWeight.SemiBold
                    )
                    when {
                        !state.isStepTwoUnlocked -> {
                            Text(
                                text = "Primero envia el enlace de recuperacion en el paso 01.",
                                color = ReportUiColors.HintText,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        state.isPasswordResetReady -> {
                            Text(
                                text = "Ya puedes actualizar la contrasena desde este dispositivo.",
                                color = ReportUiColors.HintText,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        else -> {
                            Text(
                                text = "Abre el enlace del correo para activar este paso.",
                                color = ReportUiColors.HintText,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    PasswordField(
                        value = state.newPassword,
                        label = "Escribe tu contrasena nueva",
                        onValueChange = viewModel::onNewPasswordChange,
                        enabled = state.isPasswordResetReady
                    )

                    PasswordField(
                        value = state.confirmPassword,
                        label = "Vuelve a escribir tu contrasena nueva",
                        onValueChange = viewModel::onConfirmPasswordChange,
                        enabled = state.isPasswordResetReady
                    )

                    Button(
                        onClick = {
                            viewModel.updatePassword {
                                navController.navigate(Routes.Login.route) {
                                    popUpTo(Routes.RecoverPassword.route) { inclusive = true }
                                }
                            }
                        },
                        enabled = canUpdatePassword,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (state.isLoading) "Actualizando..." else "Actualizar Contrasena")
                    }

                    if (state.errorMessage != null && state.isStepTwoUnlocked) {
                        Text(
                            text = state.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Text(
                text = "Regresar al login",
                color = Color(0xFF3B5B7A),
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
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = ReportUiColors.HintText,
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
        }
    )
}
