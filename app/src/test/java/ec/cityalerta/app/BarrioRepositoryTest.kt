package ec.cityalerta.app

import ec.cityalerta.app.model.data.barrio.Barrio
import ec.cityalerta.app.model.data.barrio.BarrioCreateDto
import ec.cityalerta.app.model.data.barrio.BarrioUpdateDto
import ec.cityalerta.app.model.data.geoJson.Geometry
import ec.cityalerta.app.model.repository.BarrioRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests unitarios para BarrioRepository.
 * Valida la creacion y manipulacion de datos de barrios.
 */
class BarrioRepositoryTest {

    private lateinit var repository: BarrioRepository

    @Before
    fun setUp() {
        repository = BarrioRepository()
    }

    @Test
    fun testBarrioCreateDtoCreation() {
        // Arrange
        val geometry = Geometry(
            type = "Polygon",
            coordinates = listOf(
                listOf(
                    listOf(-80.73, -1.04),
                    listOf(-80.72, -1.04),
                    listOf(-80.72, -1.05),
                    listOf(-80.73, -1.05)
                )
            )
        )
        val createDto = BarrioCreateDto(
            ciudadId = "ciudad-123",
            nombre = "Barrio Centro",
            nivelPeligrosidad = "MEDIO",
            perimetro = geometry
        )

        // Assert
        assertNotNull(createDto)
        assertEquals("ciudad-123", createDto.ciudadId)
        assertEquals("Barrio Centro", createDto.nombre)
        assertEquals("MEDIO", createDto.nivelPeligrosidad)
        assertEquals(geometry, createDto.perimetro)
    }

    @Test
    fun testBarrioUpdateDtoCreation() {
        // Arrange
        val geometry = Geometry(
            type = "Polygon",
            coordinates = listOf(
                listOf(
                    listOf(-80.73, -1.04),
                    listOf(-80.72, -1.04),
                    listOf(-80.72, -1.05),
                    listOf(-80.73, -1.05)
                )
            )
        )
        val updateDto = BarrioUpdateDto(
            nombre = "Nuevo nombre",
            nivelPeligrosidad = "ALTO",
            perimetro = geometry
        )

        // Assert
        assertNotNull(updateDto)
        assertEquals("Nuevo nombre", updateDto.nombre)
        assertEquals("ALTO", updateDto.nivelPeligrosidad)
        assertEquals(geometry, updateDto.perimetro)
    }

    @Test
    fun testBarrioUpdateDtoWithNullPerimetro() {
        // Arrange
        val updateDto = BarrioUpdateDto(
            nombre = "Barrio Sin Perimetro",
            nivelPeligrosidad = "BAJO",
            perimetro = null
        )

        // Assert
        assertNotNull(updateDto)
        assertEquals("Barrio Sin Perimetro", updateDto.nombre)
        assertEquals("BAJO", updateDto.nivelPeligrosidad)
        assertEquals(null, updateDto.perimetro)
    }

    @Test
    fun testBarrioDataClass() {
        // Arrange
        val geometry = Geometry(
            type = "Polygon",
            coordinates = listOf(
                listOf(
                    listOf(-80.73, -1.04),
                    listOf(-80.72, -1.04),
                    listOf(-80.72, -1.05),
                    listOf(-80.73, -1.05)
                )
            )
        )
        val barrio = Barrio(
            id = "barrio-1",
            ciudadId = "ciudad-1",
            nombre = "Sector Historico",
            nivelPeligrosidad = "MEDIO",
            perimetro = geometry,
            createdAt = "2024-01-01T10:00:00Z",
            updatedAt = "2024-01-05T15:30:00Z"
        )

        // Assert
        assertNotNull(barrio)
        assertEquals("barrio-1", barrio.id)
        assertEquals("ciudad-1", barrio.ciudadId)
        assertEquals("Sector Historico", barrio.nombre)
        assertEquals("MEDIO", barrio.nivelPeligrosidad)
        assertEquals(geometry, barrio.perimetro)
        assertEquals("2024-01-01T10:00:00Z", barrio.createdAt)
        assertEquals("2024-01-05T15:30:00Z", barrio.updatedAt)
    }

    @Test
    fun testGeometryDataClass() {
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
        val geometry = Geometry(type = "Polygon", coordinates = coordinates)

        // Assert
        assertNotNull(geometry)
        assertEquals("Polygon", geometry.type)
        assertEquals(coordinates, geometry.coordinates)
        assertEquals(1, geometry.coordinates.size)
        assertEquals(5, geometry.coordinates[0].size)
    }

