package ec.cityalerta.app

import ec.cityalerta.app.model.repository.BarrioRepository
import ec.cityalerta.app.model.repository.CiudadRepository
import ec.cityalerta.app.model.repository.PerfilImagenRepository
import ec.cityalerta.app.model.repository.PerfilRepository
import ec.cityalerta.app.model.repository.PerfilResumenRepository
import ec.cityalerta.app.model.repository.PerfilStorageRepository
import ec.cityalerta.app.model.repository.ReporteImagenRepository
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteStorageRepository
import ec.cityalerta.app.model.repository.ReporteUbicacionRepository
import ec.cityalerta.app.testdoubles.FakeAuthRepository
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
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelSettingsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository()

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
    private lateinit var barrioRepository: BarrioRepository

    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = ProfileViewModel(
            authRepository = authRepository,
            perfilRepository = perfilRepository,
            perfilResumenRepository = perfilResumenRepository,
            ciudadRepository = ciudadRepository,
            reporteRepository = reporteRepository,
            reporteImagenRepository = reporteImagenRepository,
            perfilImagenRepository = perfilImagenRepository,
            reporteUbicacionRepository = reporteUbicacionRepository,
            reporteStorageRepository = reporteStorageRepository,
            perfilStorageRepository = perfilStorageRepository,
            barrioRepository = barrioRepository
        )
    }

    @Test
    fun updateEmail_sameEmail_setsErrorMessage() = runTest {
        val result = viewModel.updateEmail(
            newEmail = "test@example.com",
            currentEmail = "test@example.com",
            invalidEmailMessage = "invalid",
            sameEmailMessage = "same",
            successMessage = "ok",
            errorMessage = "error"
        )

        advanceUntilIdle()

        assertEquals(ProfileViewModel.EmailUpdateValidation.SAME, result)
        assertEquals("same", viewModel.state.value.errorMessage)
    }

    @Test
    fun updateEmail_invalidEmail_returnsInvalid() = runTest {
        val result = viewModel.updateEmail(
            newEmail = "not-an-email",
            currentEmail = "test@example.com",
            invalidEmailMessage = "invalid",
            sameEmailMessage = "same",
            successMessage = "ok",
            errorMessage = "error"
        )

        assertEquals(ProfileViewModel.EmailUpdateValidation.INVALID, result)
    }

    @Test
    fun updateEmail_success_setsInfoMessage() = runTest {
        authRepository.updateEmailResult = Result.success(Unit)

        val result = viewModel.updateEmail(
            newEmail = "nuevo@example.com",
            currentEmail = "test@example.com",
            invalidEmailMessage = "invalid",
            sameEmailMessage = "same",
            successMessage = "Correo actualizado",
            errorMessage = "error"
        )

        advanceUntilIdle()

        assertEquals(ProfileViewModel.EmailUpdateValidation.OK, result)
        assertEquals("nuevo@example.com", authRepository.lastUpdateEmail)
        assertEquals("Correo actualizado", viewModel.state.value.settingsInfoMessage)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun deleteAccount_success_invokesCallback() = runTest {
        var deleted = false

        viewModel.deleteAccount(
            successMessage = "ok",
            errorMessage = "error"
        ) {
            deleted = true
        }

        advanceUntilIdle()

        assertTrue(deleted)
        assertEquals(1, authRepository.deleteAccountCalls)
        assertFalse(viewModel.state.value.isDeletingAccount)
    }

    @Test
    fun clearSettingsMessages_clearsMessages() = runTest {
        viewModel.updateEmail(
            newEmail = "test@example.com",
            currentEmail = "other@example.com",
            invalidEmailMessage = "invalid",
            sameEmailMessage = "same",
            successMessage = "ok",
            errorMessage = "error"
        )
        advanceUntilIdle()

        viewModel.clearSettingsMessages()

        assertNull(viewModel.state.value.settingsInfoMessage)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun logOut_success_resetsState() = runTest {
        viewModel.logOut {}

        advanceUntilIdle()

        assertEquals("", viewModel.state.value.fullName)
    }

    @Test
    fun deleteAccount_failure_setsErrorMessage() = runTest {
        authRepository.deleteAccountResult = Result.failure(Exception("No autorizado"))

        viewModel.deleteAccount(
            successMessage = "ok",
            errorMessage = "No se pudo eliminar"
        ) {}

        advanceUntilIdle()

        assertEquals("No autorizado", viewModel.state.value.errorMessage)
        assertFalse(viewModel.state.value.isDeletingAccount)
    }
}
