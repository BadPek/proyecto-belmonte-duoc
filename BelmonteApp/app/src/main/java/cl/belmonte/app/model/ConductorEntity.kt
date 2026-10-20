package cl.belmonte.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para la tabla "conductores" (especialización 1:1 de usuarios).
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Almacena los datos exclusivos de los choferes de Belmonte (clase de licencia profesional,
 * fecha de vencimiento, foto y disponibilidad).
 *
 * ¿POR QUÉ SE HACE ASÍ?
 * Siguiendo el modelo v3, idConductor es el mismo idUsuario de la tabla usuarios (relación 1:1).
 * Así no repetimos rut, nombre, email ni teléfono en dos tablas distintas.
 */
@Entity(tableName = "conductores")
data class ConductorEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_conductor")
    val idConductor: Long, // Mismo ID que usuarios.id_usuario (6, 7, 8, 9 en los datos de prueba)

    @ColumnInfo(name = "clase_licencia")
    val claseLicencia: String, // Ej: A2, A3, A4

    @ColumnInfo(name = "vencimiento_licencia")
    val vencimientoLicencia: String, // Formato YYYY-MM-DD

    @ColumnInfo(name = "foto_url")
    val fotoUrl: String?,

    @ColumnInfo(name = "disponible")
    val disponible: Boolean = true
)
