package cl.belmonte.app.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.belmonte.app.model.EstadoViajeEntity
import cl.belmonte.app.model.HistorialEstadoEntity
import cl.belmonte.app.model.HistorialReasignacionEntity
import cl.belmonte.app.model.IncidenciaEntity
import cl.belmonte.app.model.ViajeEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO principal de operación de traslados: viajes, estados, historial de cambios,
 * reasignaciones de conductor e incidencias en ruta.
 *
 * ¿QUÉ HACE ESTA INTERFAZ?
 * Resuelve las consultas de los 4 roles sobre los viajes (Consultas A, C, D, E, F y G del script SQL)
 * y permite insertar nuevos viajes, actualizar su estado y guardar la trazabilidad completa.
 */
@Dao
interface ViajeDao {

    // --- CONSULTAS DE VIAJES ---

    @Query("SELECT * FROM viajes ORDER BY fecha_servicio DESC, hora_servicio DESC")
    fun obtenerTodosLosViajes(): Flow<List<ViajeEntity>>

    @Query("SELECT * FROM viajes WHERE id_solicitante = :idSolicitante ORDER BY fecha_servicio DESC, hora_servicio DESC")
    fun obtenerViajesPorSolicitante(idSolicitante: Long): Flow<List<ViajeEntity>>

    @Query("SELECT * FROM viajes WHERE id_conductor = :idConductor ORDER BY fecha_servicio DESC, hora_servicio ASC")
    fun obtenerViajesPorConductor(idConductor: Long): Flow<List<ViajeEntity>>

    @Query("SELECT * FROM viajes WHERE id_estado = :idEstado ORDER BY fecha_servicio ASC, hora_servicio ASC")
    fun obtenerViajesPorEstado(idEstado: Int): Flow<List<ViajeEntity>>

    @Query("SELECT * FROM viajes WHERE id_empresa = :idEmpresa ORDER BY fecha_servicio DESC, hora_servicio DESC")
    fun obtenerViajesPorEmpresa(idEmpresa: Long): Flow<List<ViajeEntity>>

    @Query("SELECT * FROM viajes WHERE id_viaje = :idViaje LIMIT 1")
    fun observarViajePorId(idViaje: Long): Flow<ViajeEntity?>

    @Query("SELECT * FROM viajes WHERE id_viaje = :idViaje LIMIT 1")
    suspend fun obtenerViajePorId(idViaje: Long): ViajeEntity?

    /**
     * Cuenta cuántos cupos de un tipo de vehículo ha usado un trabajador en un mes (RN-07).
     * Excluye los viajes CANCELADOS (id_estado != 5) porque al cancelar el cupo se devuelve.
     * Ejemplo de prefijoMes: "2026-10%"
     */
    @Query(
        """
        SELECT COUNT(*) FROM viajes 
        WHERE id_solicitante = :idSolicitante 
          AND id_tipo_vehiculo = :idTipoVehiculo 
          AND id_estado != 5 
          AND fecha_servicio LIKE :prefijoMes
        """
    )
    suspend fun contarViajesUsadosTrabajadorEnMes(
        idSolicitante: Long,
        idTipoVehiculo: Int,
        prefijoMes: String
    ): Int

    /**
     * Cuenta cuántos viajes no cancelados ha realizado toda una empresa por tipo de vehículo en el mes.
     */
    @Query(
        """
        SELECT COUNT(*) FROM viajes 
        WHERE id_empresa = :idEmpresa 
          AND id_tipo_vehiculo = :idTipoVehiculo 
          AND id_estado != 5 
          AND fecha_servicio LIKE :prefijoMes
        """
    )
    suspend fun contarViajesUsadosEmpresaEnMes(
        idEmpresa: Long,
        idTipoVehiculo: Int,
        prefijoMes: String
    ): Int

    /**
     * Suma el cargo_extra de los viajes FINALIZADOS (id_estado = 4) de una empresa en el mes (RN-14).
     */
    @Query(
        """
        SELECT COALESCE(SUM(cargo_extra), 0) FROM viajes 
        WHERE id_empresa = :idEmpresa 
          AND id_estado = 4 
          AND fecha_servicio LIKE :prefijoMes
        """
    )
    suspend fun sumarCargoExtraFinalizadosEmpresa(idEmpresa: Long, prefijoMes: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarViaje(viaje: ViajeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarViajes(viajes: List<ViajeEntity>)

    @Update
    suspend fun actualizarViaje(viaje: ViajeEntity)

    @Delete
    suspend fun eliminarViaje(viaje: ViajeEntity)

    // --- CATÁLOGO DE ESTADOS DE VIAJE ---

    @Query("SELECT * FROM estados_viaje ORDER BY orden ASC")
    fun obtenerEstadosViaje(): Flow<List<EstadoViajeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEstadosViaje(estados: List<EstadoViajeEntity>)

    // --- HISTORIAL DE ESTADOS DEL VIAJE (RN-03) ---

    @Query("SELECT * FROM historial_estados_viaje WHERE id_viaje = :idViaje ORDER BY fecha_cambio ASC, id_historial ASC")
    fun obtenerHistorialPorViaje(idViaje: Long): Flow<List<HistorialEstadoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarHistorialEstado(historial: HistorialEstadoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarHistorialesEstado(historiales: List<HistorialEstadoEntity>)

    @Delete
    suspend fun eliminarHistorialEstado(historial: HistorialEstadoEntity)

    // --- HISTORIAL DE REASIGNACIONES (CU-10) ---

    @Query("SELECT * FROM historial_reasignaciones WHERE id_viaje = :idViaje ORDER BY fecha_reasignacion DESC")
    fun obtenerReasignacionesPorViaje(idViaje: Long): Flow<List<HistorialReasignacionEntity>>

    @Query("SELECT * FROM historial_reasignaciones ORDER BY fecha_reasignacion DESC")
    fun obtenerTodasLasReasignaciones(): Flow<List<HistorialReasignacionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarReasignacion(reasignacion: HistorialReasignacionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarReasignaciones(reasignaciones: List<HistorialReasignacionEntity>)

    // --- INCIDENCIAS EN RUTA (CU-08) ---

    @Query("SELECT * FROM incidencias WHERE id_viaje = :idViaje ORDER BY fecha_registro DESC")
    fun obtenerIncidenciasPorViaje(idViaje: Long): Flow<List<IncidenciaEntity>>

    @Query("SELECT * FROM incidencias ORDER BY fecha_registro DESC")
    fun obtenerTodasLasIncidencias(): Flow<List<IncidenciaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarIncidencia(incidencia: IncidenciaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarIncidencias(incidencias: List<IncidenciaEntity>)

    @Update
    suspend fun actualizarIncidencia(incidencia: IncidenciaEntity)

    @Delete
    suspend fun eliminarIncidencia(incidencia: IncidenciaEntity)
}
