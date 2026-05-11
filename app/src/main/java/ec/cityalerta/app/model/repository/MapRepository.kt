package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.repository.interfaces.IMapRepository

class MapRepository : IMapRepository {

    private val ciudadRepository = CiudadRepository()

    override suspend fun getCiudadById(id: String): Ciudad? {
        return ciudadRepository.getById(id).getOrNull()
    }
}
