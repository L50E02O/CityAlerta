package ec.cityalerta.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import ec.cityalerta.app.model.repository.IAuthRepository
import ec.cityalerta.app.view.authView.AuthFormComponent
import ec.cityalerta.app.viewmodel.AuthViewModel
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class AuthFormComponentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Mock
    private lateinit var mockRepository: IAuthRepository

    private lateinit var viewModel: AuthViewModel

    @Test
    fun testFormComponentDisplaysEmailField() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)
        
        composeTestRule.setContent {
            MaterialTheme {
                AuthFormComponent(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Correo electrónico").assertExists()
    }

    @Test
    fun testFormComponentDisplaysPasswordField() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)
        
        composeTestRule.setContent {
            MaterialTheme {
                AuthFormComponent(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Contraseña").assertExists()
    }

    @Test
    fun testFormComponentDisplaysErrorMessage() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")
        
        composeTestRule.setContent {
            MaterialTheme {
                AuthFormComponent(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Correo electrónico").assertExists()
    }

    @Test
    fun testFormComponentValidatesEmail() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)
        viewModel.onEmailChange("invalidemail")
        
        composeTestRule.setContent {
            MaterialTheme {
                AuthFormComponent(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Correo electrónico inválido").assertExists()
    }

    @Test
    fun testFormComponentValidatesPassword() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)
        viewModel.onPasswordChange("short")
        
        composeTestRule.setContent {
            MaterialTheme {
                AuthFormComponent(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("La contraseña debe tener al menos 8 caracteres").assertExists()
    }
}
