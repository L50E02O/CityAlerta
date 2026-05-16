package ec.cityalerta.app.model.local

import android.content.Context

class PasswordRecoveryPreferences(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun canRecover(email: String): Boolean {
        val normalized = normalizeEmail(email)
        if (normalized.isEmpty()) return true
        val lastAttempt = prefs.getLong(prefsKey(normalized), 0L)
        if (lastAttempt == 0L) return true
        return System.currentTimeMillis() - lastAttempt >= ONE_DAY_MS
    }

    fun millisUntilNextRecovery(email: String): Long {
        val normalized = normalizeEmail(email)
        if (normalized.isEmpty()) return 0L
        val lastAttempt = prefs.getLong(prefsKey(normalized), 0L)
        if (lastAttempt == 0L) return 0L
        val remaining = ONE_DAY_MS - (System.currentTimeMillis() - lastAttempt)
        return remaining.coerceAtLeast(0L)
    }

    fun recordRecovery(email: String) {
        val normalized = normalizeEmail(email)
        if (normalized.isEmpty()) return
        prefs.edit().putLong(prefsKey(normalized), System.currentTimeMillis()).apply()
    }

    private fun normalizeEmail(email: String): String = email.trim().lowercase()

    private fun prefsKey(email: String): String = "$KEY_PREFIX$email"

    companion object {
        private const val PREFS_NAME = "password_recovery_prefs"
        private const val KEY_PREFIX = "last_recovery_"
        const val ONE_DAY_MS = 24L * 60L * 60L * 1000L
    }
}
