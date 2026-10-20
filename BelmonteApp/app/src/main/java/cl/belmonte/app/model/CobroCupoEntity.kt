package cl.belmonte.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para la tabla "cobro_cupos" (detalle de cupos que incluye un paquete mensual).
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Indica cuántos viajes contratados de cada tipo de vehículo (AUTO, VAN, CAMION) trae el paquete
 * mensual de una empresa.
 * Ejemplo en datos de prueba: Minera Andes Sur (idCobro=1) tiene 30 en AUTO, 20 en VAN y 10 en CAMION.
 */
@Entity(tableName = "cobro_cupos")
data class CobroCupoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_cobro_cupo")
    val idCobroCupo: Long = 0,

    @ColumnInfo(name = "id_cobro")
    val idCobro: Long,

    @ColumnInfo(name = "id_tipo_vehiculo")
    val idTipoVehiculo: Int, // 1 = AUTO, 2 = VAN, 3 = CAMION

    @ColumnInfo(name = "cantidad_viajes")
    val cantidadViajes: Int // Cantidad total contratada por la empresa para ese tipo
)
