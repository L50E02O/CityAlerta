package ec.cityalerta.app.model.utils

import android.content.Intent
import android.net.Uri

object AuthDeepLinkParser {

    enum class AuthLinkType {
        RECOVERY,
        SIGNUP,
        OTHER
    }

    fun parseType(intent: Intent?): AuthLinkType {
        val uri = intent?.data ?: return AuthLinkType.OTHER
        val type = readTypeParam(uri)?.lowercase().orEmpty()
        return when {
            type == "recovery" -> AuthLinkType.RECOVERY
            type == "signup" || type == "email" || type == "invite" -> AuthLinkType.SIGNUP
            uri.fragment?.contains("type=recovery", ignoreCase = true) == true -> AuthLinkType.RECOVERY
            else -> AuthLinkType.OTHER
        }
    }

    fun isAppAuthDeepLink(intent: Intent?): Boolean {
        val data = intent?.data ?: return false
        return data.scheme == "cityalerta" && data.host == "auth"
    }

    private fun readTypeParam(uri: Uri): String? {
        uri.getQueryParameter("type")?.let { return it }
        val fragment = uri.fragment ?: return null
        return fragment.split("&")
            .mapNotNull { part ->
                val keyValue = part.split("=", limit = 2)
                if (keyValue.size == 2 && keyValue[0].equals("type", ignoreCase = true)) {
                    keyValue[1]
                } else {
                    null
                }
            }
            .firstOrNull()
    }
}
