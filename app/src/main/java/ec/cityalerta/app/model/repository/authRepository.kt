package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.repository.interfaces.IAuthRepository
import io.github.jan.supabase.gotrue.auth
import ec.cityalerta.app.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.coroutines.cancellation.CancellationException

class AuthRepository: IAuthRepository {
    override suspend fun signUp(email: String, password: String, ciudadId: String): Result<Unit> {
        return try{
            SupabaseProvider.client.auth.signUpWith(Email){
                this.email = email
                this.password = password
                this.data = buildJsonObject {
                    put("ciudad_id", ciudadId)
                    put("nombre_completo", "Usuario")
                }
            }
            if (SupabaseProvider.client.auth.currentSessionOrNull() != null) {
                SupabaseProvider.client.auth.signOut()
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

    override suspend fun sendPasswordRecovery(email: String): Result<Unit> {
        return try {
            SupabaseProvider.client.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            val session = SupabaseProvider.client.auth.currentSessionOrNull()
                ?: return Result.failure(Exception("Abre el enlace del correo antes de actualizar la contrasena"))

            val accessToken = session.accessToken
            val httpClient = HttpClient(Android)
            try {
                val resp: HttpResponse = httpClient.patch("${BuildConfig.SUPABASE_URL.trimEnd('/')}/auth/v1/user") {
                    header("Authorization", "Bearer $accessToken")
                    contentType(ContentType.Application.Json)
                    setBody("{\"password\":\"$newPassword\"}")
                }

                if (resp.status.value !in 200..299) {
                    val body = resp.bodyAsText()
                    return Result.failure(Exception("No se pudo actualizar la contrasena: ${resp.status.value} $body"))
                }
            } finally {
                httpClient.close()
            }

            SupabaseProvider.client.auth.signOut()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserEmail(): Result<String> {
        return try {
            val email = SupabaseProvider.client.auth.currentSessionOrNull()?.user?.email
            if (email.isNullOrBlank()) {
                Result.failure(Exception("No hay correo asociado a la sesion"))
            } else {
                Result.success(email)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserId(): Result<String>{
        return try{
            val session = SupabaseProvider.client.auth.currentSessionOrNull()
            val user = session?.user

            if (user == null){
                Result.failure(Exception("No hay usuario logueado"))
            }else{
                Result.success(user.id)
            }
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun getCiudadId(): Result<String> {
        return try {
            val user = SupabaseProvider.client.auth.currentSessionOrNull()?.user
                ?: return Result.failure(Exception("No hay usuario logueado"))

            val perfil = SupabaseProvider.client.from("perfil")
                .select { filter { eq("id", user.id) } }
                .decodeList<JsonObject>()
                .firstOrNull()

            val ciudadId = perfil?.get("ciudad_id")
                ?.toString()
                ?.trim('"')
                ?.takeIf { it != "null" && it.isNotEmpty() }

            val metadataCiudadId = user.userMetadata?.get("ciudad_id")
                ?.toString()
                ?.trim('"')
                ?.takeIf { it != "null" && it.isNotEmpty() }

            when {
                ciudadId != null -> Result.success(ciudadId)
                metadataCiudadId != null -> Result.success(metadataCiudadId)
                else -> Result.failure(Exception("Ciudad no configurada en el perfil"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun buscarCiudadPorNombre(nombre: String): Result<String?> {
        return try {
            val ciudad = SupabaseProvider.client.from("ciudad")
                .select { filter { ilike("nombre", nombre) } }
                .decodeList<JsonObject>()
                .firstOrNull()

            val id = ciudad?.get("id")?.toString()?.trim('"')
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}