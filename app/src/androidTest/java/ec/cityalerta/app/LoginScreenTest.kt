package ec.cityalerta.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.view.authView.LoginScreen
import ec.cityalerta.app.viewmodel.AuthViewModel
import androidx.navigation.compose.rememberNavController
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class LoginScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Mock
    private lateinit var mockRepository: AuthRepositoryContract

    private lateinit var viewModel: AuthViewModel

    @Test
    fun testLoginScreenWithValidCredentials() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)
        
        composeTestRule.setContent {
            MaterialTheme {
                val navController = rememberNavController()
                LoginScreen(navController = navController, viewModel = viewModel)
            }
        }

        // Prueba el flujo de ingreso de credenciales válidas
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("validPassword123")

        composeTestRule.waitForIdle()

        assert(viewModel.uiState.email == "test@example.com")
        assert(viewModel.uiState.password == "validPassword123")
    }

    @Test
    fun testLoginScreenWithInvalidEmail() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)
        
        composeTestRule.setContent {
            MaterialTheme {
                val navController = rememberNavController()
                LoginScreen(navController = navController, viewModel = viewModel)
            }
        }

        // Prueba con email inválido
        viewModel.onEmailChange("notanemail")
        composeTestRule.waitForIdle()

        assert(viewModel.uiState.email == "notanemail")
    }
}
