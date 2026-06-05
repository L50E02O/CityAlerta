package ec.cityalerta.app.model.data.contracts.notifications

interface NotificationHandlerContract {
    fun showNotification(title: String, body: String, data: Map<String, String> = emptyMap())
}
