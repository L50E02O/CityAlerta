package ec.cityalerta.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import ec.cityalerta.app.testdoubles.FakeAuthRepository
import ec.cityalerta.app.view.authView.RegisterScreen
import ec.cityalerta.app.viewmodel.AuthViewModel
import org.junit.Rule
import org.junit.Test

class RegisterScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testRegisterScreenWithValidCredentials() {
        val viewModel = AuthViewModel(FakeAuthRepository())

        composeTestRule.setContent {
            MaterialTheme {
                val navController = rememberNavController()
                RegisterScreen(navController = navController, viewModel = viewModel)
            }
        }

        viewModel.onEmailChange("newuser@example.com")
        viewModel.onPasswordChange("securePassword123")
        composeTestRule.waitForIdle()

        assert(viewModel.uiState.value.email == "newuser@example.com")
        assert(viewModel.uiState.value.password == "securePassword123")
    }
}
