package ec.cityalerta.app.model.data.push

data class PushSubscriptionUpdateDto(
    val usuario_id: String? = null,
    val device_id: String? = null,
    val platform: String? = null,
    val enabled: Boolean? = null
)
