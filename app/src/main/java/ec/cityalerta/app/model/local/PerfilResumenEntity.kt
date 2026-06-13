package ec.cityalerta.app.model.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "perfil_resumen")
data class PerfilResumenEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "nombre_completo") val nombreCompleto: String,
    @ColumnInfo(name = "rol_slug") val rolSlug: String,
    @ColumnInfo(name = "activo") val activo: Boolean,
    @ColumnInfo(name = "ciudad_id") val ciudadId: String,
    @ColumnInfo(name = "total_reportes") val totalReportes: Int,
    @ColumnInfo(name = "reportes_resueltos") val reportesResueltos: Int
)
