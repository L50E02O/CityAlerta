package ec.cityalerta.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
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
    private lateinit var mockRepository: AuthRepositoryContract

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

        composeTestRule.onNodeWithText("CORREO ELECTRÓNICO").assertExists()
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

        composeTestRule.onNodeWithText("CONTRASEÑA").assertExists()
    }

    @Test
    fun testEmailFieldShowsErrorMessageForInvalidEmail() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)
        viewModel.onEmailChange("invalidemail")

        composeTestRule.setContent {
            MaterialTheme {
                AuthFormComponent(viewModel = viewModel)
            }
        }

        // Verifica que el mensaje de error del email inválido aparezca
        composeTestRule.onNodeWithText("Correo electrónico inválido").assertExists()
    }

    @Test
    fun testEmailFieldDoesNotShowErrorForValidEmail() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)
        viewModel.onEmailChange("test@example.com")

        composeTestRule.setContent {
            MaterialTheme {
                AuthFormComponent(viewModel = viewModel)
            }
        }

        // Verifica que el campo de email válido no muestre error
        composeTestRule.onNodeWithText("Correo electrónico inválido").assertDoesNotExist()
    }

    @Test
    fun testPasswordFieldValidation() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)
        viewModel.onPasswordChange("short")

        composeTestRule.setContent {
            MaterialTheme {
                AuthFormComponent(viewModel = viewModel)
            }
        }

        // Verifica que aparezca el mensaje de contraseña corta
        composeTestRule.onNodeWithText("La contraseña debe tener al menos 8 caracteres").assertExists()
    }

    @Test
    fun testPasswordFieldDoesNotShowErrorForValidPassword() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)
        viewModel.onPasswordChange("password123")

        composeTestRule.setContent {
            MaterialTheme {
                AuthFormComponent(viewModel = viewModel)
            }
        }

        // Verifica que una contraseña válida no muestre error
        composeTestRule.onNodeWithText("La contraseña debe tener al menos 8 caracteres").assertDoesNotExist()
    }

    @Test
    fun testEmailFieldColorUpdatesOnErrorState() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)

        composeTestRule.setContent {
            MaterialTheme {
                AuthFormComponent(viewModel = viewModel)
            }
        }

        // Primero entra un email inválido
        viewModel.onEmailChange("invalidemail")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Correo electrónico inválido").assertExists()

        // Luego se corrige el email
        viewModel.onEmailChange("test@example.com")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Correo electrónico inválido").assertDoesNotExist()
    }
}



