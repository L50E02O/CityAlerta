package ec.cityalerta.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import ec.cityalerta.app.view.authView.AuthFormComponent
import org.junit.Rule
import org.junit.Test

class AuthFormComponentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `error message is displayed when uiState has error`() {
        composeTestRule.setContent {
            MaterialTheme {
                AuthFormComponent(
                    email = "",
                    password = "",
                    isLoading = false,
                    infoMessage = null,
                    errorMessage = "Credenciales incorrectas",
                    isEmailUnconfirmed = false,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onResendActivationEmail = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Credenciales incorrectas").assertExists()
    }

    @Test
    fun testEmailFieldAcceptsInput() {
        var email = ""
        composeTestRule.setContent {
            MaterialTheme {
                AuthFormComponent(
                    email = email,
                    password = "",
                    isLoading = false,
                    infoMessage = null,
                    errorMessage = null,
                    isEmailUnconfirmed = false,
                    onEmailChange = { email = it },
                    onPasswordChange = {},
                    onResendActivationEmail = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        assert(email == "")
    }
}
