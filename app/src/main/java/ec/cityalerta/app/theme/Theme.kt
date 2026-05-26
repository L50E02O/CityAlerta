package ec.cityalerta.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

val LocalThemeManager = staticCompositionLocalOf<ThemeManager> {
    error("No ThemeManager provided")
}

val LocalAccessibilityManager = staticCompositionLocalOf<AccessibilityManager> {
    error("No AccessibilityManager provided")
}

val LocalLocaleManager = staticCompositionLocalOf<LocaleManager> {
    error("No LocaleManager provided")
}

private val LightColors = lightColorScheme(
    primary = Color(0xFFE53935), // Rojo de los botones
    onPrimary = Color.White,
    secondary = Color(0xFF1B2633), // Azul oscuro
    onSecondary = Color.White,
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1B2633),
    surface = Color.White,
    onSurface = Color(0xFF1B2633),
    surfaceVariant = Color(0xFFF8F9FA),
    onSurfaceVariant = Color(0xFF6C757D),
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFE9ECEF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE53935),
    onPrimary = Color.White,
    secondary = Color(0xFF3B5B7A), // Lighter blue for secondary in dark mode
    onSecondary = Color.White,
    background = Color(0xFF0F1720),
    onBackground = Color.White,
    surface = Color(0xFF1B2633),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF141D26),
    onSurfaceVariant = Color(0xFFADB5BD),
    outline = Color(0xFF303E4D),
    outlineVariant = Color(0xFF3E4E5E),
    error = Color(0xFFCF6679),
    onError = Color.Black,
    errorContainer = Color(0xFFB00020).copy(alpha = 0.2f),
    onErrorContainer = Color.White
)

@Composable
fun CityAlertaTheme(
    themeManager: ThemeManager,
    accessibilityManager: AccessibilityManager,
    localeManager: LocaleManager,
    content: @Composable () -> Unit
) {
    val themePreference by themeManager.themeState
    val textScale by accessibilityManager.textScaleState
    val highContrast by accessibilityManager.highContrastState

    val isDark = when (themePreference) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }

    val colors: ColorScheme = when {
        highContrast && isDark -> DarkColors.copy(
            primary = Color(0xFF5B8FC7),
            background = Color(0xFF000000),
            surface = Color(0xFF1A1A1A)
        )
        highContrast && !isDark -> LightColors.copy(
            primary = Color(0xFF1E4D7B),
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFF0F0F0)
        )
        isDark -> DarkColors
        else -> LightColors
    }

    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalThemeManager provides themeManager,
        LocalAccessibilityManager provides accessibilityManager,
        LocalLocaleManager provides localeManager,
        LocalDensity provides Density(density.density, textScale)
    ) {
        MaterialTheme(
            colorScheme = colors,
            content = content
        )
    }
}
