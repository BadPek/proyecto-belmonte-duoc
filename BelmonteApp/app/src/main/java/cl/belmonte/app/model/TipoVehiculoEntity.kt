package cl.belmonte.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para la tabla "tipos_vehiculo" (catálogo: AUTO, VAN, CAMION).
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Define cada categoría de vehículo que una empresa puede usar con sus cupos mensuales,
 * incluyendo cuántos kilómetros cubre 1 cupo (kmIncluidos) y cuánto cuesta cada kilómetro
 * adicional sobre ese tope (valorKmExtra en CLP).
 *
 * ¿POR QUÉ SE HACE ASÍ?
 * Permite aplicar la Regla de Negocio RN-11 al pedir un viaje:
 * km_extra = max(0, distancia_km - km_incluidos)
 * cargo_extra = km_extra * valor_km_extra
 */
@Entity(tableName = "tipos_vehiculo")
data class TipoVehiculoEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_tipo_vehiculo")
    val idTipoVehiculo: Int, // 1 = AUTO, 2 = VAN, 3 = CAMION

    @ColumnInfo(name = "codigo")
    val codigo: String, // "AUTO", "VAN", "CAMION"

    @ColumnInfo(name = "nombre")
    val nombre: String, // "Auto ejecutivo", "Van", "Camión"

    @ColumnInfo(name = "descripcion")
    val descripcion: String?,

    @ColumnInfo(name = "km_incluidos")
    val kmIncluidos: Int, // 40 km para Auto/Van, 60 km para Camión

    @ColumnInfo(name = "valor_km_extra")
    val valorKmExtra: Int, // $1.100 Auto, $1.600 Van, $2.500 Camión

    @ColumnInfo(name = "activo")
    val activo: Boolean = true
)
