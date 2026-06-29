package ec.cityalerta.app.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.ReporteEstado

@Entity(tableName = "reportes")
data class ReporteEntity(
    @PrimaryKey val id: String,
    val usuario_id: String,
    val ciudad_id: String,
    val ubicacion_id: String,
    val descripcion: String,
    val estado: ReporteEstado,
    val fecha_reporte: String,
    val categoria: ReportType,
    val created_at: String?,
    val updated_at: String?,
    val barrio_id: String,
    // Campos adicionales para UI en cache
    val barrio_nombre: String? = null,
    val direccion_aproximada: String? = null,
    val image_url: String? = null
)
