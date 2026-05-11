package ec.cityalerta.app.model.repository.interfaces

import ec.cityalerta.app.model.data.ciudad.Ciudad

interface IMapRepository {
    suspend fun getCiudadById(id: String): Ciudad?
}
