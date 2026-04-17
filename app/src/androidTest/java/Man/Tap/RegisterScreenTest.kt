package man.tap.view.authView

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import man.tap.model.repository.IAuthRepository
import man.tap.viewmodel.AuthViewModel
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class RegisterScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Mock
    private lateinit var mockRepository: IAuthRepository

    private lateinit var navController: NavController
    private lateinit var viewModel: AuthViewModel

    @Test
    fun testRegisterScreenDisplaysTitle() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)
        
        composeTestRule.setContent {
            navController = rememberNavController()
            MaterialTheme {
                RegisterScreen(navController = navController, viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Registrarse").assertExists()
    }

    @Test
    fun testRegisterScreenDisplaysFormComponent() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)
        
        composeTestRule.setContent {
            navController = rememberNavController()
            MaterialTheme {
                RegisterScreen(navController = navController, viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Correo electrónico").assertExists()
        composeTestRule.onNodeWithText("Contraseña").assertExists()
    }
}
