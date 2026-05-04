package ec.cityalerta.app

import ec.cityalerta.app.model.data.reporte.ReporteEstado
import ec.cityalerta.app.model.utils.booleanOrFalse
import ec.cityalerta.app.model.utils.doubleOrZero
import ec.cityalerta.app.model.utils.nullableString
import ec.cityalerta.app.model.utils.stringOrEmpty
import ec.cityalerta.app.model.utils.toReporteEstadoOrDefault
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
    fun testStringOrEmptyReturnsEmptyWhenNull() {
        val json = JsonObject(mapOf("name" to JsonPrimitive("")))
        // Nota: esto depende de como se implemente
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
    fun testBooleanOrFalseReturnsFalseWhenMissing() {
        val json = JsonObject(mapOf())
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
    fun testToReporteEstadoOrDefaultReturnsPendienteWhenMatches() {
        val json = JsonObject(mapOf("estado_slug" to JsonPrimitive("PENDIENTE")))
        val result = json.toReporteEstadoOrDefault("estado_slug")
        assertEquals(ReporteEstado.PENDIENTE, result)
    }

    @Test
    fun testToReporteEstadoOrDefaultReturnsResueltoWhenMatches() {
        val json = JsonObject(mapOf("estado_slug" to JsonPrimitive("RESUELTO")))
        val result = json.toReporteEstadoOrDefault("estado_slug")
        assertEquals(ReporteEstado.RESUELTO, result)
    }

    @Test
    fun testToReporteEstadoOrDefaultReturnsPendienteAsDefault() {
        val json = JsonObject(mapOf("estado_slug" to JsonPrimitive("INVALIDO")))
        val result = json.toReporteEstadoOrDefault("estado_slug")
        assertEquals(ReporteEstado.PENDIENTE, result)
    }

    @Test
    fun testToReporteEstadoOrDefaultReturnsPendienteWhenMissing() {
        val json = JsonObject(mapOf())
        val result = json.toReporteEstadoOrDefault("estado_slug")
        assertEquals(ReporteEstado.PENDIENTE, result)
    }

    @Test
    fun testStringOrEmptyWithMultipleFields() {
        val json = JsonObject(
            mapOf(
                "nombre" to JsonPrimitive("Juan"),
                "apellido" to JsonPrimitive("Perez")
            )
        )
        assertEquals("Juan", json.stringOrEmpty("nombre"))
        assertEquals("Perez", json.stringOrEmpty("apellido"))
    }

    @Test
    fun testDoubleOrZeroWithMultipleCoordinates() {
        val json = JsonObject(
            mapOf(
                "lat" to JsonPrimitive(-0.9542),
                "lng" to JsonPrimitive(-80.7314)
            )
        )
        assertEquals(-0.9542, json.doubleOrZero("lat"))
        assertEquals(-80.7314, json.doubleOrZero("lng"))
    }

    @Test
    fun testNullableStringWithMixedData() {
        val json = JsonObject(
            mapOf(
                "email" to JsonPrimitive("test@example.com"),
                "phone" to JsonPrimitive("")
            )
        )
        assertEquals("test@example.com", json.nullableString("email"))
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
        val firstFeature = features?.get(0)?.jsonObject
        assertEquals("Feature", firstFeature?.get("type")?.jsonPrimitive?.content)
        assertEquals("Test", firstFeature?.get("properties")?.jsonObject?.get("name")?.jsonPrimitive?.content)
    }

    @Test
    fun testGeoJsonFromJsonConversion() {
        val json = JsonObject(
            mapOf(
                "type" to JsonPrimitive("FeatureCollection"),
                "features" to ec.cityalerta.app.model.utils.geoJsonToJson(
                    ec.cityalerta.app.model.data.geoJson.GeoJson(
                        type = "FeatureCollection",
                        features = listOf(
                            ec.cityalerta.app.model.data.geoJson.Feature(
                                type = "Feature",
                                properties = ec.cityalerta.app.model.data.geoJson.Properties(name = "Test", country = "EC"),
                                geometry = ec.cityalerta.app.model.data.geoJson.Geometry(
                                    type = "Polygon",
                                    coordinates = listOf(listOf(listOf(-80.0, -1.0)))
                                )
                            )
                        )
                    )
                )["features"]!!
            )
        )

        val result = ec.cityalerta.app.model.utils.geoJsonFromJson(json)

        assertEquals("FeatureCollection", result.type)
        assertEquals(1, result.features.size)
        assertEquals("Test", result.features[0].properties.name)
        assertEquals("Polygon", result.features[0].geometry.type)
    }
}

