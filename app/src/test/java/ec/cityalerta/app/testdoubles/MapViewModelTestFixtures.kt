package ec.cityalerta.app.testdoubles

import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.data.geoJson.Geometry
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacion

object MapViewModelTestFixtures {

    const val CIUDAD_UUID = "12345678-1234-1234-1234-123456789012"

    fun sampleGeometry(): Geometry = Geometry(
        type = "Polygon",
        coordinates = listOf(
            listOf(
                listOf(-80.73, -1.04),
                listOf(-80.72, -1.04),
                listOf(-80.72, -1.05),
                listOf(-80.73, -1.05)
            )
        )
    )

    fun sampleCiudad(id: String = CIUDAD_UUID, nombre: String = "Manta"): Ciudad = Ciudad(
        id = id,
        nombre = nombre,
        pais = "Ecuador",
        geojson = sampleGeometry(),
        centroLat = -1.04,
        centroLng = -80.73
    )

    fun sampleReporte(
        id: String = "reporte-1",
        ciudadId: String = CIUDAD_UUID,
        ubicacionId: String = "ubicacion-1",
        categoria: ReportType = ReportType.BACHE
    ): Reporte = Reporte(
        id = id,
        usuario_id = "user-1",
        ciudad_id = ciudadId,
        ubicacion_id = ubicacionId,
        descripcion = "Bache en la via",
        estado = ReporteEstado.PENDIENTE,
        fecha_reporte = "2024-06-01",
        categoria = categoria,
        barrio_id = "barrio-1"
    )

    fun sampleUbicacion(
        id: String = "ubicacion-1",
        lat: Double = -1.04,
        lng: Double = -80.73
    ): ReporteUbicacion = ReporteUbicacion(
        id = id,
        lat = lat,
        lng = lng,
        direccion_aproximada = "Av. Principal"
    )
}
