package ec.cityalerta.app

import ec.cityalerta.app.navigation.AppNavigation
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.gms.maps.MapsInitializer
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.utils.AuthDeepLinkParser
import io.github.jan.supabase.gotrue.handleDeeplinks
import ec.cityalerta.app.theme.AccessibilityManager
import ec.cityalerta.app.theme.CityAlertaTheme
import ec.cityalerta.app.theme.LocaleManager
import ec.cityalerta.app.theme.ThemeManager
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import ec.cityalerta.app.view.utils.CityAlertaNavigationType

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.applyLocaleToContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemNavigationBar()

        handleAuthIntent(intent)

        setContent {
            val themeManager = androidx.compose.runtime.remember { ThemeManager(this@MainActivity) }
            val accessibilityManager = androidx.compose.runtime.remember { AccessibilityManager(this@MainActivity) }
            val localeManager = androidx.compose.runtime.remember { LocaleManager(this@MainActivity) }
            CityAlertaTheme(
                themeManager = themeManager,
                accessibilityManager = accessibilityManager,
                localeManager = localeManager
            ) {
                LaunchedEffect(Unit) {
                    val apiKey = BuildConfig.GOOGLE_MAPS_API_KEY
                    if (apiKey.isNotEmpty()) {
                        MapsInitializer.initialize(this@MainActivity, MapsInitializer.Renderer.LATEST) { }
                    }
                }
                
                val windowSize = calculateWindowSizeClass(this@MainActivity)
                val navigationType = when (windowSize.widthSizeClass) {
                    WindowWidthSizeClass.Compact -> CityAlertaNavigationType.BOTTOM_NAVIGATION
                    WindowWidthSizeClass.Medium -> CityAlertaNavigationType.NAVIGATION_RAIL
                    WindowWidthSizeClass.Expanded -> CityAlertaNavigationType.PERMANENT_NAVIGATION_DRAWER
                    else -> CityAlertaNavigationType.BOTTOM_NAVIGATION
                }

                AppNavigation(
                    authInfoMessage = null,
                    navigationType = navigationType
                )
            }
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
