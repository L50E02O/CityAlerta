package man.tap

import man.tap.navigation.AppNavigation
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.maps.MapsInitializer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Inyectar API Key de Google Maps dinámicamente desde BuildConfig
        val apiKey = BuildConfig.GOOGLE_MAPS_API_KEY
        if (apiKey.isNotEmpty()) {
            MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST) { }
        }
        
        setContent {
            AppNavigation()
        }
    }
}