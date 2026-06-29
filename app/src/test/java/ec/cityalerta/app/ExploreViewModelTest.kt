package ec.cityalerta.app

import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.data.geoJson.Geometry
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.repository.*
import ec.cityalerta.app.util.MainDispatcherRule
import ec.cityalerta.app.viewmodel.ExploreViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var authRepository: AuthRepositoryContract
    @Mock
    private lateinit var reporteRepository: ReporteRepository
    @Mock
    private lateinit var reporteStorageRepository: ReporteStorageRepository
    @Mock
    private lateinit var ciudadRepository: CiudadRepository

    private lateinit var viewModel: ExploreViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = ExploreViewModel(
            authRepository,
            reporteRepository,
            reporteStorageRepository,
            ciudadRepository
        )
    }

    @Test
    fun testInitialState() {
        assertEquals("Cargando...", viewModel.state.value.ciudadNombre)
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.reportes.isEmpty())
    }

    @Test
    fun testLoadData_Success() = runTest {
        val ciudadId = "city-1"
        whenever(authRepository.getCiudadId()).thenReturn(Result.success(ciudadId))
        whenever(ciudadRepository.getById(ciudadId)).thenReturn(Result.success(
            Ciudad(ciudadId, "Manta", "Ecuador", Geometry("Point", emptyList()), 0.0, 0.0)
        ))
        
        val reporte = Reporte(
            id = "rep-1",
            usuario_id = "user-1",
            ciudad_id = ciudadId,
            ubicacion_id = "ubic-1",
            descripcion = "Desc",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-01",
            categoria = ReportType.BACHE,
            created_at = null,
            updated_at = null,
            barrio_id = "barrio-1"
        )
        whenever(reporteRepository.getReporteByCiudadId(eq(ciudadId), any(), any()))
            .thenReturn(Result.success(Pair(listOf(reporte), 1L)))

        viewModel.loadData()
        advanceUntilIdle()

        assertEquals("Manta", viewModel.state.value.ciudadNombre)
        assertEquals(1, viewModel.state.value.reportes.size)
        assertEquals("Bache", viewModel.state.value.reportes[0].categoria)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun testLoadData_NoCity() = runTest {
        whenever(authRepository.getCiudadId()).thenReturn(Result.failure(Exception("City not found")))

        viewModel.loadData()
        advanceUntilIdle()

        assertEquals("No se pudo determinar la ciudad", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun testLoadData_Error() = runTest {
        whenever(authRepository.getCiudadId()).thenThrow(RuntimeException("Network error"))

        viewModel.loadData()
        advanceUntilIdle()

        assertEquals("Network error", viewModel.state.value.error)
    }
}
