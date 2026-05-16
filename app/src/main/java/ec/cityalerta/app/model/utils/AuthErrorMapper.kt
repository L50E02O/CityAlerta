package ec.cityalerta.app.model.utils

data class MappedAuthError(
    val message: String,
    val isEmailUnconfirmed: Boolean = false
)

object AuthErrorMapper {

    fun map(throwable: Throwable): MappedAuthError {
        val raw = buildString {
            append(throwable.message.orEmpty())
            throwable.cause?.message?.let { append(' ').append(it) }
        }.lowercase()

        return when {
            isEmailNotConfirmed(raw) -> MappedAuthError(
                message = "Tu cuenta aun no esta activada. Revisa tu bandeja de entrada y la carpeta de spam, " +
                    "y abre el enlace de confirmacion que te enviamos al registrarte.",
                isEmailUnconfirmed = true
            )
            raw.contains("invalid login credentials") ||
                raw.contains("invalid credentials") ||
                raw.contains("wrong password") -> MappedAuthError(
                message = "Correo o contrasena incorrectos. Verifica tus datos e intenta de nuevo."
            )
            raw.contains("user already registered") ||
                raw.contains("already been registered") -> MappedAuthError(
                message = "Este correo ya tiene una cuenta. Inicia sesion o recupera tu contrasena."
            )
            raw.contains("rate limit") ||
                raw.contains("too many requests") ||
                raw.contains("email rate limit") ||
                raw.contains("over_email_send_rate_limit") -> MappedAuthError(
                message = "Limite de correos alcanzado. Espera unos minutos o pide al administrador " +
                    "que configure SMTP en Supabase (Authentication > SMTP Settings)."
            )
            isEmailDeliveryIssue(raw) -> MappedAuthError(
                message = "No se pudo enviar el correo desde el servidor. El administrador debe activar " +
                    "SMTP en Supabase (Authentication > SMTP Settings) con un servicio como Resend, Brevo o Gmail."
            )
            raw.contains("signup is disabled") -> MappedAuthError(
                message = "El registro no esta disponible en este momento. Intenta mas tarde."
            )
            raw.contains("redirect") && (raw.contains("invalid") || raw.contains("not allowed") || raw.contains("permitida")) -> MappedAuthError(
                message = "Falta configurar la app en Supabase: en Authentication > URL Configuration agrega " +
                    "cityalerta://auth en Redirect URLs."
            )
            else -> MappedAuthError(
                message = throwable.message?.takeIf { it.isNotBlank() }
                    ?: "No se pudo completar la operacion. Intenta de nuevo."
            )
        }
    }

    private fun isEmailDeliveryIssue(raw: String): Boolean {
        return raw.contains("smtp") ||
            raw.contains("mail") && raw.contains("fail") ||
            raw.contains("email address not authorized") ||
            raw.contains("error sending") ||
            raw.contains("unable to send") ||
            raw.contains("email provider")
    }

    private fun isEmailNotConfirmed(raw: String): Boolean {
        return raw.contains("email not confirmed") ||
            raw.contains("email_not_confirmed") ||
            raw.contains("not confirmed") ||
            raw.contains("confirm your email") ||
            raw.contains("email address is not confirmed") ||
            raw.contains("usuario no confirmado")
    }
}
