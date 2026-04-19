package man.tap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import man.tap.model.repository.IAuthRepository
import man.tap.model.repository.IMapRepository
import man.tap.model.repository.MapRepository

class AppViewModelFactory(
    private val authRepository: IAuthRepository
) : ViewModelProvider.Factory {

    private val mapRepository: IMapRepository by lazy {
        MapRepository()
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(MapViewModel::class.java) -> {
                MapViewModel(mapRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}