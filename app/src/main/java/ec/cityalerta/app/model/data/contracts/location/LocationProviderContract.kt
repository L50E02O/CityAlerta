package ec.cityalerta.app.model.data.contracts.location

import ec.cityalerta.app.model.data.location.UserLocation

fun interface LocationProviderContract {
    suspend fun getCurrentLocation(): Result<UserLocation>
}