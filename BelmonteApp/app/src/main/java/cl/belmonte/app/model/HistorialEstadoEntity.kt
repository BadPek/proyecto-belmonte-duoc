package cl.belmonte.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para la tabla "historial_estados_viaje" (trazabilidad RN-03).
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Registra una fila cada vez que un viaje cambia de estado (quién lo cambió, a qué estado pasó,
 * en qué fecha/hora y con qué observación).
 *
 * ¿POR QUÉ SE HACE ASÍ?
 * Permite reconstruir la línea de tiempo completa del viaje (por ejemplo, la hora real de
 * inicio es la fila EN_CURSO y la hora real de término es la fila FINALIZADO).
 */
@Entity(tableName = "historial_estados_viaje")
data class HistorialEstadoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_historial")
    val idHistorial: Long = 0,

    @ColumnInfo(name = "id_viaje")
    val idViaje: Long,

    @ColumnInfo(name = "id_estado")
    val idEstado: Int,

    @ColumnInfo(name = "id_usuario")
    val idUsuario: Long, // Usuario que realizó el cambio (solicitante, admin o conductor)

    @ColumnInfo(name = "fecha_cambio")
    val fechaCambio: String, // Formato YYYY-MM-DD HH:mm:ss

    @ColumnInfo(name = "observacion")
    val observacion: String?
)
