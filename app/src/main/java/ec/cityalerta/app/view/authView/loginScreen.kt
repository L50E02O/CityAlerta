package ec.cityalerta.app.view.authView

import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.viewmodel.AuthViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import ec.cityalerta.app.R


@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel,
    authInfoMessage: String? = null
) {
    LaunchedEffect(authInfoMessage) {
        if (!authInfoMessage.isNullOrBlank()) {
            viewModel.setAuthInfoMessage(authInfoMessage)
        }
    }

    AuthScreenScaffold(
        viewModel = viewModel,
        config = AuthScreenConfig(
            title = stringResource(R.string.auth_login_title),
            primaryButtonText = stringResource(R.string.auth_login_button),
            secondaryActionText = stringResource(R.string.auth_no_account),
            onPrimaryAction = {
                viewModel.onLoginClick {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            },
            onSecondaryAction = {
                navController.navigate(Routes.Register.route)
            },
            bottomContent = {
                Text(
                    text = stringResource(R.string.auth_forgot_password),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        navController.navigate(Routes.RecoverPassword.route)
                    }
                )
            }
        )
    )
}