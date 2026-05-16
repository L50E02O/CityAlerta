package ec.cityalerta.app.model.repository.interfaces

interface IAuthRepository {
    suspend fun signUp(email: String, password: String, ciudadId: String): Result<Unit>
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun logOut(): Result<Unit>
    suspend fun resetPasswordByEmail(email: String, newPassword: String): Result<Unit>
    suspend fun resendSignupConfirmation(email: String): Result<Unit>
    suspend fun updatePassword(newPassword: String): Result<Unit>
    suspend fun getUserId(): Result<String>
    suspend fun getUserEmail(): Result<String>
    suspend fun getCiudadId(): Result<String>
    suspend fun buscarCiudadPorNombre(nombre: String): Result<String?>
}
