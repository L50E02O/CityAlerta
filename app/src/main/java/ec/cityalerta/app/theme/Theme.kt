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
    surface = Color.White,
    onSurface = Color(0xFF1B2633),
    outline = Color(0xFFE0E0E0)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE53935),
    onPrimary = Color.White,
    secondary = Color(0xFF1B2633),
    onSecondary = Color.White,
    background = Color(0xFF0F1720),
    surface = Color(0xFF1B2633),
    onSurface = Color.White,
    outline = Color(0xFF303E4D)
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
