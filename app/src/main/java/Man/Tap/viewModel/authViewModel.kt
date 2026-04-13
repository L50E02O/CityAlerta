package man.tap.viewmodel

import man.tap.model.repository.IAuthRepository
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class AuthViewModel(private val repository: IAuthRepository) : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun onLoginClick(onSuccess: () -> Unit) {
        if (email.isNotEmpty() && password.isNotEmpty()){
            viewModelScope.launch {
                isLoading = true
                errorMessage = null

                var result = repository.signIn(email, password)

                result.fold(
                    onSuccess = {
                        isLoading = false
                        onSuccess()
                    },
                    onFailure = { error ->
                        isLoading = false
                        errorMessage = error.message ?: "Error desconocido"
                    }
                )
            }
        }
    }

    fun onRegisterClick(onSuccess: () -> Unit) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            viewModelScope.launch {
                isLoading = true
                errorMessage = null

                var result = repository.signUp(email, password)

                result.fold(
                    onSuccess = {
                        isLoading = false
                        onSuccess()
                    },
                    onFailure = { error ->
                        isLoading = false
                        errorMessage = error.message ?: "Error desconocido"
                    }
                )
            }
        }
    }
}