package ec.cityalerta.app

import android.content.Intent
import android.net.Uri
import ec.cityalerta.app.model.utils.AuthDeepLinkParser
import ec.cityalerta.app.model.utils.AuthDeepLinkParser.AuthLinkType
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthDeepLinkParserTest {

    @Test
    fun isAppAuthDeepLink_validSchemeAndHost() {
        val uri = mock<Uri>()
        whenever(uri.scheme).thenReturn("cityalerta")
        whenever(uri.host).thenReturn("auth")
        val intent = mock<Intent>()
        whenever(intent.data).thenReturn(uri)

        assertTrue(AuthDeepLinkParser.isAppAuthDeepLink(intent))
    }

    @Test
    fun isAppAuthDeepLink_invalidScheme() {
        val uri = mock<Uri>()
        whenever(uri.scheme).thenReturn("https")
        whenever(uri.host).thenReturn("auth")
        val intent = mock<Intent>()
        whenever(intent.data).thenReturn(uri)

        assertFalse(AuthDeepLinkParser.isAppAuthDeepLink(intent))
    }

    @Test
    fun parseType_recoveryFromQuery() {
        val uri = mock<Uri>()
        whenever(uri.getQueryParameter("type")).thenReturn("recovery")
        whenever(uri.fragment).thenReturn(null)
        val intent = mock<Intent>()
        whenever(intent.data).thenReturn(uri)

        assertEquals(AuthLinkType.RECOVERY, AuthDeepLinkParser.parseType(intent))
    }

    @Test
    fun parseType_signupFromQuery() {
        val uri = mock<Uri>()
        whenever(uri.getQueryParameter("type")).thenReturn("signup")
        whenever(uri.fragment).thenReturn(null)
        val intent = mock<Intent>()
        whenever(intent.data).thenReturn(uri)

        assertEquals(AuthLinkType.SIGNUP, AuthDeepLinkParser.parseType(intent))
    }

    @Test
    fun parseType_recoveryFromFragment() {
        val uri = mock<Uri>()
        whenever(uri.getQueryParameter("type")).thenReturn(null)
        whenever(uri.fragment).thenReturn("type=recovery")
        val intent = mock<Intent>()
        whenever(intent.data).thenReturn(uri)

        assertEquals(AuthLinkType.RECOVERY, AuthDeepLinkParser.parseType(intent))
    }

    @Test
    fun parseType_otherWhenMissingData() {
        assertEquals(AuthLinkType.OTHER, AuthDeepLinkParser.parseType(null))
        val intent = mock<Intent>()
        whenever(intent.data).thenReturn(null)
        assertEquals(AuthLinkType.OTHER, AuthDeepLinkParser.parseType(intent))
    }
}
