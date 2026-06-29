package ec.cityalerta.app.model.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PerfilResumenDao {
    @Query("SELECT * FROM perfil_resumen LIMIT 1")
    suspend fun obtenerPerfilResumen(): PerfilResumenEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarPerfilResumen(perfil: PerfilResumenEntity)

    @Query("DELETE FROM perfil_resumen")
    suspend fun borrarPerfilResumen()
}
