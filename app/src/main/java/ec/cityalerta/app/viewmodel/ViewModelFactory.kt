package ec.cityalerta.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.data.contracts.map.MapRepositoryContract
import ec.cityalerta.app.model.repository.MapRepository
import ec.cityalerta.app.model.repository.LocationRepository
import ec.cityalerta.app.model.repository.BarrioRepository
import ec.cityalerta.app.model.repository.CiudadRepository
import ec.cityalerta.app.model.repository.PerfilImagenRepository
import ec.cityalerta.app.model.repository.PerfilRepository
import ec.cityalerta.app.model.repository.PerfilResumenRepository
import ec.cityalerta.app.model.repository.PerfilStorageRepository
import ec.cityalerta.app.model.repository.ReporteImagenRepository
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteStorageRepository
import ec.cityalerta.app.model.repository.ReporteUbicacionRepository
import ec.cityalerta.app.model.repository.NominatimGeocodingRepository
import ec.cityalerta.app.model.remote.service.PushSubscriptionRegistrar

class AppViewModelFactory(
    private val authRepository: AuthRepositoryContract,
    private val appContext: Context
) : ViewModelProvider.Factory {

    private val mapRepository: MapRepositoryContract by lazy {
        MapRepository()
    }

    private val reporteRepository by lazy {
        ReporteRepository()
    }

    private val ciudadRepository by lazy {
        CiudadRepository()
    }

    private val barrioRepository by lazy {
        BarrioRepository()
    }

    private val perfilRepository by lazy {
        PerfilRepository()
    }

    private val perfilResumenRepository by lazy {
        PerfilResumenRepository()
    }

    private val ubicacionReporte by lazy {
        ReporteUbicacionRepository()
    }

    private val imagenReporte by lazy {
        ReporteImagenRepository()
    }

    private val imagenPerfil by lazy {
        PerfilImagenRepository()
    }

    private val storageReporte by lazy {
        ReporteStorageRepository()
    }

    private val storagePerfil by lazy {
        PerfilStorageRepository()
    }

    private val geocodingRepository by lazy {
        NominatimGeocodingRepository()
    }

    private val locationProvider by lazy {
        LocationRepository(appContext)
    }

    private val pushRegistrar by lazy {
        PushSubscriptionRegistrar.createDefault(appContext)
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AuthViewModel(authRepository, pushRegistrar) as T
            }
            modelClass.isAssignableFrom(MapViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                MapViewModel(
                    mapRepository,
                    reporteRepository,
                    ubicacionReporte,
                    authRepository,
                    barrioRepository
                ) as T
            }
            modelClass.isAssignableFrom(ExploreViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                ExploreViewModel(
                    authRepository,
                    reporteRepository,
                    imagenReporte,
                    ubicacionReporte,
                    storageReporte,
                    perfilRepository,
                    ciudadRepository,
                    barrioRepository
                ) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                ProfileViewModel(
                    authRepository,
                    perfilRepository,
                    perfilResumenRepository,
                    ciudadRepository,
                    reporteRepository,
                    imagenReporte,
                    imagenPerfil,
                    ubicacionReporte,
                    storageReporte,
                    storagePerfil,
                    barrioRepository
                ) as T
            }
            modelClass.isAssignableFrom(ReporteViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                ReporteViewModel(
                    reporteRepository,
                    imagenReporte,
                    ubicacionReporte,
                    storageReporte,
                    locationProvider,
                    authRepository,
                    mapRepository,
                    barrioRepository,
                    geocodingRepository
                ) as T
            }
            modelClass.isAssignableFrom(SearchReportViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                SearchReportViewModel(
                    reporteRepository,
                    imagenReporte,
                    storageReporte,
                    perfilRepository
                ) as T
            }
            modelClass.isAssignableFrom(PasswordRecoveryViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                PasswordRecoveryViewModel(authRepository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
