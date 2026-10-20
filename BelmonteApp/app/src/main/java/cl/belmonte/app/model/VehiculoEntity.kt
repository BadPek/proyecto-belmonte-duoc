package cl.belmonte.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para la tabla "vehiculos" (flota propia de Belmonte SpA).
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Representa cada vehículo físico de la flota con su patente única (ej: KR-TW-21, LK-JH-44,
 * PX-RT-89, JT-KL-52), su tipo (auto, van o camión), marca, modelo y capacidad de pasajeros.
 *
 * ¿POR QUÉ SE HACE ASÍ?
 * Se utiliza tanto cuando el Admin de Belmonte asigna un vehículo del tipo pedido (RN-08)
 * como cuando el pasajero escanea el código QR con la patente del vehículo (Fase 4).
 */
@Entity(tableName = "vehiculos")
data class VehiculoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_vehiculo")
    val idVehiculo: Long = 0,

    @ColumnInfo(name = "id_tipo_vehiculo")
    val idTipoVehiculo: Int, // FK lógica a tipos_vehiculo (1=AUTO, 2=VAN, 3=CAMION)

    @ColumnInfo(name = "patente")
    val patente: String, // Patente en el código QR: KR-TW-21, LK-JH-44, PX-RT-89, JT-KL-52

    @ColumnInfo(name = "marca")
    val marca: String,

    @ColumnInfo(name = "modelo")
    val modelo: String,

    @ColumnInfo(name = "anio")
    val anio: Int,

    @ColumnInfo(name = "color")
    val color: String,

    @ColumnInfo(name = "capacidad_pasajeros")
    val capacidadPasajeros: Int,

    @ColumnInfo(name = "activo")
    val activo: Boolean = true
) {
    /** Descripción legible para mostrar en listas desplegables y tarjetas de seguimiento. */
    val resumenVehiculo: String
        get() = "$marca $modelo ($patente) - $color"
}
