package ec.cityalerta.app.model.data.contracts

import ec.cityalerta.app.model.data.ciudad.Ciudad

interface MapRepositoryContract {
    suspend fun getCiudadById(id: String): Ciudad?
}