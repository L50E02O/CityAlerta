package ec.cityalerta.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ec.cityalerta.app.model.local.PasswordRecoveryPreferences
import ec.cityalerta.app.model.repository.interfaces.IAuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class PasswordRecoveryState(
    val email: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val recoveryEmailSent: Boolean = false,
    val isPasswordResetReady: Boolean = false,
    val isRecoveryBlocked: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
) {
    val isStepTwoUnlocked: Boolean
        get() = recoveryEmailSent || isPasswordResetReady
}

private const val RECOVERY_ERROR_MESSAGE = "Error desconocido"

class PasswordRecoveryViewModel(
    private val repository: IAuthRepository,
    private val recoveryPreferences: PasswordRecoveryPreferences
) : ViewModel() {

    var uiState by mutableStateOf(PasswordRecoveryState())
        private set

    init {
        refreshSessionState()
    }

    fun onEmailChange(email: String) {
        uiState = uiState.copy(
            email = email,
            isRecoveryBlocked = !recoveryPreferences.canRecover(email)
        )
    }

    fun onNewPasswordChange(password: String) {
        uiState = uiState.copy(newPassword = password)
    }

    fun onConfirmPasswordChange(password: String) {
        uiState = uiState.copy(confirmPassword = password)
    }

    fun refreshSessionState() {
        viewModelScope.launch {
            val hasSession = repository.getUserId().isSuccess
            val sessionEmail = repository.getUserEmail().getOrNull()
            uiState = uiState.copy(
                isPasswordResetReady = hasSession,
                email = if (sessionEmail != null && uiState.email.isBlank()) sessionEmail else uiState.email,
                isRecoveryBlocked = !recoveryPreferences.canRecover(
                    sessionEmail ?: uiState.email
                )
            )
        }
    }

    fun sendRecoveryEmail() {
        if (uiState.email.isBlank()) {
            uiState = uiState.copy(errorMessage = "Ingresa tu correo electronico")
            return
        }

        if (!recoveryPreferences.canRecover(uiState.email)) {
            uiState = uiState.copy(errorMessage = dailyLimitMessage(uiState.email))
            return
        }

        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true)

        viewModelScope.launch {
            uiState = uiState.copy(errorMessage = null, successMessage = null)

            try {
                repository.sendPasswordRecovery(uiState.email).fold(
                    onSuccess = {
                        recoveryPreferences.recordRecovery(uiState.email)
                        uiState = uiState.copy(
                            successMessage = "Te enviamos un enlace de recuperacion al correo",
                            recoveryEmailSent = true,
                            isRecoveryBlocked = true,
                            isPasswordResetReady = repository.getUserId().isSuccess
                        )
                    },
                    onFailure = { error ->
                        uiState = uiState.copy(errorMessage = error.message ?: RECOVERY_ERROR_MESSAGE)
                    }
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                uiState = uiState.copy(errorMessage = e.message ?: RECOVERY_ERROR_MESSAGE)
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun updatePassword(onSuccess: () -> Unit) {
        if (!uiState.isStepTwoUnlocked) {
            uiState = uiState.copy(errorMessage = "Primero envia el enlace de recuperacion a tu correo")
            return
        }

        if (!uiState.isPasswordResetReady) {
            uiState = uiState.copy(errorMessage = "Abre el enlace del correo antes de actualizar la contrasena")
            return
        }

        if (uiState.newPassword.isBlank() || uiState.confirmPassword.isBlank()) {
            uiState = uiState.copy(errorMessage = "Completa la nueva contrasena")
            return
        }

        if (uiState.newPassword.length < 8) {
            uiState = uiState.copy(errorMessage = "La contrasena debe tener al menos 8 caracteres")
            return
        }

        if (uiState.newPassword != uiState.confirmPassword) {
            uiState = uiState.copy(errorMessage = "Las contrasenas no coinciden")
            return
        }

        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true)

        viewModelScope.launch {
            uiState = uiState.copy(errorMessage = null)
            val emailForRecord = uiState.email.ifBlank {
                repository.getUserEmail().getOrNull().orEmpty()
            }

            try {
                repository.updatePassword(uiState.newPassword).fold(
                    onSuccess = {
                        if (emailForRecord.isNotBlank()) {
                            recoveryPreferences.recordRecovery(emailForRecord)
                        }
                        uiState = uiState.copy(
                            successMessage = "Contrasena actualizada correctamente",
                            isPasswordResetReady = false,
                            recoveryEmailSent = false,
                            newPassword = "",
                            confirmPassword = "",
                            isRecoveryBlocked = true
                        )
                        onSuccess()
                    },
                    onFailure = { error ->
                        uiState = uiState.copy(errorMessage = error.message ?: RECOVERY_ERROR_MESSAGE)
                    }
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                uiState = uiState.copy(errorMessage = e.message ?: RECOVERY_ERROR_MESSAGE)
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    private fun dailyLimitMessage(email: String): String {
        val hoursLeft = TimeUnit.MILLISECONDS.toHours(
            recoveryPreferences.millisUntilNextRecovery(email)
        ).coerceAtLeast(1L)
        return "Solo puedes restablecer tu contrasena una vez al dia. Intenta en $hoursLeft h."
    }
}
