package ec.cityalerta.app.model.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CiudadDao {
    @Query("SELECT * FROM ciudades WHERE pais = :pais")
    suspend fun getCiudadesByPais(pais: String): List<CiudadEntity>

    @Query("SELECT * FROM ciudades WHERE id = :id")
    suspend fun getCiudadById(id: String): CiudadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCiudades(ciudades: List<CiudadEntity>)

    @Query("DELETE FROM ciudades")
    suspend fun deleteAll()
}
