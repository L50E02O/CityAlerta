package ec.cityalerta.app.model.data.perfilimagen

data class PerfilImagen(
    val id: String,
    val perfil_id: String,
    val storage_uuid: String,
    val url_path: String,
    val created_at: String? = null,
    val updated_at: String? = null
)
