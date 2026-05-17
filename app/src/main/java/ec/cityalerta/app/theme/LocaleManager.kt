package ec.cityalerta.app.theme

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.mutableStateOf
import androidx.core.os.LocaleListCompat
import java.util.Locale

enum class AppLanguage(val tag: String?) {
    SYSTEM(null),
    SPANISH("es"),
    ENGLISH("en");

    companion object {
        fun fromStoredTag(tag: String?): AppLanguage {
            return when (tag) {
                SPANISH.tag -> SPANISH
                ENGLISH.tag -> ENGLISH
                else -> SYSTEM
            }
        }
    }
}

class LocaleManager(private val appContext: Context) {

    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val languageState = mutableStateOf(loadLanguage())

    fun setLanguage(language: AppLanguage) {
        prefs.edit().apply {
            if (language.tag == null) {
                remove(KEY_LANGUAGE)
            } else {
                putString(KEY_LANGUAGE, language.tag)
            }
        }.apply()

        languageState.value = language
        applyAppLocales(language)
    }

    fun currentLanguage(): AppLanguage = languageState.value

    private fun loadLanguage(): AppLanguage {
        return AppLanguage.fromStoredTag(prefs.getString(KEY_LANGUAGE, null))
    }

    companion object {
        private const val PREFS_NAME = "cityalerta_prefs"
        private const val KEY_LANGUAGE = "pref_language"

        fun applyStoredLocale(context: Context) {
            val tag = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LANGUAGE, null)
            applyAppLocales(AppLanguage.fromStoredTag(tag))
        }

        fun applyLocaleToContext(context: Context): Context {
            val tag = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LANGUAGE, null)
                ?: return context

            val locale = Locale.forLanguageTag(tag)
            Locale.setDefault(locale)

            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            config.setLocales(android.os.LocaleList.forLanguageTags(tag))
            return context.createConfigurationContext(config)
        }

        private fun applyAppLocales(language: AppLanguage) {
            val locales = when (language) {
                AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
                AppLanguage.SPANISH -> LocaleListCompat.forLanguageTags("es")
                AppLanguage.ENGLISH -> LocaleListCompat.forLanguageTags("en")
            }
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }
}
