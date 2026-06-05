package ec.cityalerta.app

import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.data.reporte.ReportType
import kotlin.coroutines.cancellation.CancellationException
import ec.cityalerta.app.model.utils.booleanOrFalse
import ec.cityalerta.app.model.utils.doubleOrZero
import ec.cityalerta.app.model.utils.nullableString
import ec.cityalerta.app.model.utils.stringOrEmpty
import ec.cityalerta.app.model.utils.toReporteEstadoOrDefault
import ec.cityalerta.app.model.utils.toReportTypeOrDefault
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests unitarios para las utilidades de Supabase CRUD. Valida las funciones
 * de conversion y transformacion de tipos JSON.
 */
class SupabaseCrudUtilsTest {

    @Test
    fun testSafeSupabaseCallSuccess() = kotlinx.coroutines.test.runTest {
        val result = ec.cityalerta.app.model.utils.safeSupabaseCall { "success" }
        assertTrue(result.isSuccess)
        assertEquals("success", result.getOrNull())
    }

    @Test
    fun testSafeSupabaseCallFailure() = kotlinx.coroutines.test.runTest {
        val exception = RuntimeException("error")
        val result = ec.cityalerta.app.model.utils.safeSupabaseCall { throw exception }
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun testSafeSupabaseCallRethrowsCancellation() = kotlinx.coroutines.test.runTest {
        val cancellation = CancellationException("cancelled")
        try {
            ec.cityalerta.app.model.utils.safeSupabaseCall<String> { throw cancellation }
            assertTrue(false, "Debe relanzar CancellationException")
        } catch (e: CancellationException) {
            assertEquals(cancellation, e)
        }
    }

    @Test
    fun testToReportTypeOrDefaultReturnsMatch() {
        val json = JsonObject(mapOf("categoria" to JsonPrimitive("BACHE")))
        assertEquals(ReportType.BACHE, json.toReportTypeOrDefault("categoria"))
    }

    @Test
    fun testToReportTypeOrDefaultReturnsFallback() {
        val json = JsonObject(mapOf("categoria" to JsonPrimitive("DESCONOCIDO")))
        assertEquals(ReportType.ZONA_DE_RIESGO, json.toReportTypeOrDefault("categoria"))
    }

    @Test
    fun testStringOrEmptyReturnsValueWhenPresent() {
        val json = JsonObject(mapOf("name" to JsonPrimitive("Juan")))
        val result = json.stringOrEmpty("name")
        assertEquals("Juan", result)
    }

    @Test
    fun testStringOrEmptyReturnsEmptyWhenMissing() {
        val json = JsonObject(mapOf())
        val result = json.stringOrEmpty("name")
        assertEquals("", result)
    }

    @Test
    fun testDoubleOrZeroReturnsValueWhenPresent() {
        val json = JsonObject(mapOf("lat" to JsonPrimitive(10.5)))
        val result = json.doubleOrZero("lat")
        assertEquals(10.5, result)
    }

    @Test
    fun testDoubleOrZeroReturnsZeroWhenMissing() {
        val json = JsonObject(mapOf())
        val result = json.doubleOrZero("lat")
        assertEquals(0.0, result)
    }

    @Test
    fun testBooleanOrFalseReturnsTrueWhenTrue() {
        val json = JsonObject(mapOf("activo" to JsonPrimitive(true)))
        val result = json.booleanOrFalse("activo")
        assertTrue(result)
    }

    @Test
    fun testBooleanOrFalseReturnsFalseWhenFalse() {
        val json = JsonObject(mapOf("activo" to JsonPrimitive(false)))
        val result = json.booleanOrFalse("activo")
        assertFalse(result)
    }

    @Test
    fun testNullableStringReturnsValueWhenPresent() {
        val json = JsonObject(mapOf("email" to JsonPrimitive("test@example.com")))
        val result = json.nullableString("email")
        assertEquals("test@example.com", result)
    }

    @Test
    fun testNullableStringReturnsNullWhenMissing() {
        val json = JsonObject(mapOf())
        val result = json.nullableString("email")
        assertNull(result)
    }

    @Test
    fun testToReporteEstadoOrDefaultReturnsMatches() {
        val jsonPendiente = JsonObject(mapOf("estado_slug" to JsonPrimitive("PENDIENTE")))
        assertEquals(ReporteEstado.PENDIENTE, jsonPendiente.toReporteEstadoOrDefault("estado_slug"))

        val jsonResuelto = JsonObject(mapOf("estado_slug" to JsonPrimitive("RESUELTO")))
        assertEquals(ReporteEstado.RESUELTO, jsonResuelto.toReporteEstadoOrDefault("estado_slug"))
    }

    @Test
    fun testToReporteEstadoOrDefaultReturnsFallback() {
        val json = JsonObject(mapOf("estado_slug" to JsonPrimitive("INVALIDO")))
        assertEquals(ReporteEstado.PENDIENTE, json.toReporteEstadoOrDefault("estado_slug"))
    }

    @Test
    fun testGeoJsonToJsonConversion() {
        val geometry = ec.cityalerta.app.model.data.geoJson.Geometry(
            type = "Polygon",
            coordinates = listOf(listOf(listOf(-80.0, -1.0), listOf(-81.0, -2.0)))
        )
        val properties = ec.cityalerta.app.model.data.geoJson.Properties(name = "Test", country = "EC")
        val feature = ec.cityalerta.app.model.data.geoJson.Feature(type = "Feature", properties = properties, geometry = geometry)
        val geoJson = ec.cityalerta.app.model.data.geoJson.GeoJson(type = "FeatureCollection", features = listOf(feature))

        val result = ec.cityalerta.app.model.utils.geoJsonToJson(geoJson)

        assertEquals("FeatureCollection", result["type"]?.jsonPrimitive?.content)
        val features = result["features"]?.jsonArray
        assertEquals(1, features?.size)
    }

    @Test
    fun testGeoJsonFromJson_NullElement() {
        val result = ec.cityalerta.app.model.utils.geoJsonFromJson(null)
        assertEquals("FeatureCollection", result.type)
        assertTrue(result.features.isEmpty())
    }

    @Test
    fun testGeoJsonFromJson_ValidJson() {
        val geometry = ec.cityalerta.app.model.data.geoJson.Geometry(
            type = "Polygon",
            coordinates = listOf(listOf(listOf(-80.0, -1.0)))
        )
        val geoJson = ec.cityalerta.app.model.data.geoJson.GeoJson(
            type = "FeatureCollection",
            features = listOf(
                ec.cityalerta.app.model.data.geoJson.Feature(
                    type = "Feature",
                    properties = ec.cityalerta.app.model.data.geoJson.Properties(name = "Test", country = "EC"),
                    geometry = geometry
                )
            )
        )
        val json = ec.cityalerta.app.model.utils.geoJsonToJson(geoJson)
        
        val result = ec.cityalerta.app.model.utils.geoJsonFromJson(json)

        assertEquals("FeatureCollection", result.type)
        assertEquals(1, result.features.size)
        assertEquals("Test", result.features[0].properties.name)
        assertEquals("Polygon", result.features[0].geometry.type)
        assertEquals(-80.0, result.features[0].geometry.coordinates[0][0][0])
    }

    @Test
    fun testGeoJsonFromJson_MissingFields() {
        val json = JsonObject(mapOf("type" to JsonPrimitive("FeatureCollection"), "features" to kotlinx.serialization.json.buildJsonArray { }))
        val result = ec.cityalerta.app.model.utils.geoJsonFromJson(json)
        assertEquals("FeatureCollection", result.type)
        assertTrue(result.features.isEmpty())
    }
}
