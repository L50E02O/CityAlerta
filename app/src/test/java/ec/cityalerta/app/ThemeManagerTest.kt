package ec.cityalerta.app

import android.content.Context
import android.content.SharedPreferences
import ec.cityalerta.app.theme.ThemeManager
import ec.cityalerta.app.theme.ThemePreference
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

class ThemeManagerTest {

    private val context: Context = mock()
    private val prefs: SharedPreferences = mock()
    private val editor: SharedPreferences.Editor = mock()

    @Before
    fun setUp() {
        whenever(context.getSharedPreferences(any(), any())).thenReturn(prefs)
        whenever(prefs.edit()).thenReturn(editor)
        whenever(editor.putString(any(), any())).thenReturn(editor)
        whenever(prefs.getString(any(), any())).thenReturn(ThemePreference.DARK.name)
    }

    @Test
    fun init_loadsStoredTheme() {
        val manager = ThemeManager(context)

        assertEquals(ThemePreference.DARK, manager.themeState.value)
    }

    @Test
    fun setTheme_persistsPreference() {
        val manager = ThemeManager(context)

        manager.setTheme(ThemePreference.LIGHT)

        verify(editor).putString(any(), org.mockito.kotlin.eq(ThemePreference.LIGHT.name))
        assertEquals(ThemePreference.LIGHT, manager.themeState.value)
    }
}
