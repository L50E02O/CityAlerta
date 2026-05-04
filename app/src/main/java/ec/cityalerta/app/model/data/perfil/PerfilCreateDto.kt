package ec.cityalerta.app.model.data.perfil

data class PerfilCreateDto(
    val nombreCompleto: String,
    val rolSlug: String,
    val activo: Boolean
)

