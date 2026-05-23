package ec.cityalerta.app.viewmodel

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
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.data.contracts.location.LocationProviderContract
import ec.cityalerta.app.model.data.contracts.map.MapRepositoryContract
import ec.cityalerta.app.model.utils.GeoJsonConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

class ReporteViewModel(
    private val reporteRepository: ReporteRepository,
    private val reporteImagenRepository: ReporteImagenRepository,
    private val reporteUbicacionRepository: ReporteUbicacionRepository,
    private val reporteStorageRepository: ReporteStorageRepository,
    private val locationProvider: LocationProviderContract,
    private val authRepository: AuthRepositoryContract,
    private val mapRepository: MapRepositoryContract
): ViewModel(){

    private val _descripcion = MutableStateFlow("")
    val descripcion: StateFlow<String> = _descripcion

    private val _categoria = MutableStateFlow<ReportType?>(null)
    val categoria: StateFlow<ReportType?> = _categoria

    private val _imagenURL = MutableStateFlow<String?>(null)
    val imagenURL: StateFlow<String?> = _imagenURL
    private val _imagenBytes = MutableStateFlow<ByteArray?>(null)
    private val _lat = MutableStateFlow<Double?>(null)
    private val _lng = MutableStateFlow<Double?>(null)
    private val _currentLocation = MutableStateFlow<UserLocation?>(null)
    val currentLocation: StateFlow<UserLocation?> = _currentLocation
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    fun onDescriptionChange(value: String){
        _descripcion.value = value
    }

    fun onCategoriaChange(value: ReportType){
        _categoria.value = value
    }

    fun setImagen(uri: String) {
        _imagenURL.value = uri
    }

    fun setImagenData(bytes: ByteArray) {
        _imagenBytes.value = bytes
    }

    fun setUbicacion(lat: Double, lng: Double) {
        _lat.value = lat
        _lng.value = lng
    }

    fun requestCurrentLocation() {
        viewModelScope.launch {
            val result = locationProvider.getCurrentLocation()
            result.onSuccess { location ->
                _currentLocation.value = location
                setUbicacion(location.latitude, location.longitude)
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Error al obtener ubicacion"
            }
        }
    }

    fun sendReport(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val validationError = validateReportForm()
            if (validationError != null) {
                _errorMessage.value = validationError
                return@launch
            }

            val usuarioID = authRepository.getUserId().getOrNull().orEmpty()
            val ciudadID = authRepository.getCiudadId().getOrNull().orEmpty()
            val desc = _descripcion.value
            val cat = _categoria.value!!
            val imgBytes = _imagenBytes.value!!
            val lat = _lat.value!!
            val lng = _lng.value!!

            try {
                if (!isLocationInsideCity(lat, lng, ciudadID)) {
                    _errorMessage.value = "Ubicación fuera de los límites permitidos de la ciudad"
                    return@launch
                }

                submitReport(usuarioID, ciudadID, desc, cat, imgBytes, lat, lng)
                resetForm()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = e.message ?: "Error al enviar reporte"
            }
        }
    }

    private suspend fun isLocationInsideCity(lat: Double, lng: Double, ciudadId: String): Boolean {
        val ciudad = mapRepository.getCiudadById(ciudadId)
        return if (ciudad != null) {
            val polygonPoints = GeoJsonConverter.extractPolygonPoints(ciudad.geojson)
            GeoJsonConverter.pointInPolygon(LatLng(lat, lng), polygonPoints)
        } else {
            false
        }
    }

    private suspend fun validateReportForm(): String? {
        val usuarioResult = authRepository.getUserId()
        if (usuarioResult.isFailure) {
            return usuarioResult.exceptionOrNull()?.message ?: "Error al obtener usuario"
        }

        val ciudadResult = authRepository.getCiudadId()
        if (ciudadResult.isFailure) {
            return ciudadResult.exceptionOrNull()?.message ?: "Error al obtener ciudad"
        }

        return when {
            _categoria.value == null -> "Categoria no seleccionada"
            _descripcion.value.isBlank() -> "Falta descripcion"
            _imagenURL.value == null || _imagenBytes.value == null -> "Falta imagen"
            _lat.value == null || _lng.value == null -> "Falta ubicacion"
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
    ) {
        _errorMessage.value = null
        val storageUuid = UUID.randomUUID().toString()
        val storagePath = reporteStorageRepository.uploadReportImage(
            bytes = imgBytes,
            objectName = storageUuid
        ).getOrThrow()

        val ubicacion = reporteUbicacionRepository.create(
            ReporteUbicacionCreateDto(
                lat = lat,
                lng = lng,
                direccion_aproximada = " $lat, $lng"
            )
        ).getOrThrow()

        val reporte = reporteRepository.create(
            ReporteCreateDto(
                usuario_id = usuarioID,
                ciudad_id = ciudadID,
                ubicacion_id = ubicacion.id,
                descripcion = descripcion,
                estado = ReporteEstado.PENDIENTE,
                fecha_reporte = Instant.now().toString(),
                categoria = categoria,
                barrio_id = "aff5277d-95a7-452f-a456-8bc2bc57cb2f"
            )
        ).getOrThrow()

        reporteImagenRepository.create(
            ReporteImagenCreateDto(
                reporte_id = reporte.id,
                storage_uuid = storageUuid,
                url_path = storagePath
            )
        )
    }

    private fun resetForm(){
        _descripcion.value = ""
        _categoria.value = null
        _imagenURL.value = null
        _imagenBytes.value = null
        _lat.value = null
        _lng.value = null
        _currentLocation.value = null
        _errorMessage.value = null
    }
}
