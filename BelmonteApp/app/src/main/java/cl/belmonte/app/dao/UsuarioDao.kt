package cl.belmonte.app.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.belmonte.app.model.ConductorEntity
import cl.belmonte.app.model.EmpresaEntity
import cl.belmonte.app.model.UsuarioEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para las tablas de usuarios, empresas y conductores.
 *
 * ¿QUÉ HACE ESTA INTERFAZ?
 * Declara las operaciones SQL (@Insert, @Update, @Delete y @Query) que Room implementa
 * automáticamente en tiempo de compilación mediante KSP.
 *
 * ¿POR QUÉ USAMOS Flow E INSTRUCCIONES suspend?
 * - Las funciones suspend se ejecutan en segundo plano (corrutinas) sin bloquear la pantalla.
 * - Las funciones que retornan Flow<List<...>> notifican automáticamente a la interfaz Compose
 *   cada vez que se inserta o actualiza un dato en SQLite.
 */
@Dao
interface UsuarioDao {

    // --- Operaciones sobre USUARIOS ---

    /**
     * Valida las credenciales de login local (CU-01).
     * Compara email o RUT y contraseña en la tabla local de usuarios activos.
     */
    @Query(
        """
        SELECT * FROM usuarios 
        WHERE (LOWER(email) = LOWER(:identificador) OR LOWER(rut) = LOWER(:identificador))
          AND password_hash = :password 
          AND activo = 1 
        LIMIT 1
        """
    )
    suspend fun login(identificador: String, password: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id_usuario = :idUsuario LIMIT 1")
    suspend fun obtenerUsuarioPorId(idUsuario: Long): UsuarioEntity?

    @Query("SELECT * FROM usuarios ORDER BY id_usuario ASC")
    fun obtenerTodosLosUsuarios(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE id_empresa = :idEmpresa AND activo = 1 ORDER BY nombres ASC")
    fun obtenerUsuariosPorEmpresa(idEmpresa: Long): Flow<List<UsuarioEntity>>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contarUsuarios(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: UsuarioEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuarios(usuarios: List<UsuarioEntity>)

    @Update
    suspend fun actualizarUsuario(usuario: UsuarioEntity)

    @Delete
    suspend fun eliminarUsuario(usuario: UsuarioEntity)

    // --- Operaciones sobre EMPRESAS ---

    @Query("SELECT * FROM empresas ORDER BY id_empresa ASC")
    fun obtenerEmpresas(): Flow<List<EmpresaEntity>>

    @Query("SELECT * FROM empresas WHERE id_empresa = :idEmpresa LIMIT 1")
    suspend fun obtenerEmpresaPorId(idEmpresa: Long): EmpresaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEmpresas(empresas: List<EmpresaEntity>)

    @Update
    suspend fun actualizarEmpresa(empresa: EmpresaEntity)

    @Delete
    suspend fun eliminarEmpresa(empresa: EmpresaEntity)

    // --- Operaciones sobre CONDUCTORES ---

    @Query("SELECT * FROM conductores ORDER BY id_conductor ASC")
    fun obtenerConductores(): Flow<List<ConductorEntity>>

    @Query("SELECT * FROM conductores WHERE id_conductor = :idConductor LIMIT 1")
    suspend fun obtenerConductorPorId(idConductor: Long): ConductorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarConductores(conductores: List<ConductorEntity>)

    @Update
    suspend fun actualizarConductor(conductor: ConductorEntity)

    @Delete
    suspend fun eliminarConductor(conductor: ConductorEntity)
}
