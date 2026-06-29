package ec.cityalerta.app

import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.data.contracts.geocoding.GeocodingRepositoryContract
import ec.cityalerta.app.model.data.contracts.location.LocationProviderContract
import ec.cityalerta.app.model.data.contracts.map.MapRepositoryContract
import ec.cityalerta.app.model.data.geoJson.Geometry
import ec.cityalerta.app.model.data.contracts.moderation.ImageModerationContract
import ec.cityalerta.app.model.data.location.UserLocation
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacion
import ec.cityalerta.app.model.repository.BarrioRepository
import ec.cityalerta.app.model.repository.ReporteImagenRepository
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteStorageRepository
import ec.cityalerta.app.model.repository.ReporteUbicacionRepository
import ec.cityalerta.app.util.MainDispatcherRule
import ec.cityalerta.app.viewmodel.ReporteViewModel
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReporteViewModelTest {

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
    private lateinit var locationProvider: LocationProviderContract
    @Mock
    private lateinit var authRepository: AuthRepositoryContract
    @Mock
    private lateinit var mapRepository: MapRepositoryContract
    @Mock
    private lateinit var barrioRepository: BarrioRepository
    @Mock
    private lateinit var geocodingRepository: GeocodingRepositoryContract
    @Mock
    private lateinit var imageModerationRepository: ImageModerationContract

    private lateinit var viewModel: ReporteViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = ReporteViewModel(
            reporteRepository,
            reporteImagenRepository,
            reporteUbicacionRepository,
            reporteStorageRepository,
            locationProvider,
            authRepository,
            mapRepository,
            barrioRepository,
            geocodingRepository,
            imageModerationRepository
        )
    }

    @Test
    fun testInitialState() {
        assertEquals("", viewModel.uiState.value.descripcion)
        assertNull(viewModel.uiState.value.categoria)
        assertNull(viewModel.uiState.value.imagenURL)
        assertNull(viewModel.uiState.value.currentLocation)
        assertNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun testOnDescriptionChange() {
        val desc = "Test description"
        viewModel.onDescriptionChange(desc)
        assertEquals(desc, viewModel.uiState.value.descripcion)
    }

    @Test
    fun testOnCategoriaChange() {
        val category = ReportType.BACHE
        viewModel.onCategoriaChange(category)
        assertEquals(category, viewModel.uiState.value.categoria)
    }

    @Test
    fun testSetImagen() {
        val uri = "content://media/external/images/media/1"
        viewModel.setImagen(uri)
        assertEquals(uri, viewModel.uiState.value.imagenURL)
    }

    @Test
    fun testSetUbicacion() {
        val lat = -1.0
        val lng = -80.0
        viewModel.setUbicacion(lat, lng)
        assertEquals(lat, viewModel.uiState.value.currentLocation?.latitude)
        assertEquals(lng, viewModel.uiState.value.currentLocation?.longitude)
    }

    @Test
    fun testRequestCurrentLocationSuccess() = runTest {
        val location = UserLocation(-1.0, -80.0)
        whenever(locationProvider.getCurrentLocation()).thenReturn(Result.success(location))

        viewModel.requestCurrentLocation()
        advanceUntilIdle()

        assertEquals(location, viewModel.uiState.value.currentLocation)
    }

    @Test
    fun testRequestCurrentLocationFailure() = runTest {
        whenever(locationProvider.getCurrentLocation()).thenReturn(Result.failure(Exception("GPS error")))

        viewModel.requestCurrentLocation()
        advanceUntilIdle()

        assertEquals("GPS error", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun testSendReportValidationFailure_MissingFields() = runTest {
        whenever(authRepository.getUserId()).thenReturn(Result.success("user-123"))
        whenever(authRepository.getCiudadId()).thenReturn(Result.success("city-123"))

        viewModel.sendReport()
        advanceUntilIdle()

        assertEquals("Categoría no seleccionada", viewModel.uiState.value.errorMessage)

        viewModel.onCategoriaChange(ReportType.BACHE)
        viewModel.sendReport()
        advanceUntilIdle()
        assertEquals("Falta descripción", viewModel.uiState.value.errorMessage)

        viewModel.onDescriptionChange("Bache grande")
        viewModel.sendReport()
        advanceUntilIdle()
        assertEquals("Falta imagen", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun testSendReportLocationOutsideCity() = runTest {
        val ciudadId = "city-123"
        whenever(authRepository.getUserId()).thenReturn(Result.success("user-123"))
        whenever(authRepository.getCiudadId()).thenReturn(Result.success(ciudadId))
        whenever(imageModerationRepository.analyzeContent(any(), any())).thenReturn(Result.success(ec.cityalerta.app.model.data.contracts.moderation.ModerationResult(isSafe = true)))

        // Outside Manta polygon (simple square for test)
        val geoJson = Geometry(
            type = "Polygon",
            coordinates = listOf(listOf(listOf(-81.0, -2.0), listOf(-81.0, 1.0), listOf(-78.0, 1.0), listOf(-78.0, -2.0), listOf(-81.0, -2.0)))
        )
        val ciudad = Ciudad(ciudadId, "Manta", "Ecuador", geoJson, 0.0, 0.0)
        whenever(mapRepository.getCiudadById(ciudadId)).thenReturn(ciudad)

        viewModel.onCategoriaChange(ReportType.BACHE)
        viewModel.onDescriptionChange("Bache grande")
        viewModel.setImagen("uri")
        viewModel.setImagenData(byteArrayOf(1, 2, 3))
        viewModel.setUbicacion(10.0, 10.0) // Way outside

        viewModel.sendReport()
        advanceUntilIdle()

        assertEquals("Ubicación fuera de los límites permitidos de la ciudad", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun testSendReportSuccess() = runTest {
        val userId = "user-123"
        val ciudadId = "city-123"
        val lat = -1.0
        val lng = -80.0
        val bytes = byteArrayOf(1, 2, 3)

        whenever(authRepository.getUserId()).thenReturn(Result.success(userId))
        whenever(authRepository.getCiudadId()).thenReturn(Result.success(ciudadId))
        whenever(imageModerationRepository.analyzeContent(any(), any())).thenReturn(Result.success(ec.cityalerta.app.model.data.contracts.moderation.ModerationResult(isSafe = true)))

        val geoJson = Geometry(
            type = "Polygon",
            coordinates = listOf(listOf(listOf(-81.0, -2.0), listOf(-81.0, 1.0), listOf(-78.0, 1.0), listOf(-78.0, -2.0), listOf(-81.0, -2.0)))
        )
        val ciudad = Ciudad(ciudadId, "Manta", "Ecuador", geoJson, -1.0, -80.0)
        whenever(mapRepository.getCiudadById(ciudadId)).thenReturn(ciudad)
        
        whenever(barrioRepository.getByPoint(eq(ciudadId), any(), any())).thenReturn(Result.success(
            ec.cityalerta.app.model.data.barrio.Barrio("barrio-1", "Centro", ciudadId, Geometry("Polygon", emptyList()))
        ))
        
        whenever(geocodingRepository.reverseGeocode(any(), any())).thenReturn(Result.success("Calle Falsa 123"))
        
        whenever(reporteUbicacionRepository.create(any())).thenReturn(Result.success(
            ReporteUbicacion("ubic-1", lat, lng, "Calle Falsa 123")
        ))
        
        whenever(reporteStorageRepository.uploadReportImage(any(), any())).thenReturn(Result.success("path/to/image.jpg"))
        
        whenever(reporteRepository.create(any())).thenReturn(Result.success(
            Reporte(
                id = "rep-1",
                usuario_id = userId,
                ciudad_id = ciudadId,
                ubicacion_id = "ubic-1",
                descripcion = "Bache grande",
                estado = ReporteEstado.PENDIENTE,
                fecha_reporte = "2024-01-01",
                categoria = ReportType.BACHE,
                created_at = null,
                updated_at = null,
                barrio_id = "barrio-1"
            )
        ))

        viewModel.onCategoriaChange(ReportType.BACHE)
        viewModel.onDescriptionChange("Bache grande")
        viewModel.setImagen("uri")
        viewModel.setImagenData(bytes)
        viewModel.setUbicacion(lat, lng)

        var successCalled = false
        viewModel.sendReport(onSuccess = { successCalled = true })
        advanceUntilIdle()

        assertTrue(successCalled)
        assertEquals("", viewModel.uiState.value.descripcion)
        assertNull(viewModel.uiState.value.categoria)
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
