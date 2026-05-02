package ec.cityalerta.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import ec.cityalerta.app.model.repository.IAuthRepository
import ec.cityalerta.app.viewmodel.AppViewModelFactory
import ec.cityalerta.app.viewmodel.AuthViewModel
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock

class AuthViewModelFactoryTest {

    private class AnotherViewModel : ViewModel()

    private val repository: IAuthRepository = mock()
    private val factory = AppViewModelFactory(repository)

    @Test
    fun testCreateReturnsAuthViewModelWhenClassMatches() {
        val viewModel = factory.create(AuthViewModel::class.java as Class<out ViewModel>)

        assertTrue(viewModel is AuthViewModel)
    }

    @Test
    fun testCreateWithCreationExtrasReturnsAuthViewModel() {
        val viewModel = factory.create(AuthViewModel::class.java as Class<out ViewModel>, CreationExtras.Empty)

        assertTrue(viewModel is AuthViewModel)
    }

    @Test
    fun testCreateThrowsWhenClassIsUnknown() {
        val exception = try {
            factory.create(AnotherViewModel::class.java)
            null
        } catch (e: IllegalArgumentException) {
            e
        }

        assertTrue(exception != null)
        assertTrue(exception?.message?.startsWith("Unknown ViewModel class:") ?: false)
    }
}
