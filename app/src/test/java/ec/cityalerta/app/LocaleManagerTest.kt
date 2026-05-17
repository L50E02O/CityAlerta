package ec.cityalerta.app

import ec.cityalerta.app.theme.AppLanguage
import org.junit.Test
import kotlin.test.assertEquals

class LocaleManagerTest {

    @Test
    fun fromStoredTag_mapsSpanish() {
        assertEquals(AppLanguage.SPANISH, AppLanguage.fromStoredTag("es"))
    }

    @Test
    fun fromStoredTag_mapsEnglish() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromStoredTag("en"))
    }

    @Test
    fun fromStoredTag_unknownUsesSystem() {
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromStoredTag(null))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromStoredTag("fr"))
    }
}
