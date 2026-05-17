package ec.cityalerta.app.model.data.perfil

data class PerfilResumen(
    val id: String,
    val nombreCompleto: String,
    val rolSlug: String,
    val activo: Boolean,
    val ciudadId: String,
    val totalReportes: Int,
    val reportesResueltos: Int
)