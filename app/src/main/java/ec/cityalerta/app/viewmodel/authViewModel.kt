package ec.cityalerta.app.viewmodel

import ec.cityalerta.app.model.repository.interfaces.IAuthRepository
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch


data class AuthState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

private const val UNKNOWN_ERROR_MESSAGE = "Error desconocido"

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
        if (uiState.email.isEmpty() || uiState.password.isEmpty()){
            uiState = uiState.copy(errorMessage = "El correo y la contrasena no pueden estar vacios")
            return
        }

        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true)

        viewModelScope.launch {
            uiState = uiState.copy(errorMessage = null)

            try {
                val result = repository.signIn(uiState.email, uiState.password)

                result.fold(
                    onSuccess = {
                        onSuccess()
                    },
                    onFailure = { error ->
                        uiState = uiState.copy(
                            errorMessage = error.message ?: UNKNOWN_ERROR_MESSAGE
                        )
                    }
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                uiState = uiState.copy(
                    errorMessage = e.message ?: UNKNOWN_ERROR_MESSAGE
                )
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun onRegisterClick(onSuccess: () -> Unit) {
        if (uiState.email.isEmpty() || uiState.password.isEmpty()) {
            uiState = uiState.copy(errorMessage = "El correo y la contrasena no pueden estar vacios")
            return
        }

        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true)

        viewModelScope.launch {
           uiState = uiState.copy(errorMessage = null)

            try {
                val result = repository.signUp(uiState.email, uiState.password)

                result.fold(
                    onSuccess = {
                        onSuccess()
                    },
                    onFailure = { error ->
                        uiState = uiState.copy(
                            errorMessage = error.message ?: UNKNOWN_ERROR_MESSAGE
                        )
                    }
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                uiState = uiState.copy(
                    errorMessage = e.message ?: UNKNOWN_ERROR_MESSAGE
                )
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }
}