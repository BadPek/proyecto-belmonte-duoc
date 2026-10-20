package cl.belmonte.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para la tabla "incidencias" (CU-08: reportes del conductor en ruta).
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Permite al chofer registrar una emergencia o imprevisto durante un viaje asignado o en curso,
 * incluyendo el tipo de incidencia (COLISION, PANNE_MECANICA, CONGESTION_CRITICA,
 * PASAJERO_NO_SE_PRESENTA, OTRO), descripción, referencia de foto y coordenadas GPS.
 */
@Entity(tableName = "incidencias")
data class IncidenciaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_incidencia")
    val idIncidencia: Long = 0,

    @ColumnInfo(name = "id_viaje")
    val idViaje: Long,

    @ColumnInfo(name = "id_conductor")
    val idConductor: Long,

    @ColumnInfo(name = "tipo_incidencia")
    val tipoIncidencia: String, // COLISION, PANNE_MECANICA, CONGESTION_CRITICA, PASAJERO_NO_SE_PRESENTA, OTRO

    @ColumnInfo(name = "descripcion")
    val descripcion: String,

    @ColumnInfo(name = "foto_url")
    val fotoUrl: String?,

    @ColumnInfo(name = "latitud")
    val latitud: Double?,

    @ColumnInfo(name = "longitud")
    val longitud: Double?,

    @ColumnInfo(name = "fecha_registro")
    val fechaRegistro: String
)
