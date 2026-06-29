package ec.cityalerta.app

import ec.cityalerta.app.model.data.barrio.Barrio
import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.data.geoJson.Geometry
import ec.cityalerta.app.model.data.perfil.Perfil
import ec.cityalerta.app.model.data.perfil.PerfilResumen
import ec.cityalerta.app.model.data.perfilimagen.PerfilImagen
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacion
import ec.cityalerta.app.model.repository.*
import ec.cityalerta.app.util.MainDispatcherRule
import ec.cityalerta.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var authRepository: AuthRepositoryContract
    @Mock
    private lateinit var perfilRepository: PerfilRepository
    @Mock
    private lateinit var perfilResumenRepository: PerfilResumenRepository
    @Mock
    private lateinit var ciudadRepository: CiudadRepository
    @Mock
    private lateinit var reporteRepository: ReporteRepository
    @Mock
    private lateinit var reporteImagenRepository: ReporteImagenRepository
    @Mock
    private lateinit var perfilImagenRepository: PerfilImagenRepository
    @Mock
    private lateinit var reporteUbicacionRepository: ReporteUbicacionRepository
    @Mock
    private lateinit var reporteStorageRepository: ReporteStorageRepository
    @Mock
    private lateinit var perfilStorageRepository: PerfilStorageRepository
    @Mock
    private lateinit var perfilLocalRepository: PerfilLocalRepository
    @Mock
    private lateinit var barrioRepository: BarrioRepository

    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = ProfileViewModel(
            authRepository,
            perfilRepository,
            perfilResumenRepository,
            perfilLocalRepository,
            ciudadRepository,
            reporteRepository,
            reporteImagenRepository,
            perfilImagenRepository,
            reporteUbicacionRepository,
            reporteStorageRepository,
            perfilStorageRepository,
            barrioRepository
        )
    }

    @Test
    fun testInitialState() {
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("", state.fullName)
        assertEquals("", state.initials)
        assertNull(state.profileImageUrl)
        assertTrue(state.myReports.isEmpty())
    }

    @Test
    fun testLoadSummary_Success() = runTest {
        val userId = "user-123"
        val resumen = PerfilResumen(userId, "Juan Perez", "user", true, "city-1", 10, 5)
        
        whenever(authRepository.getUserId()).thenReturn(Result.success(userId))
        whenever(perfilResumenRepository.getCurrentResumen()).thenReturn(Result.success(resumen))
        whenever(ciudadRepository.getById("city-1")).thenReturn(Result.success(
            Ciudad("city-1", "Manta", "Ecuador", Geometry("Point", emptyList()), 0.0, 0.0)
        ))
        whenever(perfilImagenRepository.getImagenByPerfilId(userId)).thenReturn(Result.success(null))
        whenever(authRepository.getUserEmail()).thenReturn(Result.success("juan@example.com"))

        viewModel.loadSummary()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Juan Perez", state.fullName)
        assertEquals("JP", state.initials)
        assertEquals("Manta", state.cityName)
        assertEquals("juan@example.com", state.userEmail)
        assertEquals(10, state.totalReports)
        assertEquals(5, state.resolvedReports)
    }

    @Test
    fun testUpdateFullName_Success() = runTest {
        val userId = "user-123"
        val perfil = Perfil(userId, "Nuevo Nombre", "user", true, null, null, "city-1")
        whenever(authRepository.getUserId()).thenReturn(Result.success(userId))
        whenever(perfilRepository.update(any(), eq(userId))).thenReturn(Result.success(perfil))
        
        // For reload
        val resumen = PerfilResumen(userId, "Nuevo Nombre", "user", true, "city-1", 0, 0)
        whenever(perfilResumenRepository.getCurrentResumen()).thenReturn(Result.success(resumen))
        whenever(ciudadRepository.getById(any())).thenReturn(Result.success(null))
        whenever(perfilImagenRepository.getImagenByPerfilId(any())).thenReturn(Result.success(null))
        whenever(authRepository.getUserEmail()).thenReturn(Result.success(""))

        viewModel.updateFullName("Nuevo Nombre", "Empty", "Success", "Error")
        advanceUntilIdle()

        assertEquals("Success", viewModel.state.value.settingsInfoMessage)
        assertEquals("Nuevo Nombre", viewModel.state.value.fullName)
    }

    @Test
    fun testUpdateFullName_Empty() {
        viewModel.updateFullName("  ", "Empty Message", "Success", "Error")
        assertEquals("Empty Message", viewModel.state.value.errorMessage)
    }

    @Test
    fun testLogOut_Success() = runTest {
        whenever(authRepository.logOut()).thenReturn(Result.success(Unit))
        var successCalled = false

        viewModel.logOut(onSuccess = { successCalled = true })
        advanceUntilIdle()

        assertTrue(successCalled)
        assertEquals("", viewModel.state.value.fullName) // State reset
    }

    @Test
    fun testLoadDashboard_Success() = runTest {
        val userId = "user-1"
        val resumen = PerfilResumen(userId, "Juan", "user", true, "city-1", 1, 0)
        val createdAt = Instant.now().toString()
        val reporte = Reporte(
            id = "rep-1",
            usuario_id = userId,
            ciudad_id = "city-1",
            ubicacion_id = "ubic-1",
            descripcion = "Desc",
            estado = ReporteEstado.PENDIENTE,
            fecha_reporte = "2024-01-01",
            categoria = ReportType.BACHE,
            created_at = createdAt,
            barrio_id = "barrio-1"
        )

        whenever(authRepository.getUserId()).thenReturn(Result.success(userId))
        whenever(perfilResumenRepository.getCurrentResumen()).thenReturn(Result.success(resumen))
        whenever(ciudadRepository.getById("city-1")).thenReturn(Result.success(Ciudad("city-1", "Manta", "Ecuador", Geometry("Point", emptyList()), 0.0, 0.0)))
        whenever(reporteRepository.getReporteByUsuarioId(userId)).thenReturn(Result.success(listOf(reporte)))
        whenever(perfilImagenRepository.getImagenByPerfilId(userId)).thenReturn(Result.success(null))
        whenever(reporteImagenRepository.getFirstImagenByReporteId("rep-1")).thenReturn(Result.success(null))
        whenever(reporteUbicacionRepository.getById("ubic-1")).thenReturn(Result.success(ReporteUbicacion("ubic-1", 0.0, 0.0, "Calle 1")))
        whenever(barrioRepository.getById("barrio-1")).thenReturn(Result.success(Barrio("barrio-1", "city-1", "Centro", Geometry("Point", emptyList()))))

        viewModel.loadDashboard()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1, state.myReports.size)
        assertEquals("Desc", state.myReports[0].descripcion)
        assertEquals("Centro", state.myReports[0].barrio)
    }
}
