package cl.belmonte.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para la tabla "asignaciones_cupo" (reparto de cupos a trabajadores).
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Registra cuántos cupos de una línea del paquete (idCobroCupo) le entregó el Administrador
 * de Empresa a un trabajador específico (idUsuario).
 *
 * ¿CÓMO SE CALCULAN LOS CUPOS DISPONIBLES DEL TRABAJADOR (RN-07)?
 * Cupos disponibles del mes = cantidadViajes asignados - viajes no cancelados del trabajador en el mes.
 * Así, al crear un viaje se consume 1 cupo automáticamente, y si se cancela el viaje (idEstado=5),
 * el cupo vuelve a estar disponible sin riesgo de desincronización.
 */
@Entity(tableName = "asignaciones_cupo")
data class AsignacionCupoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_asignacion")
    val idAsignacion: Long = 0,

    @ColumnInfo(name = "id_cobro_cupo")
    val idCobroCupo: Long,

    @ColumnInfo(name = "id_usuario")
    val idUsuario: Long,

    @ColumnInfo(name = "cantidad_viajes")
    val cantidadViajes: Int,

    @ColumnInfo(name = "fecha_asignacion")
    val fechaAsignacion: String
)
