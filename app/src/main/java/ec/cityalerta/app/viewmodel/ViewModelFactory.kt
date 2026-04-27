package ec.cityalerta.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ec.cityalerta.app.model.repository.IAuthRepository
import ec.cityalerta.app.model.repository.IMapRepository
import ec.cityalerta.app.model.repository.MapRepository

class AppViewModelFactory(
    private val authRepository: IAuthRepository
) : ViewModelProvider.Factory {

    private val mapRepository: IMapRepository by lazy {
        MapRepository()
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AuthViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(MapViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                MapViewModel(mapRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}