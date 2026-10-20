package cl.belmonte.app.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.belmonte.app.model.TipoVehiculoEntity
import cl.belmonte.app.model.VehiculoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para las tablas "tipos_vehiculo" y "vehiculos" (flota de Belmonte SpA).
 *
 * ¿QUÉ HACE ESTA INTERFAZ?
 * Permite consultar el catálogo de tipos de vehículo (con sus topes de km incluidos y tarifas
 * de km extra) y los vehículos de la flota filtrados por tipo o por patente (para el lector QR).
 */
@Dao
interface VehiculoDao {

    // --- TIPOS DE VEHÍCULO ---

    @Query("SELECT * FROM tipos_vehiculo WHERE activo = 1 ORDER BY id_tipo_vehiculo ASC")
    fun obtenerTiposVehiculo(): Flow<List<TipoVehiculoEntity>>

    @Query("SELECT * FROM tipos_vehiculo WHERE id_tipo_vehiculo = :idTipo LIMIT 1")
    suspend fun obtenerTipoVehiculoPorId(idTipo: Int): TipoVehiculoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTiposVehiculo(tipos: List<TipoVehiculoEntity>)

    @Update
    suspend fun actualizarTipoVehiculo(tipo: TipoVehiculoEntity)

    @Delete
    suspend fun eliminarTipoVehiculo(tipo: TipoVehiculoEntity)

    // --- VEHÍCULOS DE LA FLOTA ---

    @Query("SELECT * FROM vehiculos WHERE activo = 1 ORDER BY id_vehiculo ASC")
    fun obtenerVehiculos(): Flow<List<VehiculoEntity>>

    @Query("SELECT * FROM vehiculos WHERE id_tipo_vehiculo = :idTipo AND activo = 1 ORDER BY id_vehiculo ASC")
    fun obtenerVehiculosPorTipo(idTipo: Int): Flow<List<VehiculoEntity>>

    @Query("SELECT * FROM vehiculos WHERE id_vehiculo = :idVehiculo LIMIT 1")
    suspend fun obtenerVehiculoPorId(idVehiculo: Long): VehiculoEntity?

    /**
     * Busca un vehículo por su patente (ej: "KR-TW-21").
     * Se usa en la Fase 4 cuando el pasajero escanea el código QR pegado en el vehículo.
     */
    @Query("SELECT * FROM vehiculos WHERE UPPER(patente) = UPPER(:patente) LIMIT 1")
    suspend fun obtenerVehiculoPorPatente(patente: String): VehiculoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVehiculos(vehiculos: List<VehiculoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVehiculo(vehiculo: VehiculoEntity): Long

    @Update
    suspend fun actualizarVehiculo(vehiculo: VehiculoEntity)

    @Delete
    suspend fun eliminarVehiculo(vehiculo: VehiculoEntity)
}
