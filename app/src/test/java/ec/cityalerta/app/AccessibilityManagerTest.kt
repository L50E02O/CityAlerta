package ec.cityalerta.app

import android.content.Context
import android.content.SharedPreferences
import ec.cityalerta.app.theme.AccessibilityManager
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessibilityManagerTest {

    private val context: Context = mock()
    private val prefs: SharedPreferences = mock()
    private val editor: SharedPreferences.Editor = mock()

    @Before
    fun setUp() {
        whenever(context.getSharedPreferences(any(), any())).thenReturn(prefs)
        whenever(prefs.edit()).thenReturn(editor)
        whenever(editor.putFloat(any(), any())).thenReturn(editor)
        whenever(editor.putBoolean(any(), any())).thenReturn(editor)
        whenever(prefs.getFloat(any(), any())).thenReturn(1f)
        whenever(prefs.getBoolean(any(), any())).thenReturn(false)
    }

    @Test
    fun setTextScale_clampsAndPersists() {
        val manager = AccessibilityManager(context)

        manager.setTextScale(2f)

        verify(editor).putFloat(any(), org.mockito.kotlin.eq(AccessibilityManager.MAX_TEXT_SCALE))
        assertEquals(AccessibilityManager.MAX_TEXT_SCALE, manager.textScaleState.floatValue)
    }

    @Test
    fun setHighContrast_persistsValue() {
        val manager = AccessibilityManager(context)

        manager.setHighContrast(true)

        verify(editor).putBoolean(any(), org.mockito.kotlin.eq(true))
        assertTrue(manager.highContrastState.value)
    }
}
