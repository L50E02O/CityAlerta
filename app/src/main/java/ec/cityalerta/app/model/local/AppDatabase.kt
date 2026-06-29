package ec.cityalerta.app.model.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        PerfilResumenEntity::class,
        CiudadEntity::class,
        ReporteEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun perfilResumenDao(): PerfilResumenDao
    abstract fun ciudadDao(): CiudadDao
    abstract fun reporteDao(): ReporteDao
}
