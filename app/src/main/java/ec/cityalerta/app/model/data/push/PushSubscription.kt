package ec.cityalerta.app.model.data.push

data class PushSubscription(
    val id: String,
    val usuario_id: String,
    val token: String,
    val device_id: String? = null,
    val platform: String? = null,
    val enabled: Boolean = true,
    val created_at: String? = null,
    val updated_at: String? = null
)
