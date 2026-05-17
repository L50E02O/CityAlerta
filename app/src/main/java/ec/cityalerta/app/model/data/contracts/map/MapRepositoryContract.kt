package ec.cityalerta.app.model.data.contracts.map

import ec.cityalerta.app.model.data.ciudad.Ciudad

fun interface MapRepositoryContract {
    suspend fun getCiudadById(id: String): Ciudad?
}