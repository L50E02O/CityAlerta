package ec.cityalerta.app

import ec.cityalerta.app.model.data.MapMarker
import ec.cityalerta.app.model.data.Usuario
import ec.cityalerta.app.model.data.barrio.Barrio
import ec.cityalerta.app.model.data.barrio.BarrioCreateDto
import ec.cityalerta.app.model.data.barrio.BarrioUpdateDto
import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.data.ciudad.CiudadCreateDto
import ec.cityalerta.app.model.data.ciudad.CiudadUpdateDto
import ec.cityalerta.app.model.data.geoJson.Feature
import ec.cityalerta.app.model.data.geoJson.GeoJson
import ec.cityalerta.app.model.data.geoJson.Geometry
import ec.cityalerta.app.model.data.geoJson.Properties
import ec.cityalerta.app.model.data.location.UserLocation
import ec.cityalerta.app.model.data.perfil.Perfil
import ec.cityalerta.app.model.data.perfil.PerfilCreateDto
import ec.cityalerta.app.model.data.perfil.PerfilResumen
import ec.cityalerta.app.model.data.perfil.PerfilUpdateDto
import ec.cityalerta.app.model.data.perfilimagen.PerfilImagen
import ec.cityalerta.app.model.data.perfilimagen.PerfilImagenCreateDto
import ec.cityalerta.app.model.data.perfilimagen.PerfilImagenUpdateDto
import ec.cityalerta.app.model.data.push.PushSubscription
import ec.cityalerta.app.model.data.push.PushSubscriptionCreateDto
import ec.cityalerta.app.model.data.push.PushSubscriptionUpdateDto
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.model.data.reporte.Reporte
import ec.cityalerta.app.model.data.reporte.ReporteCreateDto
import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.data.reporte.ReporteSearchResult
import ec.cityalerta.app.model.data.reporte.ReporteUpdateDto
import ec.cityalerta.app.model.data.reporteimagen.ReporteImagen
import ec.cityalerta.app.model.data.reporteimagen.ReporteImagenCreateDto
import ec.cityalerta.app.model.data.reporteimagen.ReporteImagenUpdateDto
import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacion
import ec.cityalerta.app.model.data.reporteubicacion.ReporteUbicacionCreateDto
import ec.cityalerta.app.model.remote.AuthRedirectUrls
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
    fun barrioModels() {
        val geometry = Geometry("Polygon", listOf(listOf(listOf(-80.0, -1.0))))
        val barrio = Barrio("b1", "c1", "Centro", geometry, "now", "now")
        val create = BarrioCreateDto("c1", "Centro", geometry)
        val update = BarrioUpdateDto(nombre = "Nuevo")

        assertEquals("b1", barrio.id)
        assertEquals("Centro", create.nombre)
        assertEquals("Nuevo", update.nombre)
    }

    @Test
    fun ciudadModels() {
        val geometry = Geometry("Polygon", listOf(listOf(listOf(-80.0, -1.0))))
        val ciudad = Ciudad("c1", "Manta", "Ecuador", geometry, -1.0, -80.0, "now", "now")
        val create = CiudadCreateDto("Manta", "Ecuador", geometry, -1.0, -80.0)
        val update = CiudadUpdateDto(nombre = "Quito")

        assertEquals("c1", ciudad.id)
        assertEquals("Manta", create.nombre)
        assertEquals("Quito", update.nombre)
    }

    @Test
    fun perfilModels() {
        val perfil = Perfil("u1", "Juan", "user", true, "now", "now", "c1")
        val create = PerfilCreateDto("Juan", "user", true, "c1")
        val resumen = PerfilResumen("u1", "Juan", "user", true, "c1", 10, 5)
        val update = PerfilUpdateDto(nombreCompleto = "Nuevo")

        assertEquals("Juan", perfil.nombreCompleto)
        assertEquals("Juan", create.nombreCompleto)
        assertEquals(10, resumen.totalReportes)
        assertEquals("Nuevo", update.nombreCompleto)
    }

    @Test
    fun reporteModels() {
        val reporte = Reporte("r1", "u1", "c1", "loc1", "Desc", ReporteEstado.PENDIENTE, "2024-01-01", ReportType.BACHE, "now", "now", "b1")
        val create = ReporteCreateDto("u1", "c1", "loc1", "Desc", ReporteEstado.PENDIENTE, "2024-01-01", ReportType.BACHE, "b1")
        val update = ReporteUpdateDto(descripcion = "Nuevo")

        assertEquals("r1", reporte.id)
        assertEquals(ReportType.BACHE, create.categoria)
        assertEquals("Nuevo", update.descripcion)
    }

    @Test
    fun perfilImagenModels() {
        val imagen = PerfilImagen("i1", "u1", "uuid", "path", "now", "now")
        val create = PerfilImagenCreateDto("u1", "uuid", "path")
        val update = PerfilImagenUpdateDto(perfil_id = "u1", storage_uuid = "new_uuid", url_path = "new_path")

        assertEquals("i1", imagen.id)
        assertEquals("uuid", create.storage_uuid)
        assertEquals("new_path", update.url_path)
    }

    @Test
    fun reporteImagenModels() {
        val imagen = ReporteImagen("i1", "r1", "uuid", "path", "now", "now")
        val create = ReporteImagenCreateDto("r1", "uuid", "path")
        val update = ReporteImagenUpdateDto(reporte_id = "r1", storage_uuid = "new_uuid", url_path = "new_path")

        assertEquals("i1", imagen.id)
        assertEquals("uuid", create.storage_uuid)
        assertEquals("new_path", update.url_path)
    }

    @Test
    fun pushModels() {
        val sub = PushSubscription("s1", "u1", "token", "now", "now")
        val create = PushSubscriptionCreateDto("u1", "token")
        val update = PushSubscriptionUpdateDto(
            usuario_id = "u1",
            device_id = "device-1",
            platform = "android",
            enabled = true
        )

        assertEquals("s1", sub.id)
        assertEquals("token", create.token)
        assertEquals("device-1", update.device_id)
        assertEquals(true, update.enabled)
    }

    @Test
    fun locationModels() {
        val loc = UserLocation(-1.0, -80.0)
        assertEquals(-1.0, loc.latitude)
        assertEquals(-80.0, loc.longitude)
    }

    @Test
    fun reporteUbicacionModels() {
        val ubicacion = ReporteUbicacion("l1", -1.0, -80.0, "Calle 1", "now", "now")
        val create = ReporteUbicacionCreateDto(-1.0, -80.0, "Calle 1")

        assertEquals("l1", ubicacion.id)
        assertEquals("Calle 1", create.direccion_aproximada)
    }

    @Test
    fun reporteStatusTransitions() {
        assertEquals("PENDIENTE", ReporteEstado.PENDIENTE.name)
        assertEquals("EN_PROCESO", ReporteEstado.EN_PROCESO.name)
        assertEquals("RESUELTO", ReporteEstado.RESUELTO.name)
    }
}
