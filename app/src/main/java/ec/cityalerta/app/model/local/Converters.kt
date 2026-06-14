package ec.cityalerta.app.model.local

import androidx.room.TypeConverter
import ec.cityalerta.app.model.data.geoJson.Geometry
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromGeometry(value: Geometry?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toGeometry(value: String?): Geometry? {
        return value?.let { Json.decodeFromString<Geometry>(it) }
    }

    @TypeConverter
    fun fromReportType(value: ReportType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toReportType(value: String?): ReportType? {
        return value?.let { ReportType.valueOf(it) }
    }

    @TypeConverter
    fun fromReporteEstado(value: ReporteEstado?): String? {
        return value?.name
    }

    @TypeConverter
    fun toReporteEstado(value: String?): ReporteEstado? {
        return value?.let { ReporteEstado.valueOf(it) }
    }
}
