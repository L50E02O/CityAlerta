package ec.cityalerta.app.testdoubles

import ec.cityalerta.app.model.data.map.BarrioRiskState
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacion
import ec.cityalerta.app.model.utils.IRiskZoneDetector

/** Detector sin trabajo en background para tests que usan StandardTestDispatcher. */
object NoOpRiskZoneDetector : IRiskZoneDetector {
    override fun detectRiskZones(
        reports: List<Reporte>,
        ubicaciones: Map<String, ReporteUbicacion>
    ): List<BarrioRiskState> = emptyList()
}
