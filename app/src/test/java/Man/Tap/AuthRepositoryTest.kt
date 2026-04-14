package man.tap.model.repository

import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

class AuthRepositoryTest {

    @Test
    fun testAuthRepositoryImplementsInterface() {
        val isImplementing = AuthRepository::class.java.interfaces
            .any { it.name == IAuthRepository::class.java.name }
        
        assertTrue("AuthRepository debe implementar IAuthRepository", isImplementing)
    }

    @Test
    fun testAuthRepositoryHasSignUpMethod() {
        val hasMethod = AuthRepository::class.java.declaredMethods
            .any { it.name == "signUp" }
        
        assertTrue("AuthRepository debe tener el método signUp", hasMethod)
    }

    @Test
    fun testAuthRepositoryHasSignInMethod() {
        val hasMethod = AuthRepository::class.java.declaredMethods
            .any { it.name == "signIn" }
        
        assertTrue("AuthRepository debe tener el método signIn", hasMethod)
    }

    @Test
    fun testAuthRepositoryHasLogOutMethod() {
        val hasMethod = AuthRepository::class.java.declaredMethods
            .any { it.name == "logOut" }
        
        assertTrue("AuthRepository debe tener el método logOut", hasMethod)
    }

    @Test
    fun testSignUpMethodReturnsResult() {
        val signUpMethod = AuthRepository::class.java.declaredMethods
            .find { it.name == "signUp" }
        
        assertNotNull("Método signUp no encontrado", signUpMethod)
        val returnType = signUpMethod?.returnType?.simpleName
        assertTrue("signUp debe retornar Result", returnType?.contains("Result") ?: false)
    }

    @Test
    fun testSignInMethodReturnsResult() {
        val signInMethod = AuthRepository::class.java.declaredMethods
            .find { it.name == "signIn" }
        
        assertNotNull("Método signIn no encontrado", signInMethod)
        val returnType = signInMethod?.returnType?.simpleName
        assertTrue("signIn debe retornar Result", returnType?.contains("Result") ?: false)
    }

    @Test
    fun testLogOutMethodReturnsResult() {
        val logOutMethod = AuthRepository::class.java.declaredMethods
            .find { it.name == "logOut" }
        
        assertNotNull("Método logOut no encontrado", logOutMethod)
        val returnType = logOutMethod?.returnType?.simpleName
        assertTrue("logOut debe retornar Result", returnType?.contains("Result") ?: false)
    }
}
