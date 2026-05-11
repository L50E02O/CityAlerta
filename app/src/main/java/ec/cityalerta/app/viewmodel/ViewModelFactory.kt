package ec.cityalerta.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import ec.cityalerta.app.model.repository.interfaces.IAuthRepository
import ec.cityalerta.app.model.repository.interfaces.IMapRepository
import ec.cityalerta.app.model.repository.MapRepository
import ec.cityalerta.app.model.repository.LocationRepository
import ec.cityalerta.app.model.repository.ReporteImagenRepository
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteStorageRepository
import ec.cityalerta.app.model.repository.ReporteUbicacionRepository

class AppViewModelFactory(
    private val authRepository: IAuthRepository,
    private val appContext: Context
) : ViewModelProvider.Factory {

    private val mapRepository: IMapRepository by lazy {
        MapRepository()
    }

    private val reporteRepository by lazy {
        ReporteRepository()
    }

    private val ubicacionReporte by lazy {
        ReporteUbicacionRepository()
    }

    private val imagenReporte by lazy {
        ReporteImagenRepository()
    }

    private val storageReporte by lazy {
        ReporteStorageRepository()
    }

    private val locationProvider by lazy {
        LocationRepository(appContext)
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

            modelClass.isAssignableFrom(ReporteViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                ReporteViewModel(
                    reporteRepository,
                    imagenReporte,
                    ubicacionReporte,
                    storageReporte,
                    locationProvider,
                    authRepository
                ) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}