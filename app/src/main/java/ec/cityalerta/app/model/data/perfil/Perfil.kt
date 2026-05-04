package ec.cityalerta.app.model.data.perfil

data class Perfil(
    val id: String,
    val nombreCompleto: String,
    val rolSlug: String,
    val activo: Boolean,
    val createdAt: String? = null,
    val updatedAt: String? = null
)