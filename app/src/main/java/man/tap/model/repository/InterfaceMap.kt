package man.tap.model.repository

import man.tap.model.data.Ciudad
import man.tap.model.data.MapMarker

interface IMapRepository {
    fun getCiudadById(id: String): Ciudad?
    fun addMarker(marker: MapMarker)
    fun removeMarker(markerId: String)
    fun getMarkers(): List<MapMarker>
    fun clearMarkers()
}

