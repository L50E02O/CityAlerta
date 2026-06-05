package ec.cityalerta.app.model.remote

import ec.cityalerta.app.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Llamadas directas a GoTrue para forzar [redirect_to] en correos de registro y reenvio.
 */
internal object SupabaseAuthHttp {

    suspend fun signUp(
        email: String,
        password: String,
        ciudadId: String
    ): Result<Unit> = postAuth(
        path = "signup",
        body = buildJsonObject {
            put("email", email.trim())
            put("password", password)
            put("data", buildJsonObject {
                put("ciudad_id", ciudadId)
                put("nombre_completo", "Usuario")
            })
        }.toString()
    )

    suspend fun resendSignupConfirmation(email: String): Result<Unit> = postAuth(
        path = "resend",
        body = buildJsonObject {
            put("type", "signup")
            put("email", email.trim())
        }.toString()
    )

    private suspend fun postAuth(path: String, body: String): Result<Unit> {
        val httpClient = HttpClient(Android)
        return try {
            // Construimos la URL limpia con el redirect_to una sola vez
            val url = "${BuildConfig.SUPABASE_URL.trimEnd('/')}/auth/v1/$path?redirect_to=${AuthRedirectUrls.APP_DEEP_LINK}"
            
            val response: HttpResponse = httpClient.post(url) {
                header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                header("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(Exception(parseGoTrueError(errorBody, response.status.value)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            httpClient.close()
        }
    }

    private fun parseGoTrueError(body: String, status: Int): String {
        val lowered = body.lowercase()
        return when {
            lowered.contains("email not confirmed") -> "Email not confirmed"
            lowered.contains("already registered") -> "User already registered"
            lowered.contains("limit exceeded") || lowered.contains("rate limit") || status == 429 ->
                "Limite de correos alcanzado. Espera unos minutos o desactiva 'Confirm Email' en el panel de Supabase."
            else -> "Error de autenticacion ($status): $body"
        }
    }
}
