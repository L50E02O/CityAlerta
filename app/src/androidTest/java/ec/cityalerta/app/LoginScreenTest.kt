package ec.cityalerta.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import ec.cityalerta.app.testdoubles.FakeAuthRepository
import ec.cityalerta.app.view.authView.LoginScreen
import ec.cityalerta.app.viewmodel.AuthViewModel
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoginScreenWithValidCredentials() {
        val viewModel = AuthViewModel(FakeAuthRepository())

        composeTestRule.setContent {
            MaterialTheme {
                val navController = rememberNavController()
                LoginScreen(navController = navController, viewModel = viewModel)
            }
        }

        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("validPassword123")
        composeTestRule.waitForIdle()

        assert(viewModel.uiState.value.email == "test@example.com")
        assert(viewModel.uiState.value.password == "validPassword123")
    }
}
