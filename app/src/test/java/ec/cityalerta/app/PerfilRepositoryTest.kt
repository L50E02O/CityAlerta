package ec.cityalerta.app

import ec.cityalerta.app.model.data.contracts.crud.CrudRepositoryContract
import ec.cityalerta.app.model.data.perfil.Perfil
import ec.cityalerta.app.model.data.perfil.PerfilCreateDto
import ec.cityalerta.app.model.data.perfil.PerfilUpdateDto
import ec.cityalerta.app.model.repository.PerfilRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests unitarios para PerfilRepository.
 * Valida la creacion y manipulacion de datos de perfiles.
 */
class PerfilRepositoryTest {

    private lateinit var repository: PerfilRepository

    @Before
    fun setUp() {
        repository = PerfilRepository()
    }

    @Test
    fun testPerfilCreateDtoCreation() {
        val createDto = PerfilCreateDto(
            nombreCompleto = "Juan Perez",
            rolSlug = "ciudadano",
            activo = true,
            ciudadId = "ciudad-manta"
        )

        assertNotNull(createDto)
        assertEquals("Juan Perez", createDto.nombreCompleto)
        assertEquals("ciudadano", createDto.rolSlug)
        assertTrue(createDto.activo)
        assertEquals("ciudad-manta", createDto.ciudadId)
    }

    @Test
    fun testPerfilCreateDtoInactivo() {
        val createDto = PerfilCreateDto(
            nombreCompleto = "Usuario Inactivo",
            rolSlug = "admin",
            activo = false,
            ciudadId = "ciudad-1"
        )

        assertFalse(createDto.activo)
    }

    @Test
    fun testPerfilUpdateDtoCreation() {
        val updateDto = PerfilUpdateDto(
            nombreCompleto = "Juan Perez Actualizado",
            rolSlug = "moderador",
            activo = false,
            ciudadId = "ciudad-quito"
        )

        assertNotNull(updateDto)
        assertEquals("Juan Perez Actualizado", updateDto.nombreCompleto)
        assertEquals("moderador", updateDto.rolSlug)
        assertEquals(false, updateDto.activo)
        assertEquals("ciudad-quito", updateDto.ciudadId)
    }

    @Test
    fun testPerfilUpdateDtoConCamposOpcionales() {
        val updateDto = PerfilUpdateDto(
            nombreCompleto = "Solo nombre",
            rolSlug = null,
            activo = null,
            ciudadId = null
        )

        assertNotNull(updateDto)
        assertEquals("Solo nombre", updateDto.nombreCompleto)
        assertNull(updateDto.rolSlug)
        assertNull(updateDto.activo)
        assertNull(updateDto.ciudadId)
    }

    @Test
    fun testPerfilDataClass() {
        val perfil = Perfil(
            id = "perfil-1",
            nombreCompleto = "Maria Lopez",
            rolSlug = "ciudadano",
            activo = true,
            createdAt = "2024-01-01T10:00:00Z",
            updatedAt = "2024-01-05T15:30:00Z",
            ciudadId = "ciudad-manta"
        )

        assertNotNull(perfil)
        assertEquals("perfil-1", perfil.id)
        assertEquals("Maria Lopez", perfil.nombreCompleto)
        assertEquals("ciudadano", perfil.rolSlug)
        assertTrue(perfil.activo)
        assertEquals("ciudad-manta", perfil.ciudadId)
        assertEquals("2024-01-01T10:00:00Z", perfil.createdAt)
        assertEquals("2024-01-05T15:30:00Z", perfil.updatedAt)
    }

    @Test
    fun testPerfilSinTimestampsOpcionales() {
        val perfil = Perfil(
            id = "perfil-2",
            nombreCompleto = "Carlos Ruiz",
            rolSlug = "admin",
            activo = true,
            ciudadId = "ciudad-1"
        )

        assertNull(perfil.createdAt)
        assertNull(perfil.updatedAt)
    }

    @Test
    fun testPerfilConDiferentesRoles() {
        val roles = listOf("ciudadano", "admin", "moderador")

        roles.forEach { rol ->
            val perfil = Perfil(
                id = "perfil-$rol",
                nombreCompleto = "Usuario $rol",
                rolSlug = rol,
                activo = true,
                ciudadId = "ciudad-1"
            )
            assertEquals(rol, perfil.rolSlug)
        }
    }

    @Test
    fun testPerfilEquality() {
        val perfil1 = Perfil(
            id = "perfil-1",
            nombreCompleto = "Ana Torres",
            rolSlug = "ciudadano",
            activo = true,
            ciudadId = "ciudad-1"
        )
        val perfil2 = perfil1.copy()

        assertEquals(perfil1, perfil2)
        assertEquals(perfil1.id, perfil2.id)
        assertEquals(perfil1.nombreCompleto, perfil2.nombreCompleto)
    }

    @Test
    fun testPerfilCopyWithModifications() {
        val original = Perfil(
            id = "perfil-1",
            nombreCompleto = "Original",
            rolSlug = "ciudadano",
            activo = true,
            ciudadId = "ciudad-1"
        )

        val modificado = original.copy(
            nombreCompleto = "Modificado",
            activo = false,
            rolSlug = "admin"
        )

        assertEquals("perfil-1", modificado.id)
        assertEquals("Modificado", modificado.nombreCompleto)
        assertFalse(modificado.activo)
        assertEquals("admin", modificado.rolSlug)
        assertEquals("Original", original.nombreCompleto)
        assertTrue(original.activo)
    }

    @Test
    fun testMultiplesPerfilesMismaCiudad() {
        val ciudadId = "ciudad-manta"
        val perfiles = (1..3).map { index ->
            PerfilCreateDto(
                nombreCompleto = "Usuario $index",
                rolSlug = "ciudadano",
                activo = true,
                ciudadId = ciudadId
            )
        }

        assertEquals(3, perfiles.size)
        assertTrue(perfiles.all { it.ciudadId == ciudadId })
    }

    @Test
    fun testPerfilRepositoryInitialization() {
        assertNotNull(repository)
        assertTrue(repository is PerfilRepository)
    }

    @Test
    fun testPerfilRepositoryImplementaContratoCrud() {
        assertTrue(repository is CrudRepositoryContract<*, *, *>)
    }
}
