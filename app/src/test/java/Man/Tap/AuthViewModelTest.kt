package man.tap.viewmodel

import man.tap.model.repository.IAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    
    @Mock
    private lateinit var mockRepository: IAuthRepository

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        assertEquals("", viewModel.uiState.email)
        assertEquals("", viewModel.uiState.password)
        assertFalse(viewModel.uiState.isLoading)
        assertNull(viewModel.uiState.errorMessage)
    }

    @Test
    fun testOnEmailChange() {
        val testEmail = "test@example.com"
        viewModel.onEmailChange(testEmail)
        assertEquals(testEmail, viewModel.uiState.email)
    }

    @Test
    fun testOnPasswordChange() {
        val testPassword = "password123"
        viewModel.onPasswordChange(testPassword)
        assertEquals(testPassword, viewModel.uiState.password)
    }

    @Test
    fun testOnLoginClickWithEmptyFields() {
        var successCalled = false
        viewModel.onLoginClick(onSuccess = { successCalled = true })
        
        assertTrue(viewModel.uiState.errorMessage != null)
        assertFalse(successCalled)
    }

    @Test
    fun testOnLoginClickSuccess() = runTest {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")
        
        whenever(mockRepository.signIn("test@example.com", "password123"))
            .thenReturn(Result.success(Unit))
        
        var successCalled = false
        viewModel.onLoginClick(onSuccess = { successCalled = true })
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue(successCalled)
        assertNull(viewModel.uiState.errorMessage)
        assertFalse(viewModel.uiState.isLoading)
    }

    @Test
    fun testOnLoginClickFailure() = runTest {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")
        
        val error = Exception("Login failed")
        whenever(mockRepository.signIn("test@example.com", "password123"))
            .thenReturn(Result.failure(error))
        
        var successCalled = false
        viewModel.onLoginClick(onSuccess = { successCalled = true })
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertFalse(successCalled)
        assertNotNull(viewModel.uiState.errorMessage)
        assertFalse(viewModel.uiState.isLoading)
    }

    @Test
    fun testOnRegisterClickWithEmptyFields() {
        var successCalled = false
        viewModel.onRegisterClick(onSuccess = { successCalled = true })
        
        assertTrue(viewModel.uiState.errorMessage != null)
        assertFalse(successCalled)
    }

    @Test
    fun testOnRegisterClickSuccess() = runTest {
        viewModel.onEmailChange("newuser@example.com")
        viewModel.onPasswordChange("password123")
        
        whenever(mockRepository.signUp("newuser@example.com", "password123"))
            .thenReturn(Result.success(Unit))
        
        var successCalled = false
        viewModel.onRegisterClick(onSuccess = { successCalled = true })
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue(successCalled)
        assertNull(viewModel.uiState.errorMessage)
        assertFalse(viewModel.uiState.isLoading)
    }

    @Test
    fun testOnRegisterClickFailure() = runTest {
        viewModel.onEmailChange("newuser@example.com")
        viewModel.onPasswordChange("password123")
        
        val error = Exception("Registration failed")
        whenever(mockRepository.signUp("newuser@example.com", "password123"))
            .thenReturn(Result.failure(error))
        
        var successCalled = false
        viewModel.onRegisterClick(onSuccess = { successCalled = true })
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertFalse(successCalled)
        assertNotNull(viewModel.uiState.errorMessage)
        assertFalse(viewModel.uiState.isLoading)
    }
}
