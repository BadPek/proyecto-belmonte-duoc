package cl.belmonte.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para la tabla "estados_viaje" (catálogo oficial de estados del traslado).
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Define los 5 estados posibles del ciclo de vida de un viaje (RN-02):
 * 1 = PENDIENTE, 2 = ASIGNADO, 3 = EN_CURSO, 4 = FINALIZADO, 5 = CANCELADO.
 */
@Entity(tableName = "estados_viaje")
data class EstadoViajeEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_estado")
    val idEstado: Int,

    @ColumnInfo(name = "codigo")
    val codigo: String, // PENDIENTE, ASIGNADO, EN_CURSO, FINALIZADO, CANCELADO

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "descripcion")
    val descripcion: String?,

    @ColumnInfo(name = "orden")
    val orden: Int
)
