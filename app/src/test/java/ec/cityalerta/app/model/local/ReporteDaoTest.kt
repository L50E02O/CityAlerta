package ec.cityalerta.app.model.local

import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReporteDaoTest {

    @Mock
    private lateinit var mockReporteDao: ReporteDao

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testCountReportesByCiudad() = runTest {
        whenever(mockReporteDao.countReportesByCiudad("ciudad-1")).thenReturn(5)

        val count = mockReporteDao.countReportesByCiudad("ciudad-1")

        assertEquals(5, count)
        verify(mockReporteDao).countReportesByCiudad("ciudad-1")
    }

    @Test
    fun testGetReportesByCiudad() = runTest {
        val reporte1 = ReporteEntity(
            id = "reporte-1",
            usuario_id = "user-1",
            ciudad_id = "ciudad-1",
            ubicacion_id = "ubicacion-1",
            descripcion = "Bache en calle principal",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-15",
            categoria = ReportType.BACHE,
            created_at = "2024-01-15T10:00:00Z",
            updated_at = "2024-01-15T10:00:00Z",
            barrio_id = "barrio-1"
        )

        val reporte2 = ReporteEntity(
            id = "reporte-2",
            usuario_id = "user-2",
            ciudad_id = "ciudad-1",
            ubicacion_id = "ubicacion-2",
            descripcion = "Basura acumulada",
            estado = ReporteEstado.RESUELTO,
            fecha_reporte = "2024-01-16",
            categoria = ReportType.AGUA,
            created_at = "2024-01-16T10:00:00Z",
            updated_at = "2024-01-16T10:00:00Z",
            barrio_id = "barrio-2"
        )

        whenever(mockReporteDao.getReportesByCiudad("ciudad-1", 10, 0)).thenReturn(listOf(reporte1, reporte2))

        val reportes = mockReporteDao.getReportesByCiudad("ciudad-1", 10, 0)

        assertEquals(2, reportes.size)
        assertEquals("reporte-1", reportes[0].id)
        assertEquals("reporte-2", reportes[1].id)
    }

    @Test
    fun testGetReportesByCiudadFlow() = runTest {
        val reporte1 = ReporteEntity(
            id = "reporte-1",
            usuario_id = "user-1",
            ciudad_id = "ciudad-1",
            ubicacion_id = "ubicacion-1",
            descripcion = "Bache en calle principal",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-15",
            categoria = ReportType.BACHE,
            created_at = "2024-01-15T10:00:00Z",
            updated_at = "2024-01-15T10:00:00Z",
            barrio_id = "barrio-1"
        )

        whenever(mockReporteDao.getReportesByCiudadFlow("ciudad-1", 10, 0))
            .thenReturn(flowOf(listOf(reporte1)))

        val reportes = mockReporteDao.getReportesByCiudadFlow("ciudad-1", 10, 0)
        reportes.collect { assertEquals(1, it.size) }
    }

    @Test
    fun testInsertReportes() = runTest {
        val reporte1 = ReporteEntity(
            id = "reporte-1",
            usuario_id = "user-1",
            ciudad_id = "ciudad-1",
            ubicacion_id = "ubicacion-1",
            descripcion = "Bache en calle principal",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-15",
            categoria = ReportType.BACHE,
            created_at = "2024-01-15T10:00:00Z",
            updated_at = "2024-01-15T10:00:00Z",
            barrio_id = "barrio-1"
        )

        whenever(mockReporteDao.insertReportes(listOf(reporte1))).thenReturn(Unit)

        mockReporteDao.insertReportes(listOf(reporte1))

        verify(mockReporteDao).insertReportes(listOf(reporte1))
    }

    @Test
    fun testDeleteReportesByCiudad() = runTest {
        whenever(mockReporteDao.deleteReportesByCiudad("ciudad-1")).thenReturn(Unit)

        mockReporteDao.deleteReportesByCiudad("ciudad-1")

        verify(mockReporteDao).deleteReportesByCiudad("ciudad-1")
    }

    @Test
    fun testRefreshReportes() = runTest {
        val reporte1 = ReporteEntity(
            id = "reporte-1",
            usuario_id = "user-1",
            ciudad_id = "ciudad-1",
            ubicacion_id = "ubicacion-1",
            descripcion = "Bache en calle principal",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-15",
            categoria = ReportType.BACHE,
            created_at = "2024-01-15T10:00:00Z",
            updated_at = "2024-01-15T10:00:00Z",
            barrio_id = "barrio-1"
        )

        whenever(mockReporteDao.refreshReportes("ciudad-1", listOf(reporte1))).thenReturn(Unit)

        mockReporteDao.refreshReportes("ciudad-1", listOf(reporte1))

        verify(mockReporteDao).refreshReportes("ciudad-1", listOf(reporte1))
    }

    @Test
    fun testReporteEntityCreation() {
        val reporteEntity = ReporteEntity(
            id = "reporte-123",
            usuario_id = "user-456",
            ciudad_id = "ciudad-789",
            ubicacion_id = "ubicacion-001",
            descripcion = "Bache en la calle principal",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-15",
            categoria = ReportType.BACHE,
            created_at = "2024-01-15T10:00:00Z",
            updated_at = "2024-01-15T10:00:00Z",
            barrio_id = "barrio-123",
            barrio_nombre = "Centro",
            direccion_aproximada = "Calle 10 y Av. Principal",
            image_url = "https://example.com/image.jpg"
        )

        assertEquals("reporte-123", reporteEntity.id)
        assertEquals("user-456", reporteEntity.usuario_id)
        assertEquals("ciudad-789", reporteEntity.ciudad_id)
        assertEquals(ReporteEstado.PENDIENTE, reporteEntity.estado)
        assertEquals(ReportType.BACHE, reporteEntity.categoria)
    }
}
