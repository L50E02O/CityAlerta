package ec.cityalerta.app.model.data

data class Usuario(
    val id: String,
    val nombreCompleto: String,
    val email: String,
    val passwordHash: String,
    val rolSlug: String,
    val activo: Boolean
)
