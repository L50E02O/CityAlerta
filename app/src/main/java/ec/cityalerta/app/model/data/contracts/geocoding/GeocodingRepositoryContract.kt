package ec.cityalerta.app.model.data.contracts.geocoding

fun interface GeocodingRepositoryContract {
    suspend fun reverseGeocode(lat: Double, lng: Double): Result<String>
}
