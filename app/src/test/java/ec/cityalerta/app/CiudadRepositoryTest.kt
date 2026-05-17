package ec.cityalerta.app

import ec.cityalerta.app.model.data.ciudad.Ciudad
import ec.cityalerta.app.model.data.ciudad.CiudadCreateDto
import ec.cityalerta.app.model.data.ciudad.CiudadUpdateDto
import ec.cityalerta.app.model.data.geoJson.Geometry
import ec.cityalerta.app.model.repository.CiudadRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests unitarios para CiudadRepository.
 * Valida la creacion y manipulacion de datos de ciudades.
 */
class CiudadRepositoryTest {

    private lateinit var repository: CiudadRepository

    @Before
    fun setUp() {
        repository = CiudadRepository()
    }

    @Test
    fun testCiudadCreateDtoCreation() {
        // Arrange
        val geometry = Geometry(
            type = "FeatureCollection",
            coordinates = listOf(
                listOf(
                    listOf(-80.73, -1.04),
                    listOf(-80.72, -1.04),
                    listOf(-80.72, -1.05),
                    listOf(-80.73, -1.05)
                )
            )
        )
        val createDto = CiudadCreateDto(
            nombre = "Manta",
            pais = "Ecuador",
            geojson = geometry,
            centroLat = -1.04,
            centroLng = -80.73
        )

        // Assert
        assertNotNull(createDto)
        assertEquals("Manta", createDto.nombre)
        assertEquals("Ecuador", createDto.pais)
        assertEquals(-1.04, createDto.centroLat)
        assertEquals(-80.73, createDto.centroLng)
    }

    @Test
    fun testCiudadUpdateDtoCreation() {
        // Arrange
        val geometry = Geometry(
            type = "FeatureCollection",
            coordinates = listOf(listOf(listOf(-80.73, -1.04)))
        )
        val updateDto = CiudadUpdateDto(
            nombre = "Manta Actualizada",
            pais = "Ecuador",
            geojson = geometry,
            centroLat = -1.05,
            centroLng = -80.74
        )

        // Assert
        assertNotNull(updateDto)
        assertEquals("Manta Actualizada", updateDto.nombre)
        assertEquals("Ecuador", updateDto.pais)
        assertEquals(-1.05, updateDto.centroLat)
        assertEquals(-80.74, updateDto.centroLng)
    }

    @Test
    fun testCiudadUpdateDtoWithNullGeojson() {
        // Arrange
        val updateDto = CiudadUpdateDto(
            nombre = "Nueva Ciudad",
            pais = "Ecuador",
            geojson = null,
            centroLat = -0.22,
            centroLng = -78.51
        )

        // Assert
        assertNotNull(updateDto)
        assertEquals("Nueva Ciudad", updateDto.nombre)
        assertEquals(null, updateDto.geojson)
        assertEquals(-0.22, updateDto.centroLat)
    }

    @Test
    fun testCiudadDataClass() {
        // Arrange
        val geometry = Geometry(
            type = "FeatureCollection",
            coordinates = listOf(listOf(listOf(-80.73, -1.04)))
        )
        val ciudad = Ciudad(
            id = "ciudad-1",
            nombre = "Manta",
            pais = "Ecuador",
            geojson = geometry,
            centroLat = -1.04,
            centroLng = -80.73,
            createdAt = "2024-01-01T10:00:00Z",
            updatedAt = "2024-01-05T15:30:00Z"
        )

        // Assert
        assertNotNull(ciudad)
        assertEquals("ciudad-1", ciudad.id)
        assertEquals("Manta", ciudad.nombre)
        assertEquals("Ecuador", ciudad.pais)
        assertEquals(-1.04, ciudad.centroLat)
        assertEquals(-80.73, ciudad.centroLng)
    }

    @Test
    fun testCiudadEquality() {
        // Arrange
        val geometry = Geometry("FeatureCollection", listOf(listOf(listOf(-80.73, -1.04))))
        val ciudad1 = Ciudad(
            id = "ciudad-1",
            nombre = "Quito",
            pais = "Ecuador",
            geojson = geometry,
            centroLat = -0.22,
            centroLng = -78.51
        )
        val ciudad2 = ciudad1.copy()

        // Assert
        assertEquals(ciudad1, ciudad2)
        assertEquals(ciudad1.id, ciudad2.id)
        assertEquals(ciudad1.nombre, ciudad2.nombre)
    }

    @Test
    fun testCiudadCopyWithModifications() {
        // Arrange
        val geometry = Geometry("FeatureCollection", listOf(listOf(listOf(-80.73, -1.04))))
        val originalCiudad = Ciudad(
            id = "ciudad-1",
            nombre = "Original",
            pais = "Ecuador",
            geojson = geometry,
            centroLat = -1.04,
            centroLng = -80.73
        )

        // Act
        val modifiedCiudad = originalCiudad.copy(
            nombre = "Modificada",
            centroLat = -2.0,
            centroLng = -79.0
        )

        // Assert
        assertEquals("ciudad-1", modifiedCiudad.id)
        assertEquals("Modificada", modifiedCiudad.nombre)
        assertEquals(-2.0, modifiedCiudad.centroLat)
        assertEquals(-79.0, modifiedCiudad.centroLng)
        assertEquals("Original", originalCiudad.nombre)
        assertEquals(-1.04, originalCiudad.centroLat)
    }

