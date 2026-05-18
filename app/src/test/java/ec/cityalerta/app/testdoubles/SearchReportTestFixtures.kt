package ec.cityalerta.app.testdoubles

import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.data.reporte.ReporteSearchResult
import ec.cityalerta.app.model.data.reporteimagen.ReporteImagen

object SearchReportTestFixtures {

    const val CIUDAD_ID = "550e8400-e29b-41d4-a716-446655440000"

    fun sampleReporte(
        id: String = "reporte-1",
        categoria: ReportType = ReportType.BACHE,
        createdAt: String? = "2024-06-01T12:00:00Z"
    ): Reporte = Reporte(
        id = id,
        usuario_id = "user-1",
        ciudad_id = CIUDAD_ID,
        ubicacion_id = "ubicacion-1",
        descripcion = "Bache en la calle",
        estado = ReporteEstado.PENDIENTE,
        fecha_reporte = "2024-06-01",
        categoria = categoria,
        created_at = createdAt,
        barrio_id = "barrio-1"
    )

    fun sampleSearchResult(
        reporte: Reporte = sampleReporte(),
        barrioNombre: String = "Centro",
        direccionAproximada: String? = "Av. Principal 123"
    ): ReporteSearchResult = ReporteSearchResult(
        reporte = reporte,
        barrioNombre = barrioNombre,
        direccionAproximada = direccionAproximada
    )

    fun sampleImagen(
        reporteId: String = "reporte-1",
        storageUuid: String = "storage-uuid-1"
    ): ReporteImagen = ReporteImagen(
        id = "imagen-1",
        reporte_id = reporteId,
        storage_uuid = storageUuid,
        url_path = "/path/imagen.jpg"
    )
}
