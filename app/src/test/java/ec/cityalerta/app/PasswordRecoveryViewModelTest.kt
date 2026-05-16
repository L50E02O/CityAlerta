package ec.cityalerta.app

import ec.cityalerta.app.model.local.PasswordRecoveryPreferences
import ec.cityalerta.app.model.repository.interfaces.IAuthRepository
import ec.cityalerta.app.viewmodel.PasswordRecoveryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class PasswordRecoveryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockRepository: IAuthRepository

    @Mock
    private lateinit var mockPreferences: PasswordRecoveryPreferences

    private lateinit var viewModel: PasswordRecoveryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockitoAnnotations.openMocks(this)
        viewModel = PasswordRecoveryViewModel(mockRepository, mockPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun resetPassword_requiresCompleteForm() = runTest {
        viewModel.onEmailChange("test@example.com")
        viewModel.onNewPasswordChange("12345678")

        viewModel.resetPassword { }
        advanceUntilIdle()

        assertEquals("Completa la nueva contrasena", viewModel.uiState.errorMessage)
    }

    @Test
    fun resetPassword_updatesPasswordByEmail() = runTest {
        whenever(mockRepository.resetPasswordByEmail("test@example.com", "Password123"))
            .thenReturn(Result.success(Unit))

        viewModel.onEmailChange("test@example.com")
        viewModel.onNewPasswordChange("Password123")
        viewModel.onConfirmPasswordChange("Password123")

        var successCalled = false
        viewModel.resetPassword { successCalled = true }
        advanceUntilIdle()

        verify(mockRepository).resetPasswordByEmail("test@example.com", "Password123")
        assertTrue(successCalled)
        assertEquals("Contrasena actualizada correctamente", viewModel.uiState.successMessage)
    }
}
