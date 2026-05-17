package ec.cityalerta.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ec.cityalerta.app.model.data.perfil.PerfilResumen
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.data.reporte.ReporteUpdateDto
import ec.cityalerta.app.model.data.perfilimagen.PerfilImagenCreateDto
import ec.cityalerta.app.model.data.perfilimagen.PerfilImagenUpdateDto
import ec.cityalerta.app.model.data.reporteimagen.ReporteImagenCreateDto
import ec.cityalerta.app.model.data.reporteimagen.ReporteImagenUpdateDto
import ec.cityalerta.app.model.data.perfil.PerfilUpdateDto
import ec.cityalerta.app.model.repository.AuthRepository
import ec.cityalerta.app.model.repository.BarrioRepository
import ec.cityalerta.app.model.repository.CiudadRepository
import ec.cityalerta.app.model.repository.PerfilRepository
import ec.cityalerta.app.model.utils.EmailValidator
import ec.cityalerta.app.model.repository.PerfilImagenRepository
import ec.cityalerta.app.model.repository.PerfilResumenRepository
import ec.cityalerta.app.model.repository.PerfilStorageRepository
import ec.cityalerta.app.model.repository.ReporteImagenRepository
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteStorageRepository
import ec.cityalerta.app.model.repository.ReporteUbicacionRepository
import ec.cityalerta.app.model.remote.SupabaseProvider
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

private const val TIME_AGO_RECENT = "Hace poco"

data class UserReportUi(
    val id: String,
    val categoria: ReportType,
    val imageUrl: String?,
    val imageId: String?,
    val storageUuid: String?,
    val barrio: String,
    val direccion: String,
    val descripcion: String,
    val estado: ReporteEstado,
    val fecha: String,
    val timeAgo: String,
    val ubicacionId: String
)

data class ProfileState(
    val isLoading: Boolean = false,
    val isUploadingImage: Boolean = false,
    val isSavingSettings: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val errorMessage: String? = null,
    val settingsInfoMessage: String? = null,
    val fullName: String = "",
    val initials: String = "",
    val profileImageUrl: String? = null,
    val profileImageId: String? = null,
    val profileStorageUuid: String? = null,
    val cityName: String = "",
    val totalReports: Int = 0,
    val resolvedReports: Int = 0,
    val myReports: List<UserReportUi> = emptyList()
)

class ProfileViewModel(
    private val authRepository: AuthRepositoryContract,
    private val perfilRepository: PerfilRepository,
    private val perfilResumenRepository: PerfilResumenRepository,
    private val ciudadRepository: CiudadRepository,
    private val reporteRepository: ReporteRepository,
    private val reporteImagenRepository: ReporteImagenRepository,
    private val perfilImagenRepository: PerfilImagenRepository,
    private val reporteUbicacionRepository: ReporteUbicacionRepository,
    private val reporteStorageRepository: ReporteStorageRepository,
    private val perfilStorageRepository: PerfilStorageRepository,
    private val barrioRepository: BarrioRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state

    private var summaryLoaded = false

    fun loadSummaryIfNeeded(force: Boolean = false) {
        if (!force && summaryLoaded && _state.value.fullName.isNotBlank()) return
        loadSummary()
    }

    fun loadSummary() {
        viewModelScope.launch {
            setLoading(true)
            loadSummaryInternal()?.let { resumen ->
                val (cityName, profileImage) = coroutineScope {
                    val cityDeferred = async { ciudadRepository.getById(resumen.ciudadId).getOrNull()?.nombre.orEmpty() }
                    val imageDeferred = async { loadProfileImage(resumen.id) }
                    cityDeferred.await() to imageDeferred.await()
                }
                _state.value = _state.value.copy(
                    errorMessage = null,
                    fullName = resumen.nombreCompleto,
                    initials = buildInitials(resumen.nombreCompleto),
                    profileImageUrl = profileImage?.first,
                    profileImageId = profileImage?.second,
                    profileStorageUuid = profileImage?.third,
                    cityName = cityName,
                    totalReports = resumen.totalReportes,
                    resolvedReports = resumen.reportesResueltos,
                    myReports = emptyList()
                )
                summaryLoaded = true
            }
            setLoading(false)
        }
    }

    fun loadDashboard() {
        viewModelScope.launch {
            setLoading(true)
            val resumen = loadSummaryInternal() ?: run {
                setLoading(false)
                return@launch
            }

            val (cityName, reportes, profileImage) = coroutineScope {
                val cityDeferred = async { ciudadRepository.getById(resumen.ciudadId).getOrNull()?.nombre.orEmpty() }
                val reportesDeferred = async { loadUserReports(resumen.id) }
                val imageDeferred = async { loadProfileImage(resumen.id) }
                Triple(cityDeferred.await(), reportesDeferred.await(), imageDeferred.await())
            }

            _state.value = _state.value.copy(
                errorMessage = null,
                fullName = resumen.nombreCompleto,
                initials = buildInitials(resumen.nombreCompleto),
                profileImageUrl = profileImage?.first,
                profileImageId = profileImage?.second,
                profileStorageUuid = profileImage?.third,
                cityName = cityName,
                totalReports = resumen.totalReportes,
                resolvedReports = resumen.reportesResueltos,
                myReports = reportes
            )
            summaryLoaded = true
            setLoading(false)
        }
    }

    fun loadMyReports() {
        viewModelScope.launch {
            setLoading(true)
            val resumen = loadSummaryInternal() ?: run {
                setLoading(false)
                return@launch
            }

            val reportes = loadUserReports(resumen.id)
            _state.value = _state.value.copy(
                errorMessage = null,
                myReports = reportes
            )
            setLoading(false)
        }
    }

    fun updateReport(
        report: UserReportUi,
        descripcion: String,
        categoria: ReportType,
        newImageBytes: ByteArray? = null
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                myReports = _state.value.myReports.map { item ->
                    if (item.id == report.id) {
                        item.copy(descripcion = descripcion, categoria = categoria)
                    } else {
                        item
                    }
                },
                errorMessage = null
            )

            reporteRepository.update(
                ReporteUpdateDto(
                    descripcion = descripcion,
                    categoria = categoria
                ),
                report.id
            ).onSuccess {
                if (newImageBytes != null) {
                    replaceReportImage(report, newImageBytes).onFailure { error ->
                        _state.value = _state.value.copy(
                            errorMessage = error.message ?: "No se pudo actualizar la imagen"
                        )
                    }
                }
                refreshMyReports()
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    errorMessage = error.message ?: "No se pudo actualizar el reporte"
                )
                refreshMyReports()
            }
        }
    }

    private suspend fun replaceReportImage(report: UserReportUi, imageBytes: ByteArray): Result<Unit> = runCatching {
        val storageUuid = UUID.randomUUID().toString()
        val storagePath = reporteStorageRepository.uploadReportImage(imageBytes, storageUuid).getOrThrow()
        val previousStorageUuid = report.storageUuid

        if (report.imageId.isNullOrBlank()) {
            reporteImagenRepository.create(
                ReporteImagenCreateDto(
                    reporte_id = report.id,
                    storage_uuid = storageUuid,
                    url_path = storagePath
                )
            ).getOrThrow()
        } else {
            reporteImagenRepository.update(
                ReporteImagenUpdateDto(
                    storage_uuid = storageUuid,
                    url_path = storagePath
                ),
                report.imageId
            ).getOrThrow()
        }

        if (!previousStorageUuid.isNullOrBlank() && previousStorageUuid != storageUuid) {
            reporteStorageRepository.deleteReportImage(previousStorageUuid)
        }
    }

    private suspend fun refreshMyReports() {
        val userId = SupabaseProvider.client.auth.currentUserOrNull()?.id
            ?: authRepository.getUserId().getOrNull()
            ?: return

        val reportes = loadUserReports(userId)
        _state.value = _state.value.copy(myReports = reportes)
    }

    fun deleteReport(report: UserReportUi) {
        viewModelScope.launch {
            val storageUuid = report.storageUuid.orEmpty()
            val imageId = report.imageId.orEmpty()

            if (storageUuid.isNotBlank()) {
                reporteStorageRepository.deleteReportImage(storageUuid)
            }

            if (imageId.isNotBlank()) {
                reporteImagenRepository.delete(imageId)
            }

            reporteRepository.delete(report.id)
                .onSuccess {
                    reporteUbicacionRepository.delete(report.ubicacionId)
                    refreshMyReports()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(errorMessage = error.message ?: "No se pudo eliminar el reporte")
                }
        }
    }

    fun deleteProfileImage() {
        viewModelScope.launch {
            val imageId = _state.value.profileImageId
            val storageUuid = _state.value.profileStorageUuid

            if (imageId.isNullOrBlank()) return@launch

            _state.value = _state.value.copy(isUploadingImage = true, errorMessage = null)

            if (!storageUuid.isNullOrBlank()) {
                perfilStorageRepository.deleteProfileImage(storageUuid).getOrNull()
            }
            perfilImagenRepository.delete(imageId)
                .onSuccess {
                    _state.value = _state.value.copy(
                        profileImageUrl = null,
                        profileImageId = null,
                        profileStorageUuid = null,
                        isUploadingImage = false,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isUploadingImage = false,
                        errorMessage = error.message ?: "No se pudo eliminar la foto de perfil"
                    )
                }
        }
    }

    fun updateProfileImage(imageBytes: ByteArray) {
        viewModelScope.launch {
            val userId = SupabaseProvider.client.auth.currentUserOrNull()?.id
                ?: authRepository.getUserId().getOrNull()
                ?: run {
                    _state.value = _state.value.copy(errorMessage = "Usuario no autenticado")
                    return@launch
                }

            _state.value = _state.value.copy(isUploadingImage = true, errorMessage = null)

            replaceProfileImage(userId, imageBytes)
                .onSuccess { profileImage ->
                    _state.value = _state.value.copy(
                        profileImageUrl = profileImage.first,
                        profileImageId = profileImage.second,
                        profileStorageUuid = profileImage.third,
                        isUploadingImage = false
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isUploadingImage = false,
                        errorMessage = error.message ?: "No se pudo actualizar la foto de perfil"
                    )
                }
        }
    }

    private suspend fun replaceProfileImage(
        perfilId: String,
        imageBytes: ByteArray
    ): Result<Triple<String?, String?, String?>> = runCatching {
        val existing = perfilImagenRepository.getImagenByPerfilId(perfilId).getOrNull()
        val previousStorageUuid = existing?.storage_uuid
        val existingImageId = existing?.id

        val storageUuid = UUID.randomUUID().toString()
        val storagePath = perfilStorageRepository.uploadProfileImage(imageBytes, storageUuid).getOrThrow()

        val savedImageId = if (existingImageId.isNullOrBlank()) {
            perfilImagenRepository.create(
                PerfilImagenCreateDto(
                    perfil_id = perfilId,
                    storage_uuid = storageUuid,
                    url_path = storagePath
                )
            ).getOrThrow().id
        } else {
            perfilImagenRepository.update(
                PerfilImagenUpdateDto(
                    storage_uuid = storageUuid,
                    url_path = storagePath
                ),
                existingImageId
            ).getOrThrow().id
        }

        if (!previousStorageUuid.isNullOrBlank() && previousStorageUuid != storageUuid) {
            perfilStorageRepository.deleteProfileImage(previousStorageUuid).getOrNull()
        }

        val imageUrl = perfilStorageRepository.generateSignedImageUrl(storageUuid).getOrNull()
        Triple(imageUrl, savedImageId, storageUuid)
    }

    private suspend fun loadProfileImage(perfilId: String): Triple<String?, String?, String?>? {
        val imagen = perfilImagenRepository.getImagenByPerfilId(perfilId).getOrNull() ?: return null
        val imageUrl = imagen.storage_uuid.takeIf { it.isNotBlank() }?.let { objectPath ->
            perfilStorageRepository.generateSignedImageUrl(objectPath).getOrNull()
        }
        return Triple(imageUrl, imagen.id, imagen.storage_uuid)
    }

    suspend fun getUserEmail(): String? {
        return authRepository.getUserEmail().getOrNull()
    }

    fun clearSettingsMessages() {
        _state.value = _state.value.copy(settingsInfoMessage = null, errorMessage = null)
    }

    fun updateFullName(newName: String, emptyNameMessage: String, successMessage: String, errorMessage: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            _state.value = _state.value.copy(errorMessage = emptyNameMessage, settingsInfoMessage = null)
            return
        }

        viewModelScope.launch {
            val userId = SupabaseProvider.client.auth.currentUserOrNull()?.id
                ?: authRepository.getUserId().getOrNull()
                ?: run {
                    _state.value = _state.value.copy(errorMessage = errorMessage)
                    return@launch
                }

            _state.value = _state.value.copy(isSavingSettings = true, errorMessage = null, settingsInfoMessage = null)

            perfilRepository.update(PerfilUpdateDto(nombreCompleto = trimmed), userId)
                .onSuccess {
                    summaryLoaded = false
                    loadSummaryIfNeeded(force = true)
                    _state.value = _state.value.copy(
                        isSavingSettings = false,
                        settingsInfoMessage = successMessage,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isSavingSettings = false,
                        errorMessage = error.message?.ifBlank { null } ?: errorMessage
                    )
                }
        }
    }

    fun updateEmail(
        newEmail: String,
        currentEmail: String,
        invalidEmailMessage: String,
        sameEmailMessage: String,
        successMessage: String,
        errorMessage: String
    ): EmailUpdateValidation {
        val trimmed = newEmail.trim()
        if (!EmailValidator.isValid(trimmed)) {
            return EmailUpdateValidation.INVALID
        }
        if (trimmed.equals(currentEmail.trim(), ignoreCase = true)) {
            _state.value = _state.value.copy(errorMessage = sameEmailMessage, settingsInfoMessage = null)
            return EmailUpdateValidation.SAME
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSavingSettings = true, errorMessage = null, settingsInfoMessage = null)

            authRepository.updateEmail(trimmed)
                .onSuccess {
                    _state.value = _state.value.copy(
                        isSavingSettings = false,
                        settingsInfoMessage = successMessage,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isSavingSettings = false,
                        errorMessage = error.message?.ifBlank { null } ?: errorMessage
                    )
                }
        }
        return EmailUpdateValidation.OK
    }

    enum class EmailUpdateValidation {
        OK, INVALID, SAME
    }

    fun deleteAccount(
        successMessage: String,
        errorMessage: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isDeletingAccount = true,
                errorMessage = null,
                settingsInfoMessage = null
            )

            authRepository.deleteAccount()
                .onSuccess {
                    summaryLoaded = false
                    _state.value = ProfileState()
                    authRepository.logOut()
                    onSuccess()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isDeletingAccount = false,
                        errorMessage = error.message?.ifBlank { null } ?: errorMessage
                    )
                }
        }
    }

    fun logOut(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logOut()
                .onSuccess {
                    summaryLoaded = false
                    _state.value = ProfileState()
                    onSuccess()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(errorMessage = error.message ?: "No se pudo cerrar la sesion")
                }
        }
    }

    val hasCustomProfileImage: Boolean
        get() = !_state.value.profileImageId.isNullOrBlank()

    private suspend fun loadSummaryInternal(): PerfilResumen? {
        val userId = SupabaseProvider.client.auth.currentUserOrNull()?.id
            ?: authRepository.getUserId().getOrNull()

        if (userId == null) {
            _state.value = _state.value.copy(errorMessage = "Usuario no autenticado")
            return null
        }

        return perfilResumenRepository.getCurrentResumen().getOrNull()
    }

    private suspend fun loadUserReports(userId: String): List<UserReportUi> = coroutineScope {
        val reportes = reporteRepository.getReporteByUsuarioId(userId).getOrNull().orEmpty()
        reportes.map { reporte ->
            async { mapReportToUi(reporte) }
        }.mapNotNull { it.await() }
    }

    private suspend fun mapReportToUi(reporte: Reporte): UserReportUi? {
        val primerImagen = reporteImagenRepository.getFirstImagenByReporteId(reporte.id).getOrNull()
        val imageUrl = primerImagen?.storage_uuid?.takeIf { it.isNotBlank() }?.let { objectPath ->
            reporteStorageRepository.generateSignedImageUrl(objectPath).getOrNull()
        }

        val ubicacion = reporteUbicacionRepository.getById(reporte.ubicacion_id).getOrNull()
        val barrio = barrioRepository.getById(reporte.barrio_id).getOrNull()

        return UserReportUi(
            id = reporte.id,
            categoria = reporte.categoria,
            imageUrl = imageUrl,
            imageId = primerImagen?.id,
            storageUuid = primerImagen?.storage_uuid,
            barrio = barrio?.nombre ?: "Barrio desconocido",
            direccion = ubicacion?.direccion_aproximada ?: "Direccion no disponible",
            descripcion = reporte.descripcion,
            estado = reporte.estado,
            fecha = reporte.fecha_reporte,
            timeAgo = calculateTimeAgo(reporte.created_at),
            ubicacionId = reporte.ubicacion_id
        )
    }

    private fun calculateTimeAgo(createdAt: String?): String {
        if (createdAt.isNullOrBlank()) return TIME_AGO_RECENT

        return try {
            val createdAtInstant = Instant.parse(createdAt)
            val now = Instant.now()
            val minutes = ChronoUnit.MINUTES.between(createdAtInstant, now)
            val hours = ChronoUnit.HOURS.between(createdAtInstant, now)
            val days = ChronoUnit.DAYS.between(createdAtInstant, now)

            when {
                days > 0 -> "Hace $days dia${if (days > 1) "s" else ""}"
                hours > 0 -> "Hace $hours hora${if (hours > 1) "s" else ""}"
                minutes > 0 -> "Hace $minutes minuto${if (minutes > 1) "s" else ""}"
                else -> TIME_AGO_RECENT
            }
        } catch (_: Exception) {
            TIME_AGO_RECENT
        }
    }

    private fun buildInitials(fullName: String): String {
        val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
        if (parts.isEmpty()) return ""
        return parts.take(2).joinToString("") { part -> part.first().uppercaseChar().toString() }
    }

    private fun setLoading(value: Boolean) {
        _state.value = _state.value.copy(isLoading = value)
    }
}