package ec.cityalerta.app.model.utils

object EmailValidator {
    private val emailPattern = Regex(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
        RegexOption.IGNORE_CASE
    )

    fun isValid(email: String): Boolean {
        return emailPattern.matches(email.trim())
    }
}
