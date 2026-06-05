package ec.cityalerta.app.model.data.push

data class PushSubscriptionCreateDto(
    val usuario_id: String,
    val token: String,
    val device_id: String? = null,
    val platform: String? = null,
    val enabled: Boolean = true
)
