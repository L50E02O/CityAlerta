package ec.cityalerta.app

import ec.cityalerta.app.model.data.perfil.PerfilResumen
import ec.cityalerta.app.model.local.PerfilResumenDao
import ec.cityalerta.app.model.local.PerfilResumenEntity
import ec.cityalerta.app.model.repository.PerfilLocalRepository
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PerfilLocalRepositoryTest {

    private lateinit var repository: PerfilLocalRepository

    @Mock
    private lateinit var mockDao: PerfilResumenDao

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = PerfilLocalRepository(mockDao)
    }

    @Test
    fun testObtenerPerfilResumen_Success() = runTest {
        val entity = PerfilResumenEntity(
            id = "user-1",
            nombreCompleto = "John Doe",
            rolSlug = "ciudadano",
            activo = true,
            ciudadId = "city-1",
            totalReportes = 10,
            reportesResueltos = 5
        )
        whenever(mockDao.obtenerPerfilResumen()).thenReturn(entity)

        val result = repository.obtenerPerfilResumen()

        assertEquals("user-1", result?.id)
        assertEquals("John Doe", result?.nombreCompleto)
        assertEquals("ciudadano", result?.rolSlug)
        assertEquals(true, result?.activo)
        assertEquals("city-1", result?.ciudadId)
        assertEquals(10, result?.totalReportes)
        assertEquals(5, result?.reportesResueltos)
    }

    @Test
    fun testObtenerPerfilResumen_Null_ReturnsNull() = runTest {
        whenever(mockDao.obtenerPerfilResumen()).thenReturn(null)

        val result = repository.obtenerPerfilResumen()

        assertNull(result)
    }

    @Test
    fun testGuardarPerfilResumen_Success() = runTest {
        val perfil = PerfilResumen(
            id = "user-1",
            nombreCompleto = "John Doe",
            rolSlug = "ciudadano",
            activo = true,
            ciudadId = "city-1",
            totalReportes = 10,
            reportesResueltos = 5
        )
        whenever(mockDao.guardarPerfilResumen(org.mockito.kotlin.any())).thenReturn(Unit)

        repository.guardarPerfilResumen(perfil)
    }

    @Test
    fun testBorrarPerfilResumen_Success() = runTest {
        whenever(mockDao.borrarPerfilResumen()).thenReturn(Unit)

        repository.borrarPerfilResumen()
    }
}
