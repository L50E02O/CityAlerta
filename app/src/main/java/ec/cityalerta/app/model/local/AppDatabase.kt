package ec.cityalerta.app.model.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PerfilResumenEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun perfilResumenDao(): PerfilResumenDao
}
