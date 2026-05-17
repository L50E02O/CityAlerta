package ec.cityalerta.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.CompositionLocalProvider

val LocalThemeManager = staticCompositionLocalOf<ThemeManager> {
    error("No ThemeManager provided")
}

private val LightColors = lightColorScheme(
    primary = Color(0xFF3B5B7A),
    onPrimary = Color.White,
    background = Color(0xFFF6F7F9),
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF3B5B7A),
    onPrimary = Color.White,
    background = Color(0xFF0F1720),
    surface = Color(0xFF1B2633),
)

@Composable
fun CityAlertaTheme(themeManager: ThemeManager, content: @Composable () -> Unit) {
    // Rely on system dark flag, which will reflect AppCompatDelegate night mode when set.
    val isDark = isSystemInDarkTheme()

    val colors: ColorScheme = if (isDark) DarkColors else LightColors

    CompositionLocalProvider(LocalThemeManager provides themeManager) {
        MaterialTheme(
            colorScheme = colors,
            content = content
        )
    }
}
