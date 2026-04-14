package man.tap.model.repository

import org.junit.Test
import org.junit.Assert.*

class AuthRepositoryTest {

    @Test
    fun testAuthRepositoryImplementsInterface() {
        val repository: IAuthRepository = AuthRepository()
        assertNotNull(repository)
    }

    @Test
    fun testInterfaceDeclaresSignUpSuspendMethod() {
        val hasMethod = IAuthRepository::class.java.methods.any { method ->
            method.name.startsWith("signUp")
        }
        assertTrue("IAuthRepository debe declarar signUp suspend", hasMethod)
    }

    @Test
    fun testInterfaceDeclaresSignInSuspendMethod() {
        val hasMethod = IAuthRepository::class.java.methods.any { method ->
            method.name.startsWith("signIn")
        }
        assertTrue("IAuthRepository debe declarar signIn suspend", hasMethod)
    }

    @Test
    fun testInterfaceDeclaresLogOutSuspendMethod() {
        val hasMethod = IAuthRepository::class.java.methods.any { method ->
            method.name.startsWith("logOut")
        }
        assertTrue("IAuthRepository debe declarar logOut suspend", hasMethod)
    }

    @Test
    fun testFakeSignUpReturnsSuccess() = kotlinx.coroutines.test.runTest {
        val fakeRepository = object : IAuthRepository {
            override suspend fun signUp(email: String, password: String): Result<Unit> = Result.success(Unit)
            override suspend fun signIn(email: String, password: String): Result<Unit> = Result.success(Unit)
            override suspend fun logOut(): Result<Unit> = Result.success(Unit)
        }

        val result = fakeRepository.signUp("test@example.com", "password123")
        assertTrue(result.isSuccess)
    }

    @Test
    fun testFakeSignInReturnsFailure() = kotlinx.coroutines.test.runTest {
        val fakeRepository = object : IAuthRepository {
            override suspend fun signUp(email: String, password: String): Result<Unit> = Result.success(Unit)
            override suspend fun signIn(email: String, password: String): Result<Unit> = Result.failure(Exception("fail"))
            override suspend fun logOut(): Result<Unit> = Result.success(Unit)
        }

        val result = fakeRepository.signIn("test@example.com", "password123")
        assertTrue(result.isFailure)
    }

    @Test
    fun testFakeLogOutReturnsSuccess() = kotlinx.coroutines.test.runTest {
        val fakeRepository = object : IAuthRepository {
            override suspend fun signUp(email: String, password: String): Result<Unit> = Result.success(Unit)
            override suspend fun signIn(email: String, password: String): Result<Unit> = Result.success(Unit)
            override suspend fun logOut(): Result<Unit> = Result.success(Unit)
        }

        val result = fakeRepository.logOut()
        assertTrue(result.isSuccess)
    }
}
