package man.tap.viewmodel

import man.tap.model.repository.IAuthRepository
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


data class AuthState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(private val repository: IAuthRepository) : ViewModel() {
    var uiState by mutableStateOf(AuthState())
        private set

    fun onEmailChange(email: String){
        uiState = uiState.copy(email = email)
    }

    fun onPasswordChange(password: String){
        uiState = uiState.copy(password = password)
    }

    fun onLoginClick(onSuccess: () -> Unit) {
        if (uiState.email.isNotEmpty() && uiState.password.isNotEmpty()){
            viewModelScope.launch {
                uiState = uiState.copy(isLoading = true, errorMessage = null)

                var result = repository.signIn(uiState.email, uiState.password)

                result.fold(
                    onSuccess = {
                        uiState = uiState.copy(isLoading = false)
                        onSuccess()
                    },
                    onFailure = { error ->
                        uiState = uiState.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error desconocido"
                        )

                    }
                )
            }
        }
    }

    fun onRegisterClick(onSuccess: () -> Unit) {
        if (uiState.email.isNotEmpty() && uiState.password.isNotEmpty()) {
            viewModelScope.launch {
               uiState = uiState.copy(isLoading = true, errorMessage = null)

                var result = repository.signUp(uiState.email, uiState.password)

                result.fold(
                    onSuccess = {
                        uiState = uiState.copy(isLoading = false)
                        onSuccess()
                    },
                    onFailure = { error ->
                        uiState = uiState.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error desconocido"
                        )

                    }
                )
            }
        }
    }
}