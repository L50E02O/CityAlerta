package ec.cityalerta.app

import ec.cityalerta.app.model.data.perfil.PerfilResumen
import org.junit.Test
import kotlin.test.assertEquals

class PerfilResumenCoverageTest {

    @Test
    fun testPerfilResumen() {
        val resumen = PerfilResumen(
            id = "perfil-1",
            nombreCompleto = "John Doe",
            rolSlug = "ciudadano",
            activo = true,
            ciudadId = "ciudad-1",
            totalReportes = 10,
            reportesResueltos = 5
        )
        assertEquals("perfil-1", resumen.id)
        assertEquals("John Doe", resumen.nombreCompleto)
        assertEquals("ciudadano", resumen.rolSlug)
        assertEquals(true, resumen.activo)
        assertEquals("ciudad-1", resumen.ciudadId)
        assertEquals(10, resumen.totalReportes)
        assertEquals(5, resumen.reportesResueltos)
    }
}
