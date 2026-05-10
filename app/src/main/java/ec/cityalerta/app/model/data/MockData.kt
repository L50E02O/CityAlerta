package ec.cityalerta.app.model.data

import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.data.geoJson.Geometry
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporte.ReporteEstado

/**
 * Mock Data para pruebas de la app.
 * Contiene datos hardcodeados que solo existen en memoria durante la ejecucion.
 */
object MockData {

    /**
     * Retorna la ciudad de Manta, Ecuador con su geometria incluida.
     */
    fun getMantaCity(): Ciudad {
        val mantaGeometry = Geometry(
            type = "Polygon",
            coordinates = listOf(
                listOf(
                    listOf(-80.66848, -0.932),
                    listOf(-80.69038, -0.94677),
                    listOf(-80.71445, -0.94924),
                    listOf(-80.72347, -0.94391),
                    listOf(-80.71703865315638, -0.928708520214311),
                    listOf(-80.72270297675826, -0.9290190768120428),
                    listOf(-80.73644, -0.94132),
                    listOf(-80.74957648003932, -0.9430565735046786),
                    listOf(-80.76223608024462, -0.9423168922701216),
                    listOf(-80.76685, -0.94535),
                    listOf(-80.78409, -0.95702),
                    listOf(-80.80525, -0.95948),
                    listOf(-80.80512605387447, -0.9529107485190997),
                    listOf(-80.81534092716709, -0.9520667285824195),
                    listOf(-80.82172, -0.95529),
                    listOf(-80.83163265987466, -0.957007783342198),
                    listOf(-80.83818, -0.97268),
                    listOf(-80.85683, -0.99276),
                    listOf(-80.86828615133228, -1.0015486418915132),
                    listOf(-80.87927, -1.02025),
                    listOf(-80.88983195851365, -1.0323631979935328),
                    listOf(-80.90345643833402, -1.036274581501719),
                    listOf(-80.90588739020465, -1.0514893367311218),
                    listOf(-80.91273, -1.05867),
                    listOf(-80.898, -1.09801),
                    listOf(-80.8837, -1.13641),
                    listOf(-80.84326, -1.1262),
                    listOf(-80.82196, -1.11416),
                    listOf(-80.78055, -1.07243),
                    listOf(-80.7727, -1.03896),
                    listOf(-80.75799, -1.03832),
                    listOf(-80.74656, -1.03656),
                    listOf(-80.73523, -1.01642),
                    listOf(-80.72663, -1.01682),
                    listOf(-80.71799, -1.01055),
                    listOf(-80.71399, -0.99072),
                    listOf(-80.70281, -0.9935),
                    listOf(-80.69464, -0.98624),
                    listOf(-80.68264, -0.97719),
                    listOf(-80.67212, -0.96352),
                    listOf(-80.66808, -0.95842),
                    listOf(-80.66551, -0.95237),
                    listOf(-80.6624, -0.94781),
                    listOf(-80.66029, -0.94281),
                    listOf(-80.65814, -0.93524),
                    listOf(-80.66129, -0.93061),
                    listOf(-80.66848, -0.932)
                )
            )
        )

        return Ciudad(
            id = "manta",
            nombre = "Manta",
            pais = "Ecuador",
            geojson = mantaGeometry,
            centroLat = -0.95,
            centroLng = -80.73
        )
    }

//    fun getMockReports(): List<Reporte> {
//        return listOf(
//            Reporte(
//                id = "reporte_1",
//                usuario_id = "perfil_1",
//                ciudad_id = "manta",
//                ubicacionId = "ubicacion_1",
//                descripcion = "Hecho violento en via San Mateo",
//                estado = ReporteEstado.PENDIENTE,
//                fechaReporte = "2026-05-05T08:15:00Z",
//                categoria = ReportType.ZONA_DE_RIESGO
//            ),
//            Reporte(
//                id = "reporte_2",
//                usuarioId = "perfil_1",
//                ciudadId = "manta",
//                ubicacionId = "ubicacion_2",
//                descripcion = "Fallo de infraestructura electrica en via San Mateo",
//                estado = ReporteEstado.EN_PROCESO,
//                fechaReporte = "2026-05-05T09:00:00Z",
//                categoria = ReportType.LUZ
//            ),
//            Reporte(
//                id = "reporte_3",
//                usuarioId = "perfil_2",
//                ciudadId = "manta",
//                ubicacionId = "ubicacion_3",
//                descripcion = "Bache detectado en via San Mateo",
//                estado = ReporteEstado.PENDIENTE,
//                fechaReporte = "2026-05-05T09:25:00Z",
//                categoria = ReportType.BACHE
//            ),
//            Reporte(
//                id = "reporte_4",
//                usuarioId = "perfil_2",
//                ciudadId = "manta",
//                ubicacionId = "ubicacion_4",
//                descripcion = "Fallo en sistema de agua en via San Mateo",
//                estado = ReporteEstado.RESUELTO,
//                fechaReporte = "2026-05-05T10:10:00Z",
//                categoria = ReportType.AGUA
//            )
//        )
//    }

    fun getMockReportMarkers(): List<MapMarker> {
        return listOf(
            MapMarker(
                id = "reporte_1",
                latitude = -0.947,
                longitude = -80.735,
                title = "Zona de riesgo en via San Mateo",
                description = "Hecho violento en via San Mateo"
            ),
            MapMarker(
                id = "reporte_2",
                latitude = -0.990,
                longitude = -80.800,
                title = "Poste caido en via San Mateo",
                description = "Fallo de infraestructura electrica en via San Mateo"
            ),
            MapMarker(
                id = "reporte_3",
                latitude = -0.947,
                longitude = -80.715,
                title = "Bache en via San Mateo",
                description = "Bache detectado en via San Mateo"
            ),
            MapMarker(
                id = "reporte_4",
                latitude = -0.983,
                longitude = -80.701,
                title = "Fuga de agua en via San Mateo",
                description = "Fallo en sistema de agua en via San Mateo"
            )
        )
    }
}
