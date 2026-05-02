package ec.cityalerta.app.model.repository

interface IAuthRepository {
    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun logOut(): Result<Unit>
}
