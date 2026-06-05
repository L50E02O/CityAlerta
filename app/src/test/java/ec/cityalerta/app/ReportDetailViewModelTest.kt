package ec.cityalerta.app

import ec.cityalerta.app.model.data.barrio.Barrio
import ec.cityalerta.app.model.data.geoJson.Geometry
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.data.reporteimagen.ReporteImagen
import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacion
import ec.cityalerta.app.model.repository.BarrioRepository
import ec.cityalerta.app.model.repository.ReporteImagenRepository
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteStorageRepository
import ec.cityalerta.app.model.repository.ReporteUbicacionRepository
import ec.cityalerta.app.util.MainDispatcherRule
import ec.cityalerta.app.viewmodel.ReportDetailViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReportDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var reporteRepository: ReporteRepository
    @Mock
    private lateinit var reporteImagenRepository: ReporteImagenRepository
    @Mock
    private lateinit var reporteUbicacionRepository: ReporteUbicacionRepository
    @Mock
    private lateinit var reporteStorageRepository: ReporteStorageRepository
    @Mock
    private lateinit var barrioRepository: BarrioRepository

    private lateinit var viewModel: ReportDetailViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = ReportDetailViewModel(
            reporteRepository,
            reporteImagenRepository,
            reporteUbicacionRepository,
            reporteStorageRepository,
            barrioRepository
        )
    }

    @Test
    fun testInitialState() {
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.report)
        assertNull(state.error)
    }

    @Test
    fun testLoadReportDetail_Success() = runTest {
        val reportId = "rep-123"
        val createdAt = Instant.now().minusSeconds(3600).toString() // 1 hour ago
        val reporte = Reporte(
            id = reportId,
            usuario_id = "user-1",
            ciudad_id = "city-1",
            ubicacion_id = "ubic-1",
            descripcion = "Bache molesto",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-01",
            categoria = ReportType.BACHE,
            created_at = createdAt,
            barrio_id = "barrio-1"
        )

        whenever(reporteRepository.getById(reportId)).thenReturn(Result.success(reporte))
        whenever(reporteImagenRepository.getFirstImagenByReporteId(reportId)).thenReturn(Result.success(
            ReporteImagen("img-1", reportId, "uuid-1", "path/1", "2024-01-01")
        ))
        whenever(reporteStorageRepository.generateSignedImageUrl("uuid-1")).thenReturn(Result.success("https://signed-url.com/1"))
        whenever(reporteUbicacionRepository.getById("ubic-1")).thenReturn(Result.success(
            ReporteUbicacion("ubic-1", -1.0, -80.0, "Calle 123")
        ))
        whenever(barrioRepository.getById("barrio-1")).thenReturn(Result.success(
            Barrio("barrio-1", "city-1", "Centro", Geometry("Polygon", emptyList()))
        ))

        viewModel.loadReportDetail(reportId)
        
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.report)
        assertEquals("Bache molesto", state.report?.descripcion)
        assertEquals("Bache", state.report?.categoria)
        assertEquals("Pendiente", state.report?.estado)
        assertEquals("Centro", state.report?.barrio)
        assertEquals("Calle 123", state.report?.direccion)
        assertEquals("https://signed-url.com/1", state.report?.imageUrl)
        assertEquals("Hace 1 hora", state.report?.timeAgo)
    }

    @Test
    fun testLoadReportDetail_NotFound() = runTest {
        val reportId = "non-existent"
        whenever(reporteRepository.getById(reportId)).thenReturn(Result.success(null as Reporte?))

        viewModel.loadReportDetail(reportId)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.report)
        assertEquals("Reporte no encontrado", state.error)
    }

    @Test
    fun testLoadReportDetail_Error() = runTest {
        val reportId = "rep-123"
        whenever(reporteRepository.getById(reportId)).thenThrow(RuntimeException("Network error"))

        viewModel.loadReportDetail(reportId)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.report)
        assertEquals("Network error", state.error)
    }

    @Test
    fun testCalculateTimeAgo_Minutes() = runTest {
        val reportId = "rep-1"
        val createdAt = Instant.now().minusSeconds(120).toString() // 2 minutes ago
        val reporte = Reporte(
            id = "rep-1",
            usuario_id = "u1",
            ciudad_id = "c1",
            ubicacion_id = "ub1",
            descripcion = "desc",
            estado = ReporteEstado.RESUELTO,
            fecha_reporte = "date",
            categoria = ReportType.LUZ,
            created_at = createdAt,
            barrio_id = "b1"
        )

        whenever(reporteRepository.getById(reportId)).thenReturn(Result.success(reporte))
        // Mock others to avoid nulls if necessary, though Result.success(null) might work
        whenever(reporteImagenRepository.getFirstImagenByReporteId(any())).thenReturn(Result.success(null))
        whenever(reporteUbicacionRepository.getById(any())).thenReturn(Result.success(null))
        whenever(barrioRepository.getById(any())).thenReturn(Result.success(null))

        viewModel.loadReportDetail(reportId)
        advanceUntilIdle()

        assertEquals("Hace 2 minutos", viewModel.state.value.report?.timeAgo)
    }
}
