package ec.cityalerta.app.view.authView

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ec.cityalerta.app.model.repository.IAuthRepository
import ec.cityalerta.app.viewmodel.AuthViewModel
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class LoginScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Mock
    private lateinit var mockRepository: IAuthRepository

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

        composeTestRule.onNodeWithText("Iniciar sesión").assertExists()
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

        composeTestRule.onNodeWithText("Correo electrónico").assertExists()
        composeTestRule.onNodeWithText("Contraseña").assertExists()
    }
}
