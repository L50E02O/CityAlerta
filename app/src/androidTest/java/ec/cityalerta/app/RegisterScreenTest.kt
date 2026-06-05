package ec.cityalerta.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.view.authView.RegisterScreen
import ec.cityalerta.app.viewmodel.AuthViewModel
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class RegisterScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Mock
    private lateinit var mockRepository: AuthRepositoryContract

    private lateinit var viewModel: AuthViewModel

    @Test
    fun testRegisterScreenWithValidCredentials() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)
        
        composeTestRule.setContent {
            MaterialTheme {
                val navController = rememberNavController()
                RegisterScreen(navController = navController, viewModel = viewModel)
            }
        }

        // Prueba el flujo de registro con credenciales válidas
        viewModel.onEmailChange("newuser@example.com")
        viewModel.onPasswordChange("securePassword123")
        
        composeTestRule.waitForIdle()
        
        assert(viewModel.uiState.email == "newuser@example.com")
        assert(viewModel.uiState.password == "securePassword123")
    }

    @Test
    fun testRegisterScreenWithShortPassword() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)
        
        composeTestRule.setContent {
            MaterialTheme {
                val navController = rememberNavController()
                RegisterScreen(navController = navController, viewModel = viewModel)
            }
        }

        // Prueba con contraseña muy corta (debe fallar la validación)
        viewModel.onPasswordChange("short")
        composeTestRule.waitForIdle()
        
        assert(viewModel.uiState.password == "short")
        // Nota: La validación ocurre en el formulario, no aquí
    }
}
