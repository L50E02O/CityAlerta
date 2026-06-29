package ec.cityalerta.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.ReporteCreateDto
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.data.reporteimagen.ReporteImagenCreateDto
import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacionCreateDto
import ec.cityalerta.app.model.data.location.UserLocation
import ec.cityalerta.app.model.repository.ReporteImagenRepository
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteStorageRepository
import ec.cityalerta.app.model.repository.ReporteUbicacionRepository
import ec.cityalerta.app.model.repository.BarrioRepository
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.data.contracts.geocoding.GeocodingRepositoryContract
import ec.cityalerta.app.model.data.contracts.location.LocationProviderContract
import ec.cityalerta.app.model.data.contracts.map.MapRepositoryContract
import ec.cityalerta.app.model.data.contracts.moderation.ImageModerationContract
import ec.cityalerta.app.model.data.contracts.moderation.ModerationResult
import ec.cityalerta.app.model.utils.GeoJsonConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

data class ReporteUiState(
    val descripcion: String = "",
    val categoria: ReportType? = null,
    val imagenURL: String? = null,
    val imagenBytes: ByteArray? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val currentLocation: UserLocation? = null,
    val infoMessage: String? = null,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val isAnalyzingImage: Boolean = false
)

class ReporteViewModel(
    private val reporteRepository: ReporteRepository,
    private val reporteImagenRepository: ReporteImagenRepository,
    private val reporteUbicacionRepository: ReporteUbicacionRepository,
    private val reporteStorageRepository: ReporteStorageRepository,
    private val locationProvider: LocationProviderContract,
    private val authRepository: AuthRepositoryContract,
    private val mapRepository: MapRepositoryContract,
    private val barrioRepository: BarrioRepository,
    private val geocodingRepository: GeocodingRepositoryContract,
    private val imageModerationRepository: ImageModerationContract
): ViewModel() {

    init {
        Log.d("ReporteViewModel", "[VM-${hashCode()}] Instancia creada")
    }

    private val _uiState = MutableStateFlow(ReporteUiState())
    val uiState: StateFlow<ReporteUiState> = _uiState.asStateFlow()

    // Helpers para compatibilidad con las pantallas mientras migran
    // Se corrigió la implementación para que sean reactivos al _uiState
    val descripcion = _uiState.map { it.descripcion }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _uiState.value.descripcion)
    
    val categoria = _uiState.map { it.categoria }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _uiState.value.categoria)
    
    val currentLocation = _uiState.map { it.currentLocation }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _uiState.value.currentLocation)
    
    val errorMessage = _uiState.map { it.errorMessage }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _uiState.value.errorMessage)

    val infoMessage = _uiState.map { it.infoMessage }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _uiState.value.infoMessage)

    val isSubmitting = _uiState.map { it.isSubmitting }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _uiState.value.isSubmitting)

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(descripcion = value) }
    }

    fun onCategoriaChange(value: ReportType) {
        _uiState.update { it.copy(categoria = value) }
    }

    fun setImagen(uri: String) {
        _uiState.update { it.copy(imagenURL = uri) }
    }

    fun setImagenData(bytes: ByteArray) {
        _uiState.update { it.copy(imagenBytes = bytes) }
    }

    fun setUbicacion(lat: Double, lng: Double) {
        _uiState.update { it.copy(lat = lat, lng = lng, currentLocation = UserLocation(lat, lng)) }
    }

    fun requestCurrentLocation() {
        viewModelScope.launch {
            val result = locationProvider.getCurrentLocation()
            result.onSuccess { location ->
                setUbicacion(location.latitude, location.longitude)
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "Error al obtener ubicación") }
            }
        }
    }

    fun clearInfoMessage() {
        Log.d("ReporteViewModel", "[VM-${hashCode()}] clearInfoMessage llamado")
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun sendReport(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            if (_uiState.value.isSubmitting) return@launch
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            
            val validationError = validateReportForm()
            if (validationError != null) {
                _uiState.update { it.copy(errorMessage = validationError, isSubmitting = false) }
                return@launch
            }

            try {
                // Validación de IA para contenido explícito (Imagen + Descripción)
                Log.d("ReporteViewModel", "Iniciando validación de contenido con IA")
                _uiState.update { it.copy(isAnalyzingImage = true) }
                
                val currentDescription = _uiState.value.descripcion
                val currentImageBytes = _uiState.value.imagenBytes!!
                
                val analysisResult = imageModerationRepository.analyzeContent(currentImageBytes, currentDescription)
                val moderation = analysisResult.getOrElse { 
                    Log.e("ReporteViewModel", "Error técnico en moderación de IA: ${it.message}. Permitiendo reporte por defecto.")
                    // Si la IA falla por red/API, NO bloqueamos al usuario
                    ModerationResult(isSafe = true)
                }
                
                _uiState.update { it.copy(isAnalyzingImage = false) }
                Log.d("ReporteViewModel", "Validación IA finalizada. ¿Es seguro?: ${moderation.isSafe}")

                if (!moderation.isSafe) {
                    val errorMsg = when {
                        moderation.imageFlagged && moderation.descriptionFlagged -> 
                            "La imagen y la descripción infringen las normas de conducta."
                        moderation.imageFlagged -> 
                            "La imagen infringe las normas de conducta (posible contenido explícito)."
                        moderation.descriptionFlagged -> 
                            "La descripción contiene lenguaje no permitido o enlaces sospechosos."
                        else -> "El contenido no cumple con las normas de la aplicación."
                    }
                    
                    Log.w("ReporteViewModel", "Contenido rechazado por la IA: $errorMsg")
                    _uiState.update { it.copy(
                        errorMessage = errorMsg,
                        isSubmitting = false 
                    ) }
                    return@launch
                }

                Log.d("ReporteViewModel", "Contenido aprobado. Procediendo con el envío a Supabase")
                val usuarioID = authRepository.getUserId().getOrNull().orEmpty()
                val ciudadID = authRepository.getCiudadId().getOrNull().orEmpty()
                val lat = _uiState.value.lat!!
                val lng = _uiState.value.lng!!

                if (!isLocationInsideCity(lat, lng, ciudadID)) {
                    _uiState.update { it.copy(errorMessage = "Ubicación fuera de los límites permitidos de la ciudad", isSubmitting = false) }
                    return@launch
                }

                submitReport(usuarioID, ciudadID, _uiState.value.descripcion, _uiState.value.categoria!!, _uiState.value.imagenBytes!!, lat, lng)
                    .onSuccess {
                        Log.d("ReporteViewModel", "[VM-${hashCode()}] Éxito en submitReport")
                        resetForm()
                        _uiState.update { it.copy(infoMessage = "Reporte enviado con éxito") }
                        Log.d("ReporteViewModel", "[VM-${hashCode()}] infoMessage seteado a: ${_uiState.value.infoMessage}")
                        onSuccess()
                    }
                    .onFailure { error ->
                        _uiState.update { it.copy(errorMessage = error.message ?: "Error al guardar el reporte", isSubmitting = false) }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Error al enviar reporte", isSubmitting = false) }
            } finally {
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    private suspend fun isLocationInsideCity(lat: Double, lng: Double, ciudadId: String): Boolean {
        val ciudad = mapRepository.getCiudadById(ciudadId) ?: return false
        val polygonPoints = GeoJsonConverter.extractPolygonPoints(ciudad.geojson)
        return GeoJsonConverter.pointInPolygon(LatLng(lat, lng), polygonPoints)
    }

    private suspend fun validateReportForm(): String? {
        val state = _uiState.value
        if (authRepository.getUserId().isFailure) return "Error al obtener usuario"
        if (authRepository.getCiudadId().isFailure) return "Error al obtener ciudad"

        return when {
            state.categoria == null -> "Categoría no seleccionada"
            state.descripcion.isBlank() -> "Falta descripción"
            state.imagenURL == null || state.imagenBytes == null -> "Falta imagen"
            state.lat == null || state.lng == null -> "Falta ubicación"
            else -> null
        }
    }

    private suspend fun submitReport(
        usuarioID: String,
        ciudadID: String,
        descripcion: String,
        categoria: ReportType,
        imgBytes: ByteArray,
        lat: Double,
        lng: Double
    ): Result<Unit> = runCatching {
        val barrioResult = barrioRepository.getByPoint(ciudadID, lat, lng)
        var barrio = barrioResult.getOrNull()

        if (barrio == null) {
            val cityBarrios = barrioRepository.getAll().getOrNull()?.filter { it.ciudadId == ciudadID } ?: emptyList()
            barrio = cityBarrios.minByOrNull { b ->
                val firstPoint = b.perimetro.coordinates.firstOrNull()?.firstOrNull()
                if (firstPoint != null && firstPoint.size >= 2) {
                    val dLat = lat - firstPoint[1]
                    val dLng = lng - firstPoint[0]
                    dLat * dLat + dLng * dLng
                } else Double.MAX_VALUE
            }
        }

        if (barrio == null) throw Exception("Esta ciudad aún no cuenta con zonas de cobertura registradas.")

        val direccion = resolveDireccion(lat, lng)
        val ubicacion = reporteUbicacionRepository.create(
            ReporteUbicacionCreateDto(lat = lat, lng = lng, direccion_aproximada = direccion)
        ).getOrThrow()

        val storageUuid = UUID.randomUUID().toString()
        val storagePath = reporteStorageRepository.uploadReportImage(imgBytes, storageUuid).getOrThrow()

        val reporte = reporteRepository.create(
            ReporteCreateDto(
                usuario_id = usuarioID,
                ciudad_id = ciudadID,
                ubicacion_id = ubicacion.id,
                descripcion = descripcion,
                estado = ReporteEstado.PENDIENTE,
                fecha_reporte = Instant.now().toString(),
                categoria = categoria,
                barrio_id = barrio.id
            )
        ).getOrThrow()

        reporteImagenRepository.create(
            ReporteImagenCreateDto(reporte_id = reporte.id, storage_uuid = storageUuid, url_path = storagePath)
        ).getOrThrow()
    }

    private fun resetForm() {
        _uiState.value = ReporteUiState()
    }

    private suspend fun resolveDireccion(lat: Double, lng: Double): String {
        return geocodingRepository.reverseGeocode(lat, lng).getOrNull() ?: "Dirección no disponible"
    }

    suspend fun getCityCenter(ciudadId: String): LatLng? {
        return mapRepository.getCiudadById(ciudadId)?.let { LatLng(it.centroLat, it.centroLng) }
    }
}
