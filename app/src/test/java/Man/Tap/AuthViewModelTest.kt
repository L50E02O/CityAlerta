package man.tap.viewmodel

import man.tap.model.repository.IAuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class AuthViewModelTest {
    @Mock
    private lateinit var mockRepository: IAuthRepository

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = AuthViewModel(mockRepository)
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
        
        assertFalse(successCalled)
        assertNotNull(viewModel.uiState.errorMessage)
        assertFalse(viewModel.uiState.isLoading)
    }
}
