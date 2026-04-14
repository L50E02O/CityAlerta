package man.tap.model.repository

import man.tap.model.remote.SupabaseProvider
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email

class AuthRepository: IAuthRepository {
    override suspend fun signUp(email: String, password: String): Result<Unit> {
        return try{
            SupabaseProvider.client.auth.signUpWith(Email){
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            SupabaseProvider.client.auth.signInWith(Email){
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun logOut(): Result<Unit> {
        return try {
            SupabaseProvider.client.auth.signOut()
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }
}