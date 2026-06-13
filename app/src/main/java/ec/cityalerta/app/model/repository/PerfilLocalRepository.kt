package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.perfil.PerfilResumen
import ec.cityalerta.app.model.local.PerfilResumenDao
import ec.cityalerta.app.model.local.PerfilResumenEntity

class PerfilLocalRepository(
    private val perfilResumenDao: PerfilResumenDao
) {

    suspend fun obtenerPerfilResumen(): PerfilResumen? {
        return perfilResumenDao.obtenerPerfilResumen()?.toPerfilResumen()
    }

    suspend fun guardarPerfilResumen(perfil: PerfilResumen) {
        perfilResumenDao.guardarPerfilResumen(perfil.toEntity())
    }

    suspend fun borrarPerfilResumen() {
        perfilResumenDao.borrarPerfilResumen()
    }

    private fun PerfilResumenEntity.toPerfilResumen(): PerfilResumen {
        return PerfilResumen(
            id = id,
            nombreCompleto = nombreCompleto,
            rolSlug = rolSlug,
            activo = activo,
            ciudadId = ciudadId,
            totalReportes = totalReportes,
            reportesResueltos = reportesResueltos
        )
    }

    private fun PerfilResumen.toEntity(): PerfilResumenEntity {
        return PerfilResumenEntity(
            id = id,
            nombreCompleto = nombreCompleto,
            rolSlug = rolSlug,
            activo = activo,
            ciudadId = ciudadId,
            totalReportes = totalReportes,
            reportesResueltos = reportesResueltos
        )
    }
}
