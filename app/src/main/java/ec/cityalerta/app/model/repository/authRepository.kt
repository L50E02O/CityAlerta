package ec.cityalerta.app.model.repository

import ec.cityalerta.app.BuildConfig
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.remote.SupabaseAuthHttp
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.utils.AuthApiResponseParser
import ec.cityalerta.app.model.utils.AuthErrorMapper
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class AuthRepository : AuthRepositoryContract {
    override suspend fun signUp(email: String, password: String, ciudadId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            SupabaseAuthHttp.signUp(email, password, ciudadId).fold(
                onSuccess = {
                    if (SupabaseProvider.client.auth.currentSessionOrNull() != null) {
                        SupabaseProvider.client.auth.signOut()
                    }
                    Result.success(Unit)
                },
                onFailure = { error -> Result.failure(mapAuthException(error as? Exception ?: Exception(error.message))) }
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            SupabaseProvider.client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun logOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            SupabaseProvider.client.auth.signOut()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyRecoveryEmail(email: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val httpClient = HttpClient(Android)
            try {
                val response: HttpResponse = httpClient.post("${BuildConfig.SUPABASE_URL.trimEnd('/')}/functions/v1/reset-password-by-email") {
                    header("x-reset-secret", BuildConfig.PASSWORD_RESET_SECRET)
                    contentType(ContentType.Application.Json)
                    setBody(
                        buildJsonObject {
                            put("email", email)
                            put("mode", "verify")
                        }.toString()
                    )
                }

                val body = response.bodyAsText()
                if (response.status.value !in 200..299) {
                    val message = runCatching {
                        Json.parseToJsonElement(body)
                            .jsonObject["error"]
                            ?.jsonPrimitive
                            ?.content
                    }.getOrNull().orEmpty().ifBlank { body }
                    Result.failure(Exception(message))
                } else {
                    val emailExists = runCatching {
                        Json.parseToJsonElement(body)
                            .jsonObject["emailExists"]
                            ?.jsonPrimitive
                            ?.booleanOrNull
                            ?: false
                    }.getOrDefault(false)

                    Result.success(emailExists)
                }
            } finally {
                httpClient.close()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun resetPasswordByEmail(email: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val httpClient = HttpClient(Android)
            try {
                val response: HttpResponse = httpClient.post("${BuildConfig.SUPABASE_URL.trimEnd('/')}/functions/v1/reset-password-by-email") {
                    header("x-reset-secret", BuildConfig.PASSWORD_RESET_SECRET)
                    contentType(ContentType.Application.Json)
                    setBody(
                        buildJsonObject {
                            put("email", email)
                            put("newPassword", newPassword)
                        }.toString()
                    )
                }

                val body = response.bodyAsText()
                if (response.status.value !in 200..299) {
                    val message = runCatching {
                        Json.parseToJsonElement(body)
                            .jsonObject["error"]
                            ?.jsonPrimitive
                            ?.content
                    }.getOrNull().orEmpty().ifBlank {
                        "No se pudo actualizar la contrasena: ${response.status.value} $body"
                    }
                    Result.failure(Exception(message))
                } else {
                    Result.success(Unit)
                }
            } finally {
                httpClient.close()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun resendSignupConfirmation(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            SupabaseAuthHttp.resendSignupConfirmation(email).fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { error ->
                    Result.failure(mapAuthException(error as? Exception ?: Exception(error.message)))
                }
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun updateEmail(newEmail: String): Result<Unit> = postManageAccount(
        body = buildJsonObject {
            put("mode", "update_email")
            put("email", newEmail.trim())
        }
    )

    override suspend fun deleteAccount(): Result<Unit> = postManageAccount(
        body = buildJsonObject {
            put("mode", "delete_account")
        }
    )

    private suspend fun postManageAccount(body: JsonObject): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val session = SupabaseProvider.client.auth.currentSessionOrNull()
                ?: return@withContext Result.failure(Exception("No hay sesion activa"))

            val accessToken = session.accessToken
            val httpClient = HttpClient(Android)
            try {
                val resp: HttpResponse = httpClient.post(
                    "${BuildConfig.SUPABASE_URL.trimEnd('/')}/functions/v1/manage-account"
                ) {
                    header("Authorization", "Bearer $accessToken")
                    header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                    contentType(ContentType.Application.Json)
                    setBody(body.toString())
                }

                if (resp.status.value !in 200..299) {
                    val responseBody = resp.bodyAsText()
                    Result.failure(
                        mapAuthException(
                            Exception(AuthApiResponseParser.parseErrorMessage(responseBody, resp.status.value))
                        )
                    )
                } else {
                    if (body["mode"]?.jsonPrimitive?.content == "update_email") {
                        runCatching { SupabaseProvider.client.auth.refreshCurrentSession() }
                    }
                    Result.success(Unit)
                }
            } finally {
                httpClient.close()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val session = SupabaseProvider.client.auth.currentSessionOrNull()
                ?: return@withContext Result.failure(Exception("Abre el enlace del correo antes de actualizar la contrasena"))

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
                    Result.failure(Exception("No se pudo actualizar la contrasena: ${resp.status.value} $body"))
                } else {
                    SupabaseProvider.client.auth.signOut()
                    Result.success(Unit)
                }
            } finally {
                httpClient.close()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserEmail(): Result<String> = withContext(Dispatchers.IO) {
        try {
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

    override suspend fun getUserId(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val session = SupabaseProvider.client.auth.currentSessionOrNull()
            val user = session?.user

            if (user == null) {
                Result.failure(Exception("No hay usuario logueado"))
            } else {
                Result.success(user.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCiudadId(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val user = SupabaseProvider.client.auth.currentSessionOrNull()?.user
                ?: return@withContext Result.failure(Exception("No hay usuario logueado"))

            val perfil = SupabaseProvider.client.from("perfil")
                .select { filter { eq("id", user.id) } }
                .decodeList<JsonObject>()
                .firstOrNull()

            val ciudadId = perfil?.get("ciudad_id")
                ?.toString()
                ?.trim('"')
                ?.takeIf { it != "null" && it.isNotEmpty() }

            if (ciudadId != null) {
                Result.success(ciudadId)
            } else {
                Result.failure(Exception("Ciudad no configurada en el perfil"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun buscarCiudadPorNombre(nombre: String): Result<String?> = withContext(Dispatchers.IO) {
        try {
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

    override fun getCurrentSession(): Any? {
        return SupabaseProvider.client.auth.currentSessionOrNull()
    }

    private fun mapAuthException(exception: Exception): Exception {
        val mapped = AuthErrorMapper.map(exception)
        return AuthMappedException(mapped.message, mapped.isEmailUnconfirmed)
    }
}

class AuthMappedException(
    message: String,
    val isEmailUnconfirmed: Boolean
) : Exception(message)
