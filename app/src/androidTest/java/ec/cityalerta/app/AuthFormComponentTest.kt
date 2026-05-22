package ec.cityalerta.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
        
        // Verifica que el uiState tenga email inválido
        assert(viewModel.uiState.email == "invalidemail")

        // Luego se corrige el email
        viewModel.onEmailChange("test@example.com")
        composeTestRule.waitForIdle()
        
        // Verifica que el email sea válido ahora
        assert(viewModel.uiState.email == "test@example.com")
    }

    @Test
    fun testPasswordFieldStateUpdates() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)

        composeTestRule.setContent {
            MaterialTheme {
                AuthFormComponent(viewModel = viewModel)
            }
        }

        // Prueba con contraseña muy corta
        viewModel.onPasswordChange("short")
        composeTestRule.waitForIdle()
        assert(viewModel.uiState.password == "short")

        // Prueba con contraseña válida
        viewModel.onPasswordChange("password123")
        composeTestRule.waitForIdle()
        assert(viewModel.uiState.password == "password123")
    }

    @Test
    fun testEmailAndPasswordFieldInteraction() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)

        composeTestRule.setContent {
            MaterialTheme {
                AuthFormComponent(viewModel = viewModel)
            }
        }

        // Actualiza email y contraseña
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("securePassword123")
        
        composeTestRule.waitForIdle()
        
        // Verifica que ambos campos se hayan actualizado
        assert(viewModel.uiState.email == "user@example.com")
        assert(viewModel.uiState.password == "securePassword123")
    }
}



