package ec.cityalerta.app.model.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReporteDao {
    @Query("SELECT * FROM reportes WHERE ciudad_id = :ciudadId ORDER BY created_at DESC")
    suspend fun getReportesByCiudad(ciudadId: String): List<ReporteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReportes(reportes: List<ReporteEntity>)

    @Query("DELETE FROM reportes WHERE ciudad_id = :ciudadId")
    suspend fun deleteReportesByCiudad(ciudadId: String)
}
