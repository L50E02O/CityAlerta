package ec.cityalerta.app.model.data

data class Barrio(
    val id: String,
    val ciudadId: String,
    val nombre: String,
    val nivelPeligrosidad: String,
    val perimetro: Geometry
)