    @Test
    fun testGeometryWithMultipleRings() {
        // Arrange
        val coordinates = listOf(
            listOf(
                listOf(-80.73, -1.04),
                listOf(-80.72, -1.04),
                listOf(-80.72, -1.05),
                listOf(-80.73, -1.05),
                listOf(-80.73, -1.04)
            ),
            listOf(
                listOf(-80.725, -1.042),
                listOf(-80.715, -1.042),
                listOf(-80.715, -1.048),
                listOf(-80.725, -1.048),
                listOf(-80.725, -1.042)
            )
        )
        val geometry = Geometry(type = "Polygon", coordinates = coordinates)

        // Assert
        assertEquals(2, geometry.coordinates.size)
        assertTrue(geometry.coordinates[0].size > 0)
        assertTrue(geometry.coordinates[1].size > 0)
    }

    @Test
    fun testBarrioCreateDtoWithDifferentPeligrosidad() {
        // Arrange
        val geometryLow = Geometry("Polygon", listOf(listOf(listOf(-80.73, -1.04), listOf(-80.72, -1.04))))
        val geometryMed = Geometry("Polygon", listOf(listOf(listOf(-80.73, -1.04), listOf(-80.72, -1.04))))
        val geometryHigh = Geometry("Polygon", listOf(listOf(listOf(-80.73, -1.04), listOf(-80.72, -1.04))))

        val barrioBajo = BarrioCreateDto("ciudad-1", "Barrio Seguro", "BAJO", geometryLow)
        val barrioMedio = BarrioCreateDto("ciudad-1", "Barrio Normal", "MEDIO", geometryMed)
        val barrioAlto = BarrioCreateDto("ciudad-1", "Barrio Peligroso", "ALTO", geometryHigh)

        // Assert
        assertEquals("BAJO", barrioBajo.nivelPeligrosidad)
        assertEquals("MEDIO", barrioMedio.nivelPeligrosidad)
        assertEquals("ALTO", barrioAlto.nivelPeligrosidad)
    }

    @Test
    fun testBarrioEquality() {
        // Arrange
        val geometry = Geometry("Polygon", listOf(listOf(listOf(-80.73, -1.04))))
        val barrio1 = Barrio(
            id = "barrio-1",
            ciudadId = "ciudad-1",
            nombre = "Barrio Centro",
            nivelPeligrosidad = "MEDIO",
            perimetro = geometry
        )
        val barrio2 = barrio1.copy()

        // Assert
        assertEquals(barrio1, barrio2)
        assertEquals(barrio1.id, barrio2.id)
        assertEquals(barrio1.nombre, barrio2.nombre)
    }

    @Test
    fun testBarrioCopyWithModifications() {
        // Arrange
        val geometry = Geometry("Polygon", listOf(listOf(listOf(-80.73, -1.04))))
        val originalBarrio = Barrio(
            id = "barrio-1",
            ciudadId = "ciudad-1",
            nombre = "Original",
            nivelPeligrosidad = "BAJO",
            perimetro = geometry
        )

        // Act
        val modifiedBarrio = originalBarrio.copy(
            nombre = "Modificado",
            nivelPeligrosidad = "ALTO"
        )

        // Assert
        assertEquals("barrio-1", modifiedBarrio.id)
        assertEquals("Modificado", modifiedBarrio.nombre)
        assertEquals("ALTO", modifiedBarrio.nivelPeligrosidad)
        assertEquals("Original", originalBarrio.nombre)
        assertEquals("BAJO", originalBarrio.nivelPeligrosidad)
    }

    @Test
    fun testBarrioRepositoryInitialization() {
        // Arrange & Act
        val repo = BarrioRepository()

        // Assert
        assertNotNull(repo)
        assertTrue(repo is BarrioRepository)
    }

    @Test
    fun testMultipleGeometryCoordinates() {
        // Arrange
        val coords1 = listOf(listOf(-80.73, -1.04), listOf(-80.72, -1.04))
        val coords2 = listOf(listOf(-80.73, -1.04), listOf(-80.72, -1.04), listOf(-80.72, -1.05))
        val coords3 = listOf(listOf(-80.73, -1.04), listOf(-80.72, -1.04), listOf(-80.72, -1.05), listOf(-80.73, -1.05))

        val geom1 = Geometry("LineString", listOf(coords1))
        val geom2 = Geometry("LineString", listOf(coords2))
        val geom3 = Geometry("Polygon", listOf(coords3))

        // Assert
        assertEquals(2, geom1.coordinates[0].size)
        assertEquals(3, geom2.coordinates[0].size)
        assertEquals(4, geom3.coordinates[0].size)
    }

    @Test
    fun testBarrioNombres() {
        // Arrange & Act
        val barrios = listOf(
            BarrioCreateDto("c1", "Barrio Centro", "MEDIO", Geometry("Polygon", emptyList())),
            BarrioCreateDto("c1", "Sector Historico", "BAJO", Geometry("Polygon", emptyList())),
            BarrioCreateDto("c1", "Zona Residencial", "ALTO", Geometry("Polygon", emptyList()))
        )

        // Assert
        assertEquals(3, barrios.size)
        assertTrue(barrios.any { it.nombre == "Barrio Centro" })
        assertTrue(barrios.any { it.nombre == "Sector Historico" })
        assertTrue(barrios.any { it.nombre == "Zona Residencial" })
    }
}
