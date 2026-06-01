package ec.cityalerta.app.model.remote.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import ec.cityalerta.app.model.data.contracts.notifications.NotificationHandlerContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

	private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
	private lateinit var notificationHandler: NotificationHandlerContract

	override fun onCreate() {
		super.onCreate()
		notificationHandler = AndroidNotificationHandler(applicationContext)
	}

	override fun onMessageReceived(message: RemoteMessage) {
		super.onMessageReceived(message)

		message.notification?.let {
			notificationHandler.showNotification(it.title ?: "CityAlerta", it.body ?: "", message.data)
		} ?: run {
			if (message.data.isNotEmpty()) {
				val title = message.data["title"] ?: "CityAlerta"
				val body = message.data["body"] ?: "Nuevo reporte recibido"
				notificationHandler.showNotification(title, body, message.data)
			}
		}
	}

	override fun onNewToken(token: String) {
		super.onNewToken(token)

		val registrar = PushSubscriptionRegistrar.createDefault(applicationContext)
		serviceScope.launch {
			registrar.cacheToken(token)
			val result = registrar.registerTokenIfAllowed()
			result.onSuccess {
			}.onFailure { error ->
				if (
					error is PushSubscriptionAuthRequiredException ||
					error is PushSubscriptionDisabledException ||
					error is PushSubscriptionTokenMissingException
				) {
					return@onFailure
				}
			}
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		serviceScope.cancel()
	}
}