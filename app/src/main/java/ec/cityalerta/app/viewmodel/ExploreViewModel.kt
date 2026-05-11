package ec.cityalerta.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ec.cityalerta.app.BuildConfig
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteImagenRepository
import ec.cityalerta.app.model.repository.ReporteUbicacionRepository
import ec.cityalerta.app.model.repository.ReporteStorageRepository
import ec.cityalerta.app.model.repository.PerfilRepository
import ec.cityalerta.app.model.repository.CiudadRepository
import ec.cityalerta.app.model.repository.BarrioRepository
import ec.cityalerta.app.model.remote.SupabaseProvider
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

data class ReporteUI(
    val id: String,
    val categoria: String,
    val imageUrl: String?,
    val barrio: String,
    val direccion: String,
    val descripcion: String,
    val estado: String,
    val fecha: String,
    val timeAgo: String
)

data class ExploreState(
    val isLoading: Boolean = false,
    val ciudadNombre: String = "Cargando...",
    val reportes: List<ReporteUI> = emptyList(),
    val error: String? = null
)

class ExploreViewModel(
    private val reporteRepository: ReporteRepository = ReporteRepository(),
    private val reporteImagenRepository: ReporteImagenRepository = ReporteImagenRepository(),
    private val reporteUbicacionRepository: ReporteUbicacionRepository = ReporteUbicacionRepository(),
    private val reporteStorageRepository: ReporteStorageRepository = ReporteStorageRepository(),
    private val perfilRepository: PerfilRepository = PerfilRepository(),
    private val ciudadRepository: CiudadRepository = CiudadRepository(),
    private val barrioRepository: BarrioRepository = BarrioRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ExploreState())
    val state: StateFlow<ExploreState> = _state

    private fun calculateTimeAgo(createdAt: String?): String {
        if (createdAt.isNullOrBlank()) {
            return "Hace poco"
        }

        return try {
            val createdAtInstant = Instant.parse(createdAt)
            val now = Instant.now()

            val minutes = ChronoUnit.MINUTES.between(createdAtInstant, now)
            val hours = ChronoUnit.HOURS.between(createdAtInstant, now)
            val days = ChronoUnit.DAYS.between(createdAtInstant, now)

            Log.d("ExploreViewModel", "Minutos: $minutes, Horas: $hours, Días: $days")

            when {
                days > 0 -> "Hace $days dia${if (days > 1) "s" else ""}"
                hours > 0 -> "Hace $hours hora${if (hours > 1) "s" else ""}"
                minutes > 0 -> "Hace $minutes minuto${if (minutes > 1) "s" else ""}"
                else -> "Hace poco"
            }
        } catch (e: Exception) {
            Log.e("ExploreViewModel", "Error calculando tiempo: ${e.message}")
            "Hace poco"
        }
    }

    public fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            Log.d("ExploreViewModel", "Cargando datos...")
            try {
                val userId = SupabaseProvider.client.auth.currentUserOrNull()?.id
                Log.d("ExploreViewModel", "ID del usuario: $userId")
                if (userId == null) {
                    _state.value = _state.value.copy(isLoading = false, error = "Usuario no autenticado")
                    Log.d("ExploreViewModel", "Usuario no autenticado")
                    return@launch
                }
                Log.d("ExploreViewModel", "Usuario autenticado")
                val perfilResult = perfilRepository.getById(userId)
                val perfil = perfilResult.getOrNull()
                Log.d("ExploreViewModel", "Perfil obtenido: $perfil")
                if (perfil == null) {
                    _state.value = _state.value.copy(isLoading = false, error = "Perfil no encontrado")
                    Log.d("ExploreViewModel", "Perfil no encontrado")
                    return@launch
                }

                val ciudadResult = ciudadRepository.getById(perfil.ciudadId)
                val ciudad = ciudadResult.getOrNull()
                _state.value = _state.value.copy(ciudadNombre = ciudad?.nombre ?: "Ubicación desconocida")
                Log.d("ExploreViewModel", "Ciudad obtenida: ${ciudad?.nombre ?: "Ubicación desconocida"}")

                // Obtener reportes solo de la ciudad del usuario
                val reportesResult = reporteRepository.getReporteByCiudadId(perfil.ciudadId)
                val reportesCiudad = reportesResult.getOrNull() ?: emptyList()
                Log.d("ExploreViewModel", "Reportes de la ciudad: $reportesCiudad")

                val reportesUI = reportesCiudad.map { reporte ->
                    // Obtener primera imagen del reporte
                    val primerImagen = reporteImagenRepository.getFirstImagenByReporteId(reporte.id).getOrNull()

                    // Convertir UUID a URL firmada
                    val imageUrl = if (primerImagen != null) {
                        val signedPath = reporteStorageRepository.generateSignedImageUrl(primerImagen.url_path).getOrNull()
                        if (signedPath != null) {
                            "${BuildConfig.STORAGE_BASE_URL}$signedPath"
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                    Log.d("ExploreViewModel", "URL generada para imagen: $imageUrl")

                    // Obtener ubicación
                    val ubicacion = reporteUbicacionRepository.getById(reporte.ubicacion_id).getOrNull()
                    
                    // Obtener nombre del barrio usando barrio_id
                    val barrio = barrioRepository.getById(reporte.barrio_id).getOrNull()
                    Log.d("ExploreViewModel", "Barrio obtenido: $barrio")
                    val barrioNombre = barrio?.nombre ?: "Barrio desconocido"

                    ReporteUI(
                        id = reporte.id,
                        categoria = when(reporte.categoria) {
                            ec.cityalerta.app.model.data.reporte.ReportType.ZONA_DE_RIESGO -> "Seguridad"
                            ec.cityalerta.app.model.data.reporte.ReportType.BACHE -> "Bache"
                            ec.cityalerta.app.model.data.reporte.ReportType.AGUA -> "Agua"
                            ec.cityalerta.app.model.data.reporte.ReportType.LUZ -> "Luz"
                            else -> "General"
                        },
                        imageUrl = imageUrl,
                        barrio = barrioNombre,
                        direccion = ubicacion?.direccion_aproximada ?: "Dirección no disponible",
                        descripcion = reporte.descripcion,
                        estado = when(reporte.estado) {
                            ec.cityalerta.app.model.data.reporte.ReporteEstado.PENDIENTE -> "Pendiente"
                            ec.cityalerta.app.model.data.reporte.ReporteEstado.EN_PROCESO -> "En Proceso"
                            ec.cityalerta.app.model.data.reporte.ReporteEstado.RESUELTO -> "Resuelto"
                        },
                        fecha = reporte.fecha_reporte,
                        timeAgo = calculateTimeAgo(reporte.created_at)
                    )
                }

                _state.value = _state.value.copy(isLoading = false, reportes = reportesUI)

            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
