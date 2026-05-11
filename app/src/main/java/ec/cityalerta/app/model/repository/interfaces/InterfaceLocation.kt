package ec.cityalerta.app.model.repository.interfaces

import ec.cityalerta.app.model.data.location.UserLocation

interface ILocationProvider {
    suspend fun getCurrentLocation(): Result<UserLocation>
}