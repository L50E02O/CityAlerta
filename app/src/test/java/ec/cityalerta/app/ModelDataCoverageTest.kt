package ec.cityalerta.app

import ec.cityalerta.app.model.data.MapMarker
import ec.cityalerta.app.model.data.Usuario
import ec.cityalerta.app.model.data.ciudad.CiudadUpdateDto
import ec.cityalerta.app.model.data.geoJson.Feature
import ec.cityalerta.app.model.data.geoJson.GeoJson
import ec.cityalerta.app.model.data.geoJson.Geometry
import ec.cityalerta.app.model.data.geoJson.Properties
import ec.cityalerta.app.model.data.perfil.PerfilUpdateDto
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.data.reporte.ReporteUpdateDto
import ec.cityalerta.app.model.remote.AuthRedirectUrls
import ec.cityalerta.app.model.data.reporte.ReporteSearchResult
import ec.cityalerta.app.testdoubles.SearchReportTestFixtures
import ec.cityalerta.app.viewmodel.ExploreState
import ec.cityalerta.app.viewmodel.ReporteUI
import ec.cityalerta.app.viewmodel.SearchReportState
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ModelDataCoverageTest {

    @Test
    fun usuarioDataClass() {
        val usuario = Usuario(id = "u1", email = "test@example.com")
        assertEquals("u1", usuario.id)
        assertEquals("test@example.com", usuario.email)
    }

    @Test
    fun mapMarkerDataClass() {
        val marker = MapMarker(
            id = "m1",
            latitude = -1.0,
            longitude = -80.0,
            title = "Titulo",
            description = "Descripcion"
        )
        assertEquals("m1", marker.id)
        assertEquals("Titulo", marker.title)
    }

    @Test
    fun reportTypeDisplayNames() {
        assertEquals("Zona de riesgo", ReportType.ZONA_DE_RIESGO.toDisplayName())
        assertEquals("Bache", ReportType.BACHE.toDisplayName())
        assertEquals("Agua", ReportType.AGUA.toDisplayName())
        assertEquals("Luz", ReportType.LUZ.toDisplayName())
    }

    @Test
    fun exploreStateDefaults() {
        val state = ExploreState()
        assertEquals("Cargando...", state.ciudadNombre)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun searchReportStateDefaults() {
        val state = SearchReportState()
        assertEquals("", state.searchQuery)
        assertEquals("", state.ciudadId)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun reporteSearchResultDataClass() {
        val result = SearchReportTestFixtures.sampleSearchResult()
        assertEquals("Centro", result.barrioNombre)
        assertNotNull(result.reporte)
    }

    @Test
    fun reporteUiDataClass() {
        val ui = ReporteUI(
            id = "r1",
            categoria = "Bache",
            categoryType = ReportType.BACHE,
            imageUrl = null,
            barrio = "Centro",
            direccion = "Calle 1",
            descripcion = "Detalle",
            estado = "Pendiente",
            fecha = "2024-01-01",
            timeAgo = "Hace 1 hora"
        )
        assertEquals("r1", ui.id)
        assertEquals("Bache", ui.categoria)
    }

    @Test
    fun authRedirectUrlsConstant() {
        assertEquals("cityalerta://auth", AuthRedirectUrls.APP_DEEP_LINK)
    }

    @Test
    fun geoJsonModels() {
        val geometry = Geometry("Polygon", listOf(listOf(listOf(-80.0, -1.0))))
        val feature = Feature("Feature", Properties("Manta", "EC"), geometry)
        val geoJson = GeoJson("FeatureCollection", listOf(feature))

        assertEquals("FeatureCollection", geoJson.type)
        assertEquals(1, geoJson.features.size)
        assertEquals("Manta", geoJson.features.first().properties.name)
    }

    @Test
    fun partialUpdateDtos() {
        val ciudadUpdate = CiudadUpdateDto(
            nombre = "Quito",
            pais = "Ecuador",
            geojson = null,
            centroLat = -0.22,
            centroLng = -78.51
        )
        val perfilUpdate = PerfilUpdateDto(nombreCompleto = "Juan Perez", ciudadId = "c1")
        val reporteUpdate = ReporteUpdateDto(
            usuario_id = "u1",
            ciudad_id = "c1",
            ubicacion_id = "loc1",
            descripcion = "Actualizado",
            estado = ReporteEstado.EN_PROCESO,
            fecha_reporte = "2024-02-01",
            categoria = ReportType.AGUA,
            barrio_id = "b1"
        )

        assertNotNull(ciudadUpdate)
        assertEquals("Juan Perez", perfilUpdate.nombreCompleto)
        assertEquals(ReporteEstado.EN_PROCESO, reporteUpdate.estado)
    }
}
