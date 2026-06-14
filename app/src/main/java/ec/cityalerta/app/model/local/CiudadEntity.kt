package ec.cityalerta.app.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import ec.cityalerta.app.model.data.geoJson.Geometry

@Entity(tableName = "ciudades")
data class CiudadEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val pais: String,
    val geojson: Geometry,
    val centroLat: Double,
    val centroLng: Double
)
