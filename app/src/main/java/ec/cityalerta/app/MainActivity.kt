package ec.cityalerta.app

import ec.cityalerta.app.navigation.AppNavigation
import ec.cityalerta.app.navigation.Routes
import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.gms.maps.MapsInitializer
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.utils.AuthDeepLinkParser
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.handleDeeplinks

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemNavigationBar()

        val apiKey = BuildConfig.GOOGLE_MAPS_API_KEY
        if (apiKey.isNotEmpty()) {
            MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST) { }
        }

        handleAuthIntent(intent)

        val linkType = AuthDeepLinkParser.parseType(intent)
        val hasSession = SupabaseProvider.client.auth.currentSessionOrNull() != null

        val startDestination = when {
            !AuthDeepLinkParser.isAppAuthDeepLink(intent) -> Routes.Login.route
            linkType == AuthDeepLinkParser.AuthLinkType.RECOVERY -> Routes.RecoverPassword.route
            hasSession -> Routes.Home.route
            else -> Routes.Login.route
        }

        val authInfoMessage = when {
            AuthDeepLinkParser.isAppAuthDeepLink(intent) &&
                linkType != AuthDeepLinkParser.AuthLinkType.RECOVERY &&
                hasSession -> "Cuenta activada correctamente. Bienvenido a CityAlerta."
            AuthDeepLinkParser.isAppAuthDeepLink(intent) &&
                linkType == AuthDeepLinkParser.AuthLinkType.RECOVERY &&
                hasSession -> null
            else -> null
        }

        setContent {
            AppNavigation(
                startDestination = startDestination,
                authInfoMessage = authInfoMessage
            )
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemNavigationBar()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthIntent(intent)
        if (AuthDeepLinkParser.isAppAuthDeepLink(intent)) {
            recreate()
        }
    }

    private fun handleAuthIntent(intent: Intent?) {
        if (AuthDeepLinkParser.isAppAuthDeepLink(intent)) {
            SupabaseProvider.client.handleDeeplinks(intent!!)
        }
    }

    private fun hideSystemNavigationBar() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
