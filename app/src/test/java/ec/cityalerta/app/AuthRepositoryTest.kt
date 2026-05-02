package ec.cityalerta.app

import kotlinx.coroutines.test.runTest
import ec.cityalerta.app.model.repository.AuthRepository
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryTest {

    @Test
    fun testSignUpReturnsFailureWhenClientCannotAuthenticate() = runTest {
        val repository = AuthRepository()

        val result = repository.signUp("test@example.com", "password123")

        assertTrue(result.isFailure)
    }

    @Test
    fun testSignInReturnsFailureWhenClientCannotAuthenticate() = runTest {
        val repository = AuthRepository()

        val result = repository.signIn("test@example.com", "password123")

        assertTrue(result.isFailure)
    }

    @Test
    fun testLogOutReturnsFailureWhenClientCannotAuthenticate() = runTest {
        val repository = AuthRepository()

        val result = repository.logOut()

        assertTrue(result.isFailure)
    }
}
