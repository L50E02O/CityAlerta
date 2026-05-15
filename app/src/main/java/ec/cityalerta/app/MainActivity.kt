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
import io.github.jan.supabase.gotrue.handleDeeplinks

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemNavigationBar()
        
        // Inyectar API Key de Google Maps dinámicamente desde BuildConfig
        val apiKey = BuildConfig.GOOGLE_MAPS_API_KEY
        if (apiKey.isNotEmpty()) {
            MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST) { }
        }

        handleAuthIntent(intent)

        val startDestination = if (isAuthRecoveryIntent(intent)) {
            Routes.RecoverPassword.route
        } else {
            Routes.Login.route
        }
        
        setContent {
            AppNavigation(startDestination = startDestination)
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
        if (isAuthRecoveryIntent(intent)) {
            recreate()
        }
    }

    private fun handleAuthIntent(intent: Intent?) {
        if (isAuthRecoveryIntent(intent)) {
            SupabaseProvider.client.handleDeeplinks(intent!!)
        }
    }

    private fun isAuthRecoveryIntent(intent: Intent?): Boolean {
        val data = intent?.data ?: return false
        return data.scheme == "cityalerta" && data.host == "auth"
    }

    private fun hideSystemNavigationBar() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}