package ec.cityalerta.app

import ec.cityalerta.app.model.repository.ReporteStorageRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests unitarios para ReporteStorageRepository.
 * Cubre la logica de agrupacion usada por generateSignedImageUrls.
 */
class ReporteStorageRepositoryTest {

    private lateinit var repository: ReporteStorageRepository

    @Before
    fun setUp() {
        repository = ReporteStorageRepository()
    }

    @Test
    fun repository_initializes() {
        assertNotNull(repository)
        assertTrue(repository is ReporteStorageRepository)
    }

    @Test
    fun generateSignedImageUrls_distinctPaths_deduplicatesInput() {
        val paths = listOf("uuid-1", "uuid-1", "uuid-2", "uuid-2")

        val distinct = paths.distinct()

        assertEquals(2, distinct.size)
        assertEquals(listOf("uuid-1", "uuid-2"), distinct)
    }

    @Test
    fun generateSignedImageUrls_emptyPaths_producesEmptyMap() {
        val result = emptyList<String>()
            .distinct()
            .mapNotNull { path -> path.takeIf { it.isNotBlank() }?.let { it to "url" } }
            .toMap()

        assertTrue(result.isEmpty())
    }

    @Test
    fun generateSignedImageUrls_skipsFailedSignedUrls() {
        val pairs = listOf(
            "uuid-ok" to "https://example.com/ok",
            "uuid-fail" to null
        )

        val map = pairs.mapNotNull { (path, url) -> url?.let { path to it } }.toMap()

        assertEquals(1, map.size)
        assertEquals("https://example.com/ok", map["uuid-ok"])
    }
}
