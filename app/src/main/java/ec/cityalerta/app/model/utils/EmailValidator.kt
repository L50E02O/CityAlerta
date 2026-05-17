package ec.cityalerta.app.model.utils

import android.util.Patterns

object EmailValidator {
    fun isValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }
}
