package ec.cityalerta.app

import android.content.Context
import android.content.SharedPreferences
import ec.cityalerta.app.theme.LocalThemeManagerHolder
import ec.cityalerta.app.theme.ThemeManager
import ec.cityalerta.app.theme.ThemePreference
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertNotNull

class ThemeHolderCoverageTest {

    @Test
    fun localThemeManagerHolder_guardaFactory() {
        val context: Context = mock()
        val prefs: SharedPreferences = mock()
        val editor: SharedPreferences.Editor = mock()
        whenever(context.getSharedPreferences(any(), any())).thenReturn(prefs)
        whenever(prefs.edit()).thenReturn(editor)
        whenever(editor.putString(any(), any())).thenReturn(editor)
        whenever(prefs.getString(any(), any())).thenReturn(ThemePreference.SYSTEM.name)

        LocalThemeManagerHolder.instanceFactory = { ThemeManager(it) }

        val manager = LocalThemeManagerHolder.instanceFactory(context)

        assertNotNull(manager)
    }
}
