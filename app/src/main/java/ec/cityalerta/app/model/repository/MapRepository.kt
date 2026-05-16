package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.data.contracts.map.MapRepositoryContract

class MapRepository : MapRepositoryContract {

    private val ciudadRepository = CiudadRepository()

    override suspend fun getCiudadById(id: String): Ciudad? {
        return ciudadRepository.getById(id).getOrNull()
    }
}
