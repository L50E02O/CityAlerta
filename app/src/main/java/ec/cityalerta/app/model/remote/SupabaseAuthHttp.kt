package ec.cityalerta.app.model.remote

import ec.cityalerta.app.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.net.URLEncoder

/**
 * Llamadas directas a GoTrue para forzar [redirect_to] en correos de registro y reenvio.
 * Sin esto, Supabase usa la Site URL del panel (p. ej. localhost).
 */
internal object SupabaseAuthHttp {

    private val redirectEncoded: String =
        URLEncoder.encode(AuthRedirectUrls.APP_DEEP_LINK, Charsets.UTF_8.name())

    suspend fun signUp(
        email: String,
        password: String,
        ciudadId: String
    ): Result<Unit> = postAuth(
        path = "signup",
        body = buildJsonObject {
            put("email", email)
            put("password", password)
            put("redirect_to", AuthRedirectUrls.APP_DEEP_LINK)
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
            put("email", email)
            put("redirect_to", AuthRedirectUrls.APP_DEEP_LINK)
        }.toString()
    )

    suspend fun resetPasswordForEmail(email: String): Result<Unit> = postAuth(
        path = "recover",
        body = buildJsonObject {
            put("email", email)
            put("redirect_to", AuthRedirectUrls.APP_DEEP_LINK)
        }.toString()
    )

    private suspend fun postAuth(path: String, body: String): Result<Unit> {
        val httpClient = HttpClient(Android)
        return try {
            val response: HttpResponse = httpClient.post(authUrl(path)) {
                header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                header("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                contentType(ContentType.Application.Json)
                parameter("redirect_to", AuthRedirectUrls.APP_DEEP_LINK)
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

    private fun authUrl(path: String): String {
        val base = BuildConfig.SUPABASE_URL.trimEnd('/')
        return "$base/auth/v1/$path?redirect_to=$redirectEncoded"
    }

    private fun parseGoTrueError(body: String, status: Int): String {
        val lowered = body.lowercase()
        return when {
            lowered.contains("email not confirmed") -> "Email not confirmed"
            lowered.contains("already registered") || lowered.contains("user already registered") ->
                "User already registered"
            lowered.contains("invalid") && lowered.contains("redirect") ->
                "La URL cityalerta://auth no esta permitida en Supabase. Agregala en Authentication > URL Configuration > Redirect URLs."
            else -> "Error de autenticacion ($status): $body"
        }
    }
}
