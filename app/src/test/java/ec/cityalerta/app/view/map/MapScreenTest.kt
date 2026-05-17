package ec.cityalerta.app.view.map

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests unitarios para utilidades de [MapScreen].
 * Usa reflexion para validar funciones file-private sin modificar el codigo de produccion.
 */
class MapScreenTest {

    @Test
    fun buildCityBounds_emptyList_returnsNull() {
        val bounds = invokeBuildCityBounds(emptyList())

        assertNull(bounds)
    }

    @Test
    fun buildCityBounds_singlePoint_returnsDegenerateBounds() {
        val point = LatLng(-1.04, -80.73)

        val bounds = invokeBuildCityBounds(listOf(point))

        assertNotNull(bounds)
        assertEquals(point, bounds.southwest)
        assertEquals(point, bounds.northeast)
    }

    @Test
    fun buildCityBounds_multiplePoints_returnsEnclosingBounds() {
        val points = listOf(
            LatLng(-1.0, -80.8),
            LatLng(-0.9, -80.7),
            LatLng(-1.1, -80.6)
        )

        val bounds = invokeBuildCityBounds(points)

        assertNotNull(bounds)
        assertEquals(-1.1, bounds.southwest.latitude, 0.0001)
        assertEquals(-80.8, bounds.southwest.longitude, 0.0001)
        assertEquals(-0.9, bounds.northeast.latitude, 0.0001)
        assertEquals(-80.6, bounds.northeast.longitude, 0.0001)
    }

    private fun invokeBuildCityBounds(points: List<LatLng>): LatLngBounds? {
        val method = Class.forName("ec.cityalerta.app.view.map.MapScreenKt")
            .getDeclaredMethod("buildCityBounds", List::class.java)
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return method.invoke(null, points) as LatLngBounds?
    }
}
