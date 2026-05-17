package ec.cityalerta.app.model.data.perfil

data class PerfilUpdateDto(
    val nombreCompleto: String? = null,
    val rolSlug: String? = null,
    val activo: Boolean? = null,
    val ciudadId: String? = null
)

