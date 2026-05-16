package ec.cityalerta.app.model.data.contracts.map

import ec.cityalerta.app.model.data.ciudad.Ciudad

interface MapRepositoryContract {
    suspend fun getCiudadById(id: String): Ciudad?
}