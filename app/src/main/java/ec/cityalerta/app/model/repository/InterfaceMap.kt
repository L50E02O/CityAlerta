package ec.cityalerta.app.model.repository

import ec.cityalerta.app.model.data.Ciudad
import ec.cityalerta.app.model.data.MapMarker

interface IMapRepository {
    fun getCiudadById(id: String): Ciudad?
    fun addMarker(marker: MapMarker)
    fun removeMarker(markerId: String)
    fun getMarkers(): List<MapMarker>
    fun clearMarkers()
}

