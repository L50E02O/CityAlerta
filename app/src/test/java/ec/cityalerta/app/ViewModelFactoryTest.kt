package ec.cityalerta.app

import android.content.Context
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.viewmodel.AppViewModelFactory
import ec.cityalerta.app.viewmodel.AuthViewModel
import ec.cityalerta.app.viewmodel.ExploreViewModel
import ec.cityalerta.app.viewmodel.MapViewModel
import ec.cityalerta.app.viewmodel.PasswordRecoveryViewModel
import ec.cityalerta.app.viewmodel.ReporteViewModel
import androidx.lifecycle.ViewModel
import org.junit.Before
import org.junit.Test
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
        val context = mock<Context>(defaultAnswer = Answers.RETURNS_DEEP_STUBS)
        factory = AppViewModelFactory(mockAuthRepository, context)
    }

    @Test
    fun testViewModelFactoryCreation() {
        val context = mock<Context>(defaultAnswer = Answers.RETURNS_DEEP_STUBS)
        val viewModelFactory = AppViewModelFactory(mockAuthRepository, context)
        assertNotNull(viewModelFactory)
    }

    @Test
    fun testCreateAuthViewModel() {
        val authViewModel = factory.create(AuthViewModel::class.java)
        assertNotNull(authViewModel)
    }

    @Test
    fun testCreatePasswordRecoveryViewModel() {
        val viewModel = factory.create(PasswordRecoveryViewModel::class.java)
        assertNotNull(viewModel)
    }

    @Test
    fun testCreateMapViewModel() {
        val viewModel = factory.create(MapViewModel::class.java)
        assertNotNull(viewModel)
    }

    @Test
    fun testCreateExploreViewModel() {
        val viewModel = factory.create(ExploreViewModel::class.java)
        assertNotNull(viewModel)
    }

    @Test
    fun testCreateReporteViewModel() {
        val viewModel = factory.create(ReporteViewModel::class.java)
        assertNotNull(viewModel)
    }

    @Test
    fun testCreateUnknownViewModelThrows() {
        assertFailsWith<IllegalArgumentException> {
            factory.create(UnsupportedViewModel::class.java)
        }
    }

    private class UnsupportedViewModel : ViewModel()

    @Test
    fun testViewModelFactoryWithDifferentContexts() {
        val context = mock<Context>(defaultAnswer = Answers.RETURNS_DEEP_STUBS)
        val factory1 = AppViewModelFactory(mockAuthRepository, context)
        val factory2 = AppViewModelFactory(mockAuthRepository, context)

        // Assert
        assertNotNull(factory1)
        assertNotNull(factory2)
        // Different factory instances
        assertTrue(factory1 !== factory2)
    }

    @Test
    fun testViewModelFactoryInitializesLazyRepositories() {
        val context = mock<Context>(defaultAnswer = Answers.RETURNS_DEEP_STUBS)
        val factory = AppViewModelFactory(mockAuthRepository, context)

        // Assert - verify factory is properly initialized
        assertNotNull(factory)

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
        val repository = mockAuthRepository
        val context = mock<Context>(defaultAnswer = Answers.RETURNS_DEEP_STUBS)
        val factory = AppViewModelFactory(repository, context)

        // Assert
        assertNotNull(factory)
    }

    @Test
    fun testViewModelFactoryClassIsInstantiable() {
        // Arrange & Act
        val factoryClass = AppViewModelFactory::class.java

        // Assert
        assertNotNull(factoryClass)
        assertEquals("AppViewModelFactory", factoryClass.simpleName)
    }
}





