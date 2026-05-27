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
    primary = CityRed,
    onPrimary = White,
    secondary = LightOnSurface,
    onSecondary = White,
    tertiary = ActionBlue,
    onTertiary = White,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant
)

private val DarkColors = darkColorScheme(
    primary = CityRed,
    onPrimary = White,
    secondary = DarkSecondary,
    onSecondary = White,
    tertiary = ActionBlueDark,
    onTertiary = White,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = ErrorRed,
    onError = Black,
    errorContainer = CityRedDark.copy(alpha = 0.2f),
    onErrorContainer = White
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
            primary = HighContrastPrimaryDark,
            background = HighContrastDarkBackground,
            surface = HighContrastDarkSurface
        )
        highContrast && !isDark -> LightColors.copy(
            primary = HighContrastPrimaryLight,
            background = White,
            surface = HighContrastSurfaceLight
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
