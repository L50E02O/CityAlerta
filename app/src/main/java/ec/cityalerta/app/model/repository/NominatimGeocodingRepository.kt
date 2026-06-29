package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.contracts.geocoding.GeocodingRepositoryContract
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Normalizer
import kotlin.coroutines.cancellation.CancellationException

class NominatimGeocodingRepository : GeocodingRepositoryContract {

    companion object {
        private val httpClient = HttpClient(Android)
    }

    override suspend fun reverseGeocode(lat: Double, lng: Double): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=$lat&lon=$lng&addressdetails=1"
            val response: HttpResponse = httpClient.get(url) {
                header("User-Agent", "CityAlerta/1.0")
                header("Accept-Language", "es")
            }

            val body = response.bodyAsText()
            if (!response.status.isSuccess()) {
                return@withContext Result.failure(Exception("Error de geocodificacion (${response.status.value})"))
            }

            val jsonObject = Json.parseToJsonElement(body).jsonObject
            val address = jsonObject["address"]?.jsonObject

            val displayName = jsonObject["display_name"]?.jsonPrimitive?.content.orEmpty()

            val road = address?.get("road")?.jsonPrimitive?.content.orEmpty()
            val houseNumber = address?.get("house_number")?.jsonPrimitive?.content.orEmpty()
            val suburb = address?.get("suburb")?.jsonPrimitive?.content.orEmpty()
            val neighbourhood = address?.get("neighbourhood")?.jsonPrimitive?.content.orEmpty()
            val city = address?.get("city")?.jsonPrimitive?.content.orEmpty()
            val town = address?.get("town")?.jsonPrimitive?.content.orEmpty()
            val village = address?.get("village")?.jsonPrimitive?.content.orEmpty()

            val line = when {
                road.isNotBlank() && houseNumber.isNotBlank() -> "$road $houseNumber"
                road.isNotBlank() -> road
                neighbourhood.isNotBlank() -> neighbourhood
                suburb.isNotBlank() -> suburb
                city.isNotBlank() -> city
                town.isNotBlank() -> town
                village.isNotBlank() -> village
                displayName.isNotBlank() -> displayName
                else -> "Direccion no disponible"
            }

            Result.success(removeAccents(line))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun removeAccents(value: String): String {
        val normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
        return normalized.replace("\\p{M}+".toRegex(), "")
    }
}
