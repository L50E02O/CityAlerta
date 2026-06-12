package ec.cityalerta.app.model.repository

import ec.cityalerta.app.BuildConfig
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.data.contracts.auth.AuthState
import ec.cityalerta.app.model.data.contracts.session.SessionRepositoryContract
import ec.cityalerta.app.model.data.local.UserSessionEntity
import ec.cityalerta.app.model.remote.SupabaseAuthHttp
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.utils.AuthApiResponseParser
import ec.cityalerta.app.model.utils.AuthErrorMapper
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserSession
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

class AuthRepository(
    private val sessionRepository: SessionRepositoryContract
) : AuthRepositoryContract {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    private val authStateFlow = _authState.asStateFlow()

    override fun observeAuthState(): Flow<AuthState> = authStateFlow

    override suspend fun signUp(email: String, password: String, ciudadId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseProvider.client.auth.signUpWith(Email) {
                    this.email = email.trim()
                    this.password = password
                    data = buildJsonObject {
                        put("ciudad_id", ciudadId)
                        put("nombre_completo", "Usuario")
                    }
                }

                val session = SupabaseProvider.client.auth.currentSessionOrNull()
                val user = session?.user
                val isConfirmed = user?.emailConfirmedAt != null

                if (session != null && !isConfirmed) {
                    SupabaseProvider.client.auth.signOut()
                    _authState.value = AuthState.EmailVerificationPending(email.trim())
                } else if (isConfirmed && session != null) {
                    val roomId = ensureRoomId(user!!.id, email.trim()).getOrElse { return@withContext Result.failure(it) }
                    persistCurrentSession(roomId)
                    _authState.value = AuthState.Authenticated(user.id)
                } else {
                    _authState.value = AuthState.EmailVerificationPending(email.trim())
                }

                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(mapAuthException(e))
            }
        }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseProvider.client.auth.signInWith(Email) {
                    this.email = email.trim()
                    this.password = password
                }

                val user = SupabaseProvider.client.auth.currentUserOrNull()
                    ?: return@withContext Result.failure(Exception("No hay usuario logueado"))

                if (user.emailConfirmedAt == null) {
                    SupabaseProvider.client.auth.signOut()
                    _authState.value = AuthState.EmailVerificationPending(email.trim())
                    return@withContext Result.failure(
                        AuthMappedException(
                            "Debes confirmar tu correo antes de iniciar sesion",
                            isEmailUnconfirmed = true
                        )
                    )
                }

                val roomId = ensureRoomId(user.id, user.email.orEmpty()).getOrElse { return@withContext Result.failure(it) }
                persistCurrentSession(roomId)
                _authState.value = AuthState.Authenticated(user.id)
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(mapAuthException(e))
            }
        }
    }

    override suspend fun checkEmailVerified(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                runCatching { SupabaseProvider.client.auth.refreshCurrentSession() }

                val user = SupabaseProvider.client.auth.currentUserOrNull()
                val confirmed = user?.emailConfirmedAt != null

                if (confirmed && user != null) {
                    val roomId = ensureRoomId(user.id, user.email.orEmpty()).getOrElse { return@withContext Result.failure(it) }
                    persistCurrentSession(roomId)
                    _authState.value = AuthState.Authenticated(user.id)
                }

                Result.success(confirmed)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(mapAuthException(e))
            }
        }
    }

    override suspend fun restoreSession(accessToken: String, refreshToken: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseProvider.client.auth.importSession(
                    UserSession(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        expiresIn = 3600,
                        tokenType = "bearer",
                        user = null
                    )
                )
                runCatching { SupabaseProvider.client.auth.refreshCurrentSession() }

                val user = SupabaseProvider.client.auth.currentUserOrNull()
                    ?: return@withContext Result.failure(Exception("Sesion invalida"))

                if (user.emailConfirmedAt == null) {
                    SupabaseProvider.client.auth.signOut()
                    return@withContext Result.failure(Exception("Correo no verificado"))
                }

                _authState.value = AuthState.Authenticated(user.id)
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(mapAuthException(e))
            }
        }
    }

    override suspend fun refreshAndPersistSession(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseProvider.client.auth.refreshCurrentSession()
                val user = SupabaseProvider.client.auth.currentUserOrNull()
                    ?: return@withContext Result.failure(Exception("No hay sesion activa"))

                val roomId = ensureRoomId(user.id, user.email.orEmpty()).getOrElse { return@withContext Result.failure(it) }
                persistCurrentSession(roomId)
                _authState.value = AuthState.Authenticated(user.id)
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(mapAuthException(e))
            }
        }
    }

    override suspend fun logOut(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseProvider.client.auth.signOut()
                sessionRepository.clearSession()
                _authState.value = AuthState.Unauthenticated
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getRoomId(): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val user = SupabaseProvider.client.auth.currentUserOrNull()
                    ?: return@withContext Result.failure(Exception("No hay usuario logueado"))
                ensureRoomId(user.id, user.email.orEmpty())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun persistCurrentSession(roomId: String) {
        val session = SupabaseProvider.client.auth.currentSessionOrNull() ?: return
        val user = session.user ?: return
        val expiresAt = System.currentTimeMillis() + (session.expiresIn ?: 3600L) * 1000L
        sessionRepository.saveSession(
            UserSessionEntity(
                userId = user.id,
                email = user.email.orEmpty(),
                roomId = roomId,
                accessToken = session.accessToken,
                refreshToken = session.refreshToken.orEmpty(),
                expiresAt = expiresAt
            )
        )
    }

    private suspend fun ensureRoomId(userId: String, email: String): Result<String> {
        return try {
            val existing = SupabaseProvider.client.from("profiles")
                .select { filter { eq("id", userId) } }
                .decodeList<JsonObject>()
                .firstOrNull()

            val existingRoomId = existing?.get("room_id")
                ?.jsonPrimitive
                ?.content
                ?.takeIf { it.isNotBlank() }

            if (existingRoomId != null) {
                return Result.success(existingRoomId)
            }

            val roomId = UUID.randomUUID().toString()
            if (existing == null) {
                SupabaseProvider.client.from("profiles").insert(
                    buildJsonObject {
                        put("id", userId)
                        put("email", email)
                        put("room_id", roomId)
                    }
                )
            } else {
                SupabaseProvider.client.from("profiles").update(
                    buildJsonObject { put("room_id", roomId) }
                ) { filter { eq("id", userId) } }
            }
            Result.success(roomId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyRecoveryEmail(email: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
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
                        return@withContext Result.failure(Exception(message))
                    }

                    val emailExists = runCatching {
                        Json.parseToJsonElement(body)
                            .jsonObject["emailExists"]
                            ?.jsonPrimitive
                            ?.booleanOrNull
                            ?: false
                    }.getOrDefault(false)

                    Result.success(emailExists)
                } finally {
                    httpClient.close()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(mapAuthException(e))
            }
        }
    }

    override suspend fun resetPasswordByEmail(email: String, newPassword: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
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
                        return@withContext Result.failure(Exception(message))
                    }
                } finally {
                    httpClient.close()
                }

                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(mapAuthException(e))
            }
        }
    }

    override suspend fun resendSignupConfirmation(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
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
    }

    override suspend fun updateEmail(newEmail: String): Result<Unit> {
        return postManageAccount(
            body = buildJsonObject {
                put("mode", "update_email")
                put("email", newEmail.trim())
            }
        )
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return postManageAccount(
            body = buildJsonObject {
                put("mode", "delete_account")
            }
        )
    }

    private suspend fun postManageAccount(body: JsonObject): Result<Unit> {
        return withContext(Dispatchers.IO) {
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
                        return@withContext Result.failure(
                            mapAuthException(
                                Exception(AuthApiResponseParser.parseErrorMessage(responseBody, resp.status.value))
                            )
                        )
                    }
                } finally {
                    httpClient.close()
                }

                if (body["mode"]?.jsonPrimitive?.content == "update_email") {
                    runCatching { SupabaseProvider.client.auth.refreshCurrentSession() }
                }

                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(mapAuthException(e))
            }
        }
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
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
                        return@withContext Result.failure(Exception("No se pudo actualizar la contrasena: ${resp.status.value} $body"))
                    }
                } finally {
                    httpClient.close()
                }

                SupabaseProvider.client.auth.signOut()
                sessionRepository.clearSession()
                _authState.value = AuthState.Unauthenticated
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getUserEmail(): Result<String> {
        return withContext(Dispatchers.IO) {
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
    }

    override suspend fun getUserId(): Result<String> {
        return withContext(Dispatchers.IO) {
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
    }

    override suspend fun getCiudadId(): Result<String> {
        return withContext(Dispatchers.IO) {
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
    }

    override suspend fun buscarCiudadPorNombre(nombre: String): Result<String?> {
        return withContext(Dispatchers.IO) {
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
