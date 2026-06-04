package ec.cityalerta.app.testdoubles

import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacion
import ec.cityalerta.app.model.repository.BarrioRepository
import ec.cityalerta.app.model.repository.ReporteRepository
import ec.cityalerta.app.model.repository.ReporteUbicacionRepository
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito

object RepositoryMockHelpers {

    fun barrioRepository(): BarrioRepository = Mockito.mock(BarrioRepository::class.java)

    fun reporteRepositoryReturning(reportes: List<Reporte>): ReporteRepository {
        val mock = Mockito.mock(ReporteRepository::class.java)
        runBlocking {
            Mockito.doReturn(Result.success(reportes)).`when`(mock).getAll()
        }
        return mock
    }

    fun reporteRepositoryFailing(message: String): ReporteRepository {
        val mock = Mockito.mock(ReporteRepository::class.java)
        runBlocking {
            Mockito.doReturn(Result.failure<List<Reporte>>(RuntimeException(message)))
                .`when`(mock)
                .getAll()
        }
        return mock
    }

    fun ubicacionRepositoryReturning(ubicaciones: List<ReporteUbicacion>): ReporteUbicacionRepository {
        val mock = Mockito.mock(ReporteUbicacionRepository::class.java)
        runBlocking {
            Mockito.doReturn(Result.success(ubicaciones)).`when`(mock).getAll()
        }
        return mock
    }

    fun ubicacionRepositoryFailing(message: String): ReporteUbicacionRepository {
        val mock = Mockito.mock(ReporteUbicacionRepository::class.java)
        runBlocking {
            Mockito.doReturn(Result.failure<List<ReporteUbicacion>>(RuntimeException(message)))
                .`when`(mock)
                .getAll()
        }
        return mock
    }
}
