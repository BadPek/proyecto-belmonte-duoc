package cl.belmonte.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para la tabla "historial_reasignaciones" (CU-10).
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Guarda el registro de auditoría cuando el Administrador de Belmonte cambia el conductor
 * asignado a un viaje debido a una incidencia en ruta o contingencia operativa.
 */
@Entity(tableName = "historial_reasignaciones")
data class HistorialReasignacionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_reasignacion")
    val idReasignacion: Long = 0,

    @ColumnInfo(name = "id_viaje")
    val idViaje: Long,

    @ColumnInfo(name = "id_conductor_anterior")
    val idConductorAnterior: Long,

    @ColumnInfo(name = "id_conductor_nuevo")
    val idConductorNuevo: Long,

    @ColumnInfo(name = "id_admin")
    val idAdmin: Long,

    @ColumnInfo(name = "motivo")
    val motivo: String,

    @ColumnInfo(name = "fecha_reasignacion")
    val fechaReasignacion: String
)
