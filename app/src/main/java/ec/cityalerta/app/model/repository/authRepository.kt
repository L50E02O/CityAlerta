package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.remote.SupabaseProvider
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlin.coroutines.cancellation.CancellationException

class AuthRepository: IAuthRepository {
    override suspend fun signUp(email: String, password: String): Result<Unit> {
        return try{
            SupabaseProvider.client.auth.signUpWith(Email){
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        }catch (e: CancellationException){
            throw e
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
        }catch (e: CancellationException){
            throw e
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun logOut(): Result<Unit> {
        return try {
            SupabaseProvider.client.auth.signOut()
            Result.success(Unit)
        }catch (e: CancellationException){
            throw e
        }catch (e: Exception){
            Result.failure(e)
        }
    }
}