package ec.cityalerta.app.model.data.reporte

enum class ReportType {
    ZONA_DE_RIESGO,
    BACHE,
    AGUA,
    LUZ;

    fun toDisplayName(): String{
        return when(this){
            ZONA_DE_RIESGO -> "Zona de riesgo"
            BACHE -> "Bache"
            AGUA -> "Agua"
            LUZ -> "Luz"
        }
    }
}