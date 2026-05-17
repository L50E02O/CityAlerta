package ec.cityalerta.app.theme

import android.content.Context
import androidx.compose.runtime.mutableFloatStateOf
import androidx.core.content.edit
import androidx.compose.runtime.mutableStateOf

class AccessibilityManager(context: Context) {
    private val prefs = context.getSharedPreferences("cityalerta_prefs", Context.MODE_PRIVATE)

    val textScaleState = mutableFloatStateOf(
        prefs.getFloat(KEY_TEXT_SCALE, DEFAULT_TEXT_SCALE).coerceIn(MIN_TEXT_SCALE, MAX_TEXT_SCALE)
    )

    val highContrastState = mutableStateOf(prefs.getBoolean(KEY_HIGH_CONTRAST, false))

    fun setTextScale(scale: Float) {
        val normalized = scale.coerceIn(MIN_TEXT_SCALE, MAX_TEXT_SCALE)
        prefs.edit { putFloat(KEY_TEXT_SCALE, normalized) }
        textScaleState.floatValue = normalized
    }

    fun setHighContrast(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_HIGH_CONTRAST, enabled) }
        highContrastState.value = enabled
    }

    companion object {
        const val MIN_TEXT_SCALE = 0.85f
        const val MAX_TEXT_SCALE = 1.35f
        const val DEFAULT_TEXT_SCALE = 1f
        private const val KEY_TEXT_SCALE = "pref_text_scale"
        private const val KEY_HIGH_CONTRAST = "pref_high_contrast"
    }
}
