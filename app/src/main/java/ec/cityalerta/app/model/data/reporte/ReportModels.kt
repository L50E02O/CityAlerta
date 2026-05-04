package ec.cityalerta.app.model.data.reporte

enum class ReportType {
    RISK_ZONE, POTHOLE, WATER, LIGHT
}

data class Report(
    val id: String,
    val type: ReportType,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val description: String
)