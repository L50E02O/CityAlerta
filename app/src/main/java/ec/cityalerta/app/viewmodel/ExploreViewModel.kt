package ec.cityalerta.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteImagenRepository
import ec.cityalerta.app.model.repository.ReporteUbicacionRepository
import ec.cityalerta.app.model.repository.PerfilRepository
import ec.cityalerta.app.model.repository.CiudadRepository
import ec.cityalerta.app.model.repository.BarrioRepository
import ec.cityalerta.app.model.remote.SupabaseProvider
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ReporteUI(
    val id: String,
    val categoria: String,
    val imageUrl: String?,
    val barrio: String,
    val direccion: String,
    val descripcion: String,
    val estado: String,
    val fecha: String
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
    private val perfilRepository: PerfilRepository = PerfilRepository(),
    private val ciudadRepository: CiudadRepository = CiudadRepository(),
    private val barrioRepository: BarrioRepository = BarrioRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ExploreState())
    val state: StateFlow<ExploreState> = _state

    init {
        Log.d("ExploreViewModel", "Inicializando ExploreViewModel")
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            Log.d("ExploreViewModel", "Cargando datos...")
            try {
                var userId = SupabaseProvider.client.auth.currentUserOrNull()?.id
                userId = "e9d0104d-be61-4b5d-aa26-4b2be1a796c1"
                if (userId == null) {
                    _state.value = _state.value.copy(isLoading = false, error = "Usuario no autenticado")
                    Log.d("ExploreViewModel", "Usuario no autenticado")
                    return@launch
                }
                Log.d("ExploreViewModel", "Usuario autenticado")
                val perfilResult = perfilRepository.getById(userId)
                val perfil = perfilResult.getOrNull()
                if (perfil == null) {
                    _state.value = _state.value.copy(isLoading = false, error = "Perfil no encontrado")
                    return@launch
                }

                val ciudadResult = ciudadRepository.getById(perfil.ciudadId)
                val ciudad = ciudadResult.getOrNull()
                _state.value = _state.value.copy(ciudadNombre = ciudad?.nombre ?: "Ubicación desconocida")

                val reportesResult = reporteRepository.getAll()
                val allReportes = reportesResult.getOrNull() ?: emptyList()
                
                // Filtrar por ciudad del perfil
                val reportesCiudad = allReportes.filter { it.ciudadId == perfil.ciudadId }

                val reportesUI = reportesCiudad.map { reporte ->
                    // Obtener imagen
                    val imagenes = reporteImagenRepository.getAll().getOrNull() ?: emptyList()
                    val firstImage = imagenes.firstOrNull { it.reporteId == reporte.id }?.urlPath

                    // Obtener ubicación
                    val ubicacion = reporteUbicacionRepository.getById(reporte.ubicacionId).getOrNull()
                    
                    // Encontrar barrio (Placeholder por ahora, ya que no hay link directo claro en los modelos actuales)
                    // En una app real, esto vendría de un join en el backend o una query espacial.
                    // Para cumplir con el requerimiento UI, usaremos el nombre del barrio si lo encontramos o un valor por defecto.
                    val barrioNombre = "Sector " + (ubicacion?.direccionAproximada?.split(",")?.firstOrNull() ?: "General")

                    ReporteUI(
                        id = reporte.id,
                        categoria = when(reporte.categoria) {
                            ec.cityalerta.app.model.data.reporte.ReportType.ZONA_DE_RIESGO -> "Seguridad"
                            ec.cityalerta.app.model.data.reporte.ReportType.BACHE -> "Bache"
                            ec.cityalerta.app.model.data.reporte.ReportType.AGUA -> "Agua"
                            ec.cityalerta.app.model.data.reporte.ReportType.LUZ -> "Luz"
                            else -> "General"
                        },
                        imageUrl = firstImage,
                        barrio = barrioNombre,
                        direccion = ubicacion?.direccionAproximada ?: "Dirección no disponible",
                        descripcion = reporte.descripcion,
                        estado = when(reporte.estado) {
                            ec.cityalerta.app.model.data.reporte.ReporteEstado.PENDIENTE -> "Pendiente"
                            ec.cityalerta.app.model.data.reporte.ReporteEstado.EN_PROCESO -> "En Proceso"
                            ec.cityalerta.app.model.data.reporte.ReporteEstado.RESUELTO -> "Resuelto"
                        },
                        fecha = reporte.fechaReporte
                    )
                }

                _state.value = _state.value.copy(isLoading = false, reportes = reportesUI)

            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