    @Test
    fun testEcuadorianCities() {
        // Arrange & Act
        val ciudades = listOf(
            CiudadCreateDto(
                nombre = "Quito",
                pais = "Ecuador",
                geojson = Geometry("FeatureCollection", emptyList()),
                centroLat = -0.22,
                centroLng = -78.51
            ),
            CiudadCreateDto(
                nombre = "Guayaquil",
                pais = "Ecuador",
                geojson = Geometry("FeatureCollection", emptyList()),
                centroLat = -2.19,
                centroLng = -79.88
            ),
            CiudadCreateDto(
                nombre = "Manta",
                pais = "Ecuador",
                geojson = Geometry("FeatureCollection", emptyList()),
                centroLat = -1.04,
                centroLng = -80.73
            )
        )

        // Assert
        assertEquals(3, ciudades.size)
        assertTrue(ciudades.any { it.nombre == "Quito" })
        assertTrue(ciudades.any { it.nombre == "Guayaquil" })
        assertTrue(ciudades.any { it.nombre == "Manta" })
        assertTrue(ciudades.all { it.pais == "Ecuador" })
    }

    @Test
    fun testCiudadCoordinates() {
        // Arrange
        val mantaCoords = Pair(-1.04, -80.73)
        val quitoCoords = Pair(-0.22, -78.51)
        val guayaquilCoords = Pair(-2.19, -79.88)

        val manta = CiudadCreateDto(
            nombre = "Manta",
            pais = "Ecuador",
            geojson = Geometry("FeatureCollection", emptyList()),
            centroLat = mantaCoords.first,
            centroLng = mantaCoords.second
        )
        val quito = CiudadCreateDto(
            nombre = "Quito",
            pais = "Ecuador",
            geojson = Geometry("FeatureCollection", emptyList()),
            centroLat = quitoCoords.first,
            centroLng = quitoCoords.second
        )
        val guayaquil = CiudadCreateDto(
            nombre = "Guayaquil",
            pais = "Ecuador",
            geojson = Geometry("FeatureCollection", emptyList()),
            centroLat = guayaquilCoords.first,
            centroLng = guayaquilCoords.second
        )

        // Assert
        assertEquals(mantaCoords.first, manta.centroLat)
        assertEquals(mantaCoords.second, manta.centroLng)
        assertEquals(quitoCoords.first, quito.centroLat)
        assertEquals(guayaquilCoords.first, guayaquil.centroLat)
    }

    @Test
    fun testGeometryFeatureCollection() {
        // Arrange
        val coordinates = listOf(
            listOf(
                listOf(-80.73, -1.04),
                listOf(-80.72, -1.04),
                listOf(-80.72, -1.05),
                listOf(-80.73, -1.05),
                listOf(-80.73, -1.04)
            )
        )
        val geometry = Geometry(type = "FeatureCollection", coordinates = coordinates)

        // Assert
        assertNotNull(geometry)
        assertEquals("FeatureCollection", geometry.type)
        assertEquals(1, geometry.coordinates.size)
        assertEquals(5, geometry.coordinates[0].size)
    }

    @Test
    fun testCiudadRepositoryInitialization() {
        // Arrange & Act
        val repo = CiudadRepository()

        assertNotNull(repo)
    }

    @Test
    fun testMultipleCiudadCreations() {
        // Arrange & Act
        val geom = Geometry("FeatureCollection", emptyList())
        val ciudad1 = CiudadCreateDto("Manta", "Ecuador", geom, -1.04, -80.73)
        val ciudad2 = CiudadCreateDto("Quito", "Ecuador", geom, -0.22, -78.51)
        val ciudad3 = CiudadCreateDto("Ambato", "Ecuador", geom, -1.24, -78.63)

        // Assert
        assertEquals("Manta", ciudad1.nombre)
        assertEquals("Quito", ciudad2.nombre)
        assertEquals("Ambato", ciudad3.nombre)
        assertTrue(ciudad1 !== ciudad2)
        assertTrue(ciudad2 !== ciudad3)
    }

    @Test
    fun testCiudadPaisValidation() {
        // Arrange & Act
        val ciudadEcuador = CiudadCreateDto(
            nombre = "Manta",
            pais = "Ecuador",
            geojson = Geometry("FeatureCollection", emptyList()),
            centroLat = -1.04,
            centroLng = -80.73
        )

        // Assert
        assertEquals("Ecuador", ciudadEcuador.pais)
        assertTrue(ciudadEcuador.pais.isNotEmpty())
    }

    @Test
    fun testCoordinateLimits() {
        // Arrange - Latitude range: -90 to 90, Longitude range: -180 to 180
        val validCoords = listOf(
            Pair(-1.04, -80.73),  // Manta
            Pair(0.0, 0.0),        // Equator/Prime Meridian
            Pair(90.0, 180.0),     // North Pole, International Date Line
            Pair(-90.0, -180.0)    // South Pole, International Date Line
        )

        // Act & Assert
        validCoords.forEach { coord ->
            val ciudad = CiudadCreateDto(
                nombre = "Test",
                pais = "Ecuador",
                geojson = Geometry("FeatureCollection", emptyList()),
                centroLat = coord.first,
                centroLng = coord.second
            )
            assertEquals(coord.first, ciudad.centroLat)
            assertEquals(coord.second, ciudad.centroLng)
        }
    }
}
