package ec.cityalerta.app.model.data.contracts.auth

import kotlinx.coroutines.flow.Flow

sealed class AuthState {
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    data class EmailVerificationPending(val email: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

interface AuthRepositoryContract {
    fun observeAuthState(): Flow<AuthState>
    suspend fun signUp(email: String, password: String, ciudadId: String): Result<Unit>
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun logOut(): Result<Unit>
    suspend fun checkEmailVerified(): Result<Boolean>
    suspend fun restoreSession(accessToken: String, refreshToken: String): Result<Unit>
    suspend fun refreshAndPersistSession(): Result<Unit>
    suspend fun verifyRecoveryEmail(email: String): Result<Boolean>
    suspend fun resetPasswordByEmail(email: String, newPassword: String): Result<Unit>
    suspend fun resendSignupConfirmation(email: String): Result<Unit>
    suspend fun updatePassword(newPassword: String): Result<Unit>
    suspend fun updateEmail(newEmail: String): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
    suspend fun getUserId(): Result<String>
    suspend fun getUserEmail(): Result<String>
    suspend fun getCiudadId(): Result<String>
    suspend fun getRoomId(): Result<String>
    suspend fun buscarCiudadPorNombre(nombre: String): Result<String?>
    fun getCurrentSession(): Any?
}
