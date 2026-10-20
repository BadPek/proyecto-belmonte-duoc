package cl.belmonte.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad central Room para la tabla "viajes" (estructura exigida por el Anexo 3.2 y modelo v3).
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Representa una solicitud de traslado corporativo desde que se pide (PENDIENTE) hasta que
 * finaliza (FINALIZADO) o se cancela (CANCELADO).
 *
 * ¿POR QUÉ GUARDA idEmpresa, distanciaKm, kmExtra y cargoExtra?
 * - idEmpresa se copia del solicitante al crear el viaje y nunca cambia (RN-10).
 * - distanciaKm (Haversine), kmExtra y cargoExtra se calculan con la tarifa vigente del tipo de
 *   vehículo al momento de pedir el viaje (RN-11) para que si después sube la tarifa, los viajes
 *   históricos no cambien su valor.
 */
@Entity(tableName = "viajes")
data class ViajeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_viaje")
    val idViaje: Long = 0,

    @ColumnInfo(name = "id_solicitante")
    val idSolicitante: Long,

    @ColumnInfo(name = "id_empresa")
    val idEmpresa: Long,

    @ColumnInfo(name = "id_tipo_vehiculo")
    val idTipoVehiculo: Int, // 1 = AUTO, 2 = VAN, 3 = CAMION

    @ColumnInfo(name = "id_conductor")
    val idConductor: Long?, // NULL mientras el viaje está PENDIENTE

    @ColumnInfo(name = "id_vehiculo")
    val idVehiculo: Long?, // NULL mientras el viaje está PENDIENTE

    @ColumnInfo(name = "id_estado")
    val idEstado: Int = 1, // 1=PENDIENTE, 2=ASIGNADO, 3=EN_CURSO, 4=FINALIZADO, 5=CANCELADO

    @ColumnInfo(name = "nombre_pasajero")
    val nombrePasajero: String,

    @ColumnInfo(name = "telefono_pasajero")
    val telefonoPasajero: String?,

    @ColumnInfo(name = "cantidad_pasajeros")
    val cantidadPasajeros: Int = 1, // 0 en viajes de carga (CAMION)

    @ColumnInfo(name = "descripcion_carga")
    val descripcionCarga: String?, // Solo para viajes de tipo CAMION

    @ColumnInfo(name = "fecha_servicio")
    val fechaServicio: String, // Formato YYYY-MM-DD (su mes AAAA-MM define de qué paquete sale el cupo)

    @ColumnInfo(name = "hora_servicio")
    val horaServicio: String, // Formato HH:mm:ss

    @ColumnInfo(name = "direccion_origen")
    val direccionOrigen: String,

    @ColumnInfo(name = "latitud_origen")
    val latitudOrigen: Double,

    @ColumnInfo(name = "longitud_origen")
    val longitudOrigen: Double,

    @ColumnInfo(name = "direccion_destino")
    val direccionDestino: String,

    @ColumnInfo(name = "latitud_destino")
    val latitudDestino: Double,

    @ColumnInfo(name = "longitud_destino")
    val longitudDestino: Double,

    @ColumnInfo(name = "numero_vuelo")
    val numeroVuelo: String?, // Opcional, para traslados de aeropuerto

    @ColumnInfo(name = "observaciones")
    val observaciones: String?,

    @ColumnInfo(name = "distancia_km")
    val distanciaKm: Double, // Calculada en línea recta con la fórmula de Haversine

    @ColumnInfo(name = "km_extra")
    val kmExtra: Double = 0.0, // Km sobre el tope del tipo de vehículo

    @ColumnInfo(name = "cargo_extra")
    val cargoExtra: Int = 0, // CLP = km_extra * valor_km_extra (se cobra a fin de mes si finaliza)

    @ColumnInfo(name = "creado_en")
    val creadoEn: String
) {
    /** Devuelve el código textual del estado actual para mostrarlo directamente en BadgeEstadoViaje. */
    val codigoEstado: String
        get() = when (idEstado) {
            1 -> "PENDIENTE"
            2 -> "ASIGNADO"
            3 -> "EN_CURSO"
            4 -> "FINALIZADO"
            5 -> "CANCELADO"
            else -> "PENDIENTE"
        }

    /** Devuelve el nombre legible del tipo de vehículo pedido. */
    val nombreTipoVehiculo: String
        get() = when (idTipoVehiculo) {
            1 -> "Auto Ejecutivo"
            2 -> "Van (Grupos)"
            3 -> "Camión (Carga)"
            else -> "Vehículo"
        }
}
