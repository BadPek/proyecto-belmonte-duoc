package cl.belmonte.app.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.belmonte.app.model.AsignacionCupoEntity
import cl.belmonte.app.model.CobroCupoEntity
import cl.belmonte.app.model.CobroEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para las tablas de facturación y paquetes mensuales: "cobros", "cobro_cupos" y "asignaciones_cupo".
 *
 * ¿QUÉ HACE ESTA INTERFAZ?
 * Permite consultar los paquetes contratados por cada empresa, cuántos cupos se han repartido
 * a cada trabajador y cuántos cupos tiene asignados un solicitante en un periodo (ej: "2026-10").
 */
@Dao
interface CobroDao {

    // --- COBROS MENSUALES (PAQUETE y EXTRAS) ---

    @Query("SELECT * FROM cobros WHERE id_empresa = :idEmpresa ORDER BY periodo DESC, id_cobro DESC")
    fun obtenerCobrosPorEmpresa(idEmpresa: Long): Flow<List<CobroEntity>>

    @Query("SELECT * FROM cobros ORDER BY periodo DESC, id_cobro DESC")
    fun obtenerTodosLosCobros(): Flow<List<CobroEntity>>

    @Query(
        """
        SELECT * FROM cobros 
        WHERE id_empresa = :idEmpresa 
          AND periodo = :periodo 
          AND tipo_cobro = 'PAQUETE' 
          AND estado_pago = 'PAGADO' 
        LIMIT 1
        """
    )
    suspend fun obtenerPaquetePagadoEmpresa(idEmpresa: Long, periodo: String): CobroEntity?

    @Query(
        """
        SELECT * FROM cobros 
        WHERE id_empresa = :idEmpresa 
          AND periodo = :periodo 
          AND tipo_cobro = 'EXTRAS' 
        LIMIT 1
        """
    )
    suspend fun obtenerCobroExtrasEmpresa(idEmpresa: Long, periodo: String): CobroEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCobro(cobro: CobroEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCobros(cobros: List<CobroEntity>)

    @Update
    suspend fun actualizarCobro(cobro: CobroEntity)

    @Delete
    suspend fun eliminarCobro(cobro: CobroEntity)

    // --- DETALLE DE CUPOS DEL PAQUETE (COBRO_CUPOS) ---

    @Query("SELECT * FROM cobro_cupos WHERE id_cobro = :idCobro ORDER BY id_tipo_vehiculo ASC")
    suspend fun obtenerCobroCuposPorCobro(idCobro: Long): List<CobroCupoEntity>

    @Query("SELECT * FROM cobro_cupos WHERE id_cobro_cupo = :idCobroCupo LIMIT 1")
    suspend fun obtenerCobroCupoPorId(idCobroCupo: Long): CobroCupoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCobroCupos(detalles: List<CobroCupoEntity>)

    // --- ASIGNACIONES DE CUPO A TRABAJADORES (ASIGNACIONES_CUPO) ---

    @Query("SELECT * FROM asignaciones_cupo WHERE id_usuario = :idUsuario")
    fun obtenerAsignacionesPorUsuario(idUsuario: Long): Flow<List<AsignacionCupoEntity>>

    @Query("SELECT * FROM asignaciones_cupo WHERE id_cobro_cupo = :idCobroCupo")
    suspend fun obtenerAsignacionesPorLineaPaquete(idCobroCupo: Long): List<AsignacionCupoEntity>

    @Query(
        """
        SELECT * FROM asignaciones_cupo 
        WHERE id_cobro_cupo = :idCobroCupo AND id_usuario = :idUsuario 
        LIMIT 1
        """
    )
    suspend fun obtenerAsignacionTrabajador(idCobroCupo: Long, idUsuario: Long): AsignacionCupoEntity?

    /**
     * Suma cuántos cupos ya fueron repartidos de una línea del paquete (RN-12).
     * La suma repartida entre todos los trabajadores no puede superar cantidad_viajes del paquete.
     */
    @Query("SELECT COALESCE(SUM(cantidad_viajes), 0) FROM asignaciones_cupo WHERE id_cobro_cupo = :idCobroCupo")
    suspend fun sumarCuposRepartidosPorLinea(idCobroCupo: Long): Int

    /**
     * Calcula cuántos cupos de un tipo de vehículo tiene asignados un trabajador en el mes,
     * considerando únicamente paquetes con estado_pago = 'PAGADO' (Consulta C del script SQL y RN-12).
     */
    @Query(
        """
        SELECT COALESCE(SUM(a.cantidad_viajes), 0)
        FROM asignaciones_cupo a
        INNER JOIN cobro_cupos cc ON cc.id_cobro_cupo = a.id_cobro_cupo
        INNER JOIN cobros c ON c.id_cobro = cc.id_cobro
        WHERE a.id_usuario = :idUsuario
          AND cc.id_tipo_vehiculo = :idTipoVehiculo
          AND c.periodo = :periodo
          AND c.tipo_cobro = 'PAQUETE'
          AND c.estado_pago = 'PAGADO'
        """
    )
    suspend fun obtenerCuposAsignadosTrabajadorEnMes(
        idUsuario: Long,
        idTipoVehiculo: Int,
        periodo: String
    ): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAsignacionCupo(asignacion: AsignacionCupoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAsignacionesCupo(asignaciones: List<AsignacionCupoEntity>)

    @Update
    suspend fun actualizarAsignacionCupo(asignacion: AsignacionCupoEntity)

    @Delete
    suspend fun eliminarAsignacionCupo(asignacion: AsignacionCupoEntity)
}
