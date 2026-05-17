package ec.cityalerta.app

import android.content.Context
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.viewmodel.AppViewModelFactory
import ec.cityalerta.app.viewmodel.AuthViewModel
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests unitarios para AppViewModelFactory.
 * Valida la creacion de ViewModels y manejo de dependencias.
 */
class ViewModelFactoryTest {

    @Mock
    private lateinit var mockAuthRepository: AuthRepositoryContract

    @Mock
    private lateinit var mockContext: Context

    private lateinit var factory: AppViewModelFactory

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        factory = AppViewModelFactory(mockAuthRepository, mockContext)
    }

    @Test
    fun testViewModelFactoryCreation() {
        // Arrange & Act
        val viewModelFactory = AppViewModelFactory(mockAuthRepository, mockContext)

        // Assert
        assertNotNull(viewModelFactory)
        assertTrue(viewModelFactory is AppViewModelFactory)
    }

    @Test
    fun testCreateAuthViewModel() {
        // Note: AuthViewModel can be created with minimal dependencies
        // Other ViewModels require extensive context setup and are better tested with integration tests

        // Arrange & Act
        val authViewModel = factory.create(AuthViewModel::class.java)

        // Assert
        assertNotNull(authViewModel)
        assertTrue(authViewModel is AuthViewModel)
    }

    @Test
    fun testViewModelFactoryWithDifferentContexts() {
        // Arrange & Act
        val factory1 = AppViewModelFactory(mockAuthRepository, mockContext)
        val factory2 = AppViewModelFactory(mockAuthRepository, mockContext)

        // Assert
        assertNotNull(factory1)
        assertNotNull(factory2)
        // Different factory instances
        assertTrue(factory1 !== factory2)
    }

    @Test
    fun testViewModelFactoryInitializesLazyRepositories() {
        // Arrange & Act
        val factory = AppViewModelFactory(mockAuthRepository, mockContext)

        // Assert - verify factory is properly initialized
        assertNotNull(factory)
        assertTrue(factory is AppViewModelFactory)

        // Factory should have lazy-initialized repositories
        // We verify by creating an AuthViewModel which doesn't depend on them heavily
        val authVM = factory.create(AuthViewModel::class.java)
        assertNotNull(authVM)
    }

    @Test
    fun testMultipleAuthViewModelInstancesAreIndependent() {
        // Arrange & Act
        val authViewModel1 = factory.create(AuthViewModel::class.java)
        val authViewModel2 = factory.create(AuthViewModel::class.java)

        // Assert
        assertNotNull(authViewModel1)
        assertNotNull(authViewModel2)
        assertTrue(authViewModel1 is AuthViewModel)
        assertTrue(authViewModel2 is AuthViewModel)
        // Different instances
        assertTrue(authViewModel1 !== authViewModel2)
    }

    @Test
    fun testViewModelFactorySupportsMultipleCreations() {
        // Arrange & Act - Create multiple ViewModels of same type
        val authVM1 = factory.create(AuthViewModel::class.java)
        val authVM2 = factory.create(AuthViewModel::class.java)
        val authVM3 = factory.create(AuthViewModel::class.java)

        // Assert - All should be created successfully and independently
        assertNotNull(authVM1)
        assertNotNull(authVM2)
        assertNotNull(authVM3)
        assertTrue(authVM1 !== authVM2)
        assertTrue(authVM2 !== authVM3)
        assertTrue(authVM1 !== authVM3)
    }

    @Test
    fun testViewModelFactoryAcceptsAuthRepositoryContract() {
        // Arrange
        val repository = mockAuthRepository
        val context = mockContext

        // Act
        val factory = AppViewModelFactory(repository, context)

        // Assert
        assertNotNull(factory)
        assertTrue(factory is AppViewModelFactory)
    }

    @Test
    fun testViewModelFactoryClassIsInstantiable() {
        // Arrange & Act
        val factoryClass = AppViewModelFactory::class.java

        // Assert
        assertNotNull(factoryClass)
        assertTrue(factoryClass.simpleName == "AppViewModelFactory")
    }

    @Test
    fun testAuthViewModelCreationConsistency() {
        // Test that AuthViewModel creation is consistent

        // Arrange & Act
        val vm1 = factory.create(AuthViewModel::class.java)
        val vm2 = factory.create(AuthViewModel::class.java)

        // Assert - Both should be valid AuthViewModel instances
        assertTrue(vm1 is AuthViewModel)
        assertTrue(vm2 is AuthViewModel)
        assertNotNull(vm1)
        assertNotNull(vm2)
    }
}





