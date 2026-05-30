package ec.cityalerta.app.model.remote.service

import com.google.firebase.messaging.FirebaseMessagingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

	private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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