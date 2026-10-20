package cl.belmonte.app.repository

import cl.belmonte.app.dao.UsuarioDao
import cl.belmonte.app.database.AppDatabase
import cl.belmonte.app.database.DatosInicialesBelmonte
import cl.belmonte.app.model.ConductorConUsuario
import cl.belmonte.app.model.EmpresaEntity
import cl.belmonte.app.model.UsuarioEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Repositorio para la gestión de Usuarios, Empresas y Conductores.
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Conecta los ViewModels con UsuarioDao. Además, garantiza que los datos de prueba estén
 * cargados antes de validar el login y combina los datos de Usuario + Conductor.
 *
 * ¿POR QUÉ EL VIEWMODEL LLAMA AL REPOSITORY Y NO DIRECTO AL DAO?
 * Porque así separamos la interfaz (ViewModel) de la fuente de datos (SQLite/Room o futuro Backend).
 */
class UsuarioRepository(
    private val db: AppDatabase,
    private val usuarioDao: UsuarioDao
) {

    /**
     * Asegura que la base de datos local tenga los datos del script SQL cargados.
     */
    suspend fun asegurarDatosIniciales() {
        DatosInicialesBelmonte.poblarSiEstaVacia(db)
    }

    /**
     * Valida el inicio de sesión local (CU-01).
     * En local compara la contraseña en texto plano ("Password123*"); en el backend se usa BCrypt.
     */
    suspend fun login(identificador: String, password: String): UsuarioEntity? {
        asegurarDatosIniciales()
        return usuarioDao.login(identificador.trim(), password)
    }

    fun obtenerTodosLosUsuarios(): Flow<List<UsuarioEntity>> =
        usuarioDao.obtenerTodosLosUsuarios()

    fun obtenerUsuariosPorEmpresa(idEmpresa: Long): Flow<List<UsuarioEntity>> =
        usuarioDao.obtenerUsuariosPorEmpresa(idEmpresa)

    suspend fun obtenerUsuarioPorId(idUsuario: Long): UsuarioEntity? =
        usuarioDao.obtenerUsuarioPorId(idUsuario)

    fun obtenerEmpresas(): Flow<List<EmpresaEntity>> =
        usuarioDao.obtenerEmpresas()

    suspend fun obtenerEmpresaPorId(idEmpresa: Long): EmpresaEntity? =
        usuarioDao.obtenerEmpresaPorId(idEmpresa)

    /**
     * Une la tabla de conductores con la tabla de usuarios para obtener nombre, teléfono y licencia.
     */
    fun obtenerConductoresConUsuario(): Flow<List<ConductorConUsuario>> {
        return combine(
            usuarioDao.obtenerTodosLosUsuarios(),
            usuarioDao.obtenerConductores()
        ) { listaUsuarios, listaConductores ->
            val mapaUsuarios = listaUsuarios.associateBy { it.idUsuario }
            listaConductores.mapNotNull { conductor ->
                val usuario = mapaUsuarios[conductor.idConductor]
                if (usuario != null) {
                    ConductorConUsuario(usuario = usuario, conductor = conductor)
                } else {
                    null
                }
            }
        }
    }

    suspend fun obtenerConductorConUsuarioPorId(idConductor: Long): ConductorConUsuario? {
        val usuario = usuarioDao.obtenerUsuarioPorId(idConductor) ?: return null
        val conductor = usuarioDao.obtenerConductorPorId(idConductor) ?: return null
        return ConductorConUsuario(usuario = usuario, conductor = conductor)
    }
}
