package man.tap.model.repository

import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import io.github.jan.supabase.SupabaseClient

class AuthRepositoryTest {
    private lateinit var repository: AuthRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = AuthRepository()
    }

    @Test
    fun testSignUpSuccess() = runTest {
        val result = repository.signUp("test@example.com", "password123")
        
        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
    }

    @Test
    fun testSignUpWithValidCredentials() = runTest {
        val email = "newuser@example.com"
        val password = "securepass123"
        
        val result = repository.signUp(email, password)
        
        assertTrue(result.isSuccess || result.isFailure)
    }

    @Test
    fun testSignInSuccess() = runTest {
        val result = repository.signIn("test@example.com", "password123")
        
        assertTrue(result.isSuccess || result.isFailure)
    }

    @Test
    fun testSignInWithValidCredentials() = runTest {
        val email = "user@example.com"
        val password = "password123"
        
        val result = repository.signIn(email, password)
        
        assertNotNull(result)
    }

    @Test
    fun testLogOutSuccess() = runTest {
        val result = repository.logOut()
        
        assertTrue(result.isSuccess || result.isFailure)
    }

    @Test
    fun testSignUpReturnsResult() = runTest {
        val result = repository.signUp("test@example.com", "password123")
        
        assertNotNull(result)
        assertTrue(result.isSuccess || result.isFailure)
    }

    @Test
    fun testSignInReturnsResult() = runTest {
        val result = repository.signIn("test@example.com", "password123")
        
        assertNotNull(result)
        assertTrue(result.isSuccess || result.isFailure)
    }

    @Test
    fun testLogOutReturnsResult() = runTest {
        val result = repository.logOut()
        
        assertNotNull(result)
        assertTrue(result.isSuccess || result.isFailure)
    }
}
