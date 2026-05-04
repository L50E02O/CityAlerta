package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.data.MapMarker
import ec.cityalerta.app.model.data.MockData
import ec.cityalerta.app.model.repository.interfaces.IMapRepository

class MapRepository : IMapRepository {

    private val markers = mutableListOf<MapMarker>()

    override fun getCiudadById(id: String): Ciudad? {
        return when (id) {
            "manta" -> MockData.getMantaCity()
            else -> null
        }
    }

    override fun addMarker(marker: MapMarker) {
        markers.add(marker)
    }

    override fun removeMarker(markerId: String) {
        markers.removeAll { it.id == markerId }
    }

    override fun getMarkers(): List<MapMarker> {
        return markers.toList()
    }

    override fun clearMarkers() {
        markers.clear()
    }
}

