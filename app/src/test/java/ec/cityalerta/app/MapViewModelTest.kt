package ec.cityalerta.app

import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.testdoubles.NoOpRiskZoneDetector
import ec.cityalerta.app.viewmodel.MapUiState
import ec.cityalerta.app.viewmodel.MapViewModel
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.data.contracts.map.MapRepositoryContract
import ec.cityalerta.app.model.repository.BarrioRepository
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteUbicacionRepository
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests unitarios para MapViewModel.
 * Valida el estado del mapa y las operaciones de interaccion.
 */
class MapViewModelTest {

    @Mock
    private lateinit var mockMapRepository: MapRepositoryContract

    @Mock
    private lateinit var mockReporteRepository: ReporteRepository

    @Mock
    private lateinit var mockUbicacionRepository: ReporteUbicacionRepository

    @Mock
    private lateinit var mockAuthRepository: AuthRepositoryContract

    @Mock
    private lateinit var mockBarrioRepository: BarrioRepository

    private lateinit var viewModel: MapViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = MapViewModel(
            mockMapRepository,
            mockReporteRepository,
            mockUbicacionRepository,
            mockAuthRepository,
            mockBarrioRepository,
            NoOpRiskZoneDetector
        )
    }

    @Test
    fun testMapUiStateInitial() {
        // Arrange & Act
        val state = viewModel.uiState

        // Assert
        assertNotNull(state)
        assertNull(state.ciudad)
        assertTrue(state.reportMarkers.isEmpty())
        assertTrue(state.reports.isEmpty())
        assertNull(state.selectedCategory)
        assertNull(state.selectedReport)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun testMapUiStateDataClass() {
        // Arrange & Act
        val state = MapUiState(
            ciudad = null,
            reportMarkers = emptyList(),
            reports = emptyList(),
            selectedCategory = null,
            selectedReport = null,
            categories = ReportType.entries,
            cameraZoom = 15f,
            errorMessage = null,
            isLoading = false
        )

        // Assert
        assertNotNull(state)
        assertEquals(15f, state.cameraZoom)
        assertTrue(state.categories.isNotEmpty())
    }

    @Test
    fun testMapViewModelCreation() {
        // Arrange & Act
        val vm = MapViewModel(
            mockMapRepository,
            mockReporteRepository,
            mockUbicacionRepository,
            mockAuthRepository,
            mockBarrioRepository,
            NoOpRiskZoneDetector
        )

        // Assert
        assertNotNull(vm)
        assertTrue(vm is MapViewModel)
    }

    @Test
    fun testUpdateCameraZoom() {
        // Arrange
        val initialZoom = viewModel.uiState.value.cameraZoom

        // Act
        viewModel.updateCameraZoom(20f)

        // Assert
        assertEquals(20f, viewModel.uiState.value.cameraZoom)
        assertNotNull(initialZoom)
    }

    @Test
    fun testOnCategorySelected() {
        // Arrange
        val category = ReportType.BACHE

        // Act
        viewModel.onCategorySelected(category)

        // Assert
        assertEquals(category, viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun testOnCategorySelectedToggle() {
        // Arrange
        val category = ReportType.BACHE

        // Act
        viewModel.onCategorySelected(category)
        val selectedAfterFirst = viewModel.uiState.value.selectedCategory

        // Act - Select again to deselect
        viewModel.onCategorySelected(category)
        val selectedAfterSecond = viewModel.uiState.value.selectedCategory

        // Assert
        assertEquals(category, selectedAfterFirst)
        assertNull(selectedAfterSecond)
    }

    @Test
    fun testDifferentCategoriesToggle() {
        // Arrange
        val category1 = ReportType.BACHE
        val category2 = ReportType.ZONA_DE_RIESGO

        // Act
        viewModel.onCategorySelected(category1)
        val selected1 = viewModel.uiState.value.selectedCategory

        viewModel.onCategorySelected(category2)
        val selected2 = viewModel.uiState.value.selectedCategory

        // Assert
        assertEquals(category1, selected1)
        assertEquals(category2, selected2)
    }

    @Test
    fun testOnReportClicked() {
        // Arrange
        val reportId = "report-1"

        // Act
        viewModel.onReportClicked(reportId)

        // Assert
        // Should not crash, report might be null if reports list is empty
        assertNull(viewModel.uiState.value.selectedReport)
    }

    @Test
    fun testOnDismissReport() {
        // Arrange & Act
        viewModel.onDismissReport()

        // Assert
        assertNull(viewModel.uiState.value.selectedReport)
    }

    @Test
    fun testMapZoomLevels() {
        // Arrange
        val zoomLevels = listOf(5f, 10f, 15f, 20f)

        // Act & Assert
        zoomLevels.forEach { zoom ->
            viewModel.updateCameraZoom(zoom)
            assertEquals(zoom, viewModel.uiState.value.cameraZoom)
        }
    }

    @Test
    fun testReportTypeCategories() {
        // Arrange & Act
        val categories = viewModel.uiState.value.categories

        // Assert
        assertEquals(ReportType.entries, categories)
        ReportType.entries.forEach { tipo ->
            assertTrue(categories.contains(tipo))
        }
    }

    @Test
    fun testMapUiStateErrorHandling() {
        // Arrange
        val errorState = MapUiState(
            errorMessage = "Error de prueba"
        )

        // Act & Assert
        assertNotNull(errorState.errorMessage)
        assertEquals("Error de prueba", errorState.errorMessage)
    }

    @Test
    fun testMapUiStateLoadingState() {
        // Arrange
        val loadingState = MapUiState(
            isLoading = true,
            errorMessage = null
        )

        // Assert
        assertTrue(loadingState.isLoading)
        assertNull(loadingState.errorMessage)
    }

    @Test
    fun testMapViewModelMultipleOperations() {
        // Arrange & Act
        viewModel.updateCameraZoom(18f)
        viewModel.onCategorySelected(ReportType.BACHE)
        viewModel.onDismissReport()

        // Assert
        assertEquals(18f, viewModel.uiState.value.cameraZoom)
        assertEquals(ReportType.BACHE, viewModel.uiState.value.selectedCategory)
        assertNull(viewModel.uiState.value.selectedReport)
    }

    @Test
    fun testMapDefaultCameraZoom() {
        // Arrange & Act
        val state = MapUiState()

        // Assert
        assertEquals(15f, state.cameraZoom)
    }

    @Test
    fun testCategorySelectionIndependence() {
        // Arrange
        val cat1 = ReportType.BACHE
        val cat2 = ReportType.ZONA_DE_RIESGO

        // Act
        viewModel.onCategorySelected(cat1)
        val select1 = viewModel.uiState.value.selectedCategory
        viewModel.onCategorySelected(cat2)
        val select2 = viewModel.uiState.value.selectedCategory

        // Assert
        assertEquals(cat1, select1)
        assertEquals(cat2, select2)
    }

    @Test
    fun testMapUiStateImmutability() {
        // Arrange
        val originalState = viewModel.uiState
        val zoom1 = originalState.cameraZoom

        // Act
        viewModel.updateCameraZoom(20f)
        val zoom2 = originalState.cameraZoom

        // Assert
        assertEquals(15f, zoom1)
        assertEquals(15f, zoom2)
        // State should be replaced, not mutated
        assertEquals(20f, viewModel.uiState.value.cameraZoom)
    }
}



