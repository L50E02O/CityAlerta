package ec.cityalerta.app.model.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ReporteDao {
    @Query("SELECT * FROM reportes WHERE ciudad_id = :ciudadId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun getReportesByCiudadFlow(ciudadId: String, limit: Int, offset: Int): Flow<List<ReporteEntity>>

    @Query("SELECT COUNT(*) FROM reportes WHERE ciudad_id = :ciudadId")
    suspend fun countReportesByCiudad(ciudadId: String): Int

    @Query("SELECT * FROM reportes WHERE ciudad_id = :ciudadId ORDER BY created_at DESC")
    suspend fun getReportesByCiudad(ciudadId: String): List<ReporteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReportes(reportes: List<ReporteEntity>)

    @Query("DELETE FROM reportes WHERE ciudad_id = :ciudadId")
    suspend fun deleteReportesByCiudad(ciudadId: String)

    @Transaction
    suspend fun refreshReportes(ciudadId: String, reportes: List<ReporteEntity>) {
        deleteReportesByCiudad(ciudadId)
        insertReportes(reportes)
    }
}
