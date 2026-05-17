package ec.cityalerta.app.theme

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit

enum class ThemePreference { LIGHT, DARK, SYSTEM }

class ThemeManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("cityalerta_prefs", Context.MODE_PRIVATE)
    private val KEY = "pref_theme"

    val themeState = mutableStateOf(ThemePreference.SYSTEM)

    init {
        val name = prefs.getString(KEY, ThemePreference.SYSTEM.name) ?: ThemePreference.SYSTEM.name
        themeState.value = ThemePreference.valueOf(name)
    }

    fun setTheme(theme: ThemePreference) {
        prefs.edit { putString(KEY, theme.name) }
        themeState.value = theme
        when (theme) {
            ThemePreference.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            ThemePreference.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            ThemePreference.SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}

object LocalThemeManagerHolder {
    lateinit var instanceFactory: (Context) -> ThemeManager
}
