package ec.cityalerta.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.view.authView.LoginScreen
import ec.cityalerta.app.viewmodel.AuthViewModel
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class LoginScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Mock
    private lateinit var mockRepository: AuthRepositoryContract

    private lateinit var navController: NavController
    private lateinit var viewModel: AuthViewModel

    @Test
    fun testLoginScreenDisplaysTitle() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)
        
        composeTestRule.setContent {
            navController = rememberNavController()
            MaterialTheme {
                LoginScreen(navController = navController, viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Bienvenido de vuelta!").assertExists()
    }

    @Test
    fun testLoginScreenDisplaysFormComponent() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)
        
        composeTestRule.setContent {
            navController = rememberNavController()
            MaterialTheme {
                LoginScreen(navController = navController, viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("CORREO ELECTRÓNICO").assertExists()
        composeTestRule.onNodeWithText("CONTRASEÑA").assertExists()
    }
}
