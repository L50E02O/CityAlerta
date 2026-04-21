package man.tap.model.repository

import man.tap.model.data.Ciudad
import man.tap.model.data.MapMarker
import man.tap.model.data.MockData

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

