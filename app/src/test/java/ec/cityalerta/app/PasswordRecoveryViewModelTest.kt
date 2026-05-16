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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
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
        whenever(mockPreferences.canRecover(any())).thenReturn(true)
        viewModel = PasswordRecoveryViewModel(mockRepository, mockPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun sendRecoveryEmail_blocksWhenDailyLimitReached() = runTest {
        whenever(mockPreferences.canRecover("test@example.com")).thenReturn(false)
        whenever(mockPreferences.millisUntilNextRecovery("test@example.com")).thenReturn(3_600_000L)

        viewModel.onEmailChange("test@example.com")
        viewModel.sendRecoveryEmail()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.errorMessage?.contains("una vez al dia") == true)
        verify(mockRepository, never()).sendPasswordRecovery(any())
    }

    @Test
    fun updatePassword_requiresStepOneBeforeReset() = runTest {
        viewModel.updatePassword { }
        advanceUntilIdle()

        assertEquals(
            "Primero envia el enlace de recuperacion a tu correo",
            viewModel.uiState.errorMessage
        )
        assertFalse(viewModel.uiState.isStepTwoUnlocked)
    }
}
