package ec.cityalerta.app.model.repository

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import ec.cityalerta.app.model.data.location.UserLocation
import ec.cityalerta.app.model.repository.interfaces.ILocationProvider
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocationRepository(context: Context) : ILocationProvider {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Result<UserLocation> = suspendCoroutine { cont ->
        client.lastLocation
            .addOnSuccessListener { location ->
                if (location == null) {
                    cont.resume(Result.failure(Exception("Ubicacion no disponible")))
                } else {
                    cont.resume(
                        Result.success(
                            UserLocation(
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        )
                    )
                }
            }
            .addOnFailureListener { error ->
                cont.resume(Result.failure(error))
            }
    }
}