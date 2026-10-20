package cl.belmonte.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para la tabla "cobros" (paquetes mensuales y cobros de km extra por empresa).
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Representa cada cobro emitido por Belmonte SpA a una empresa cliente en un periodo (AAAA-MM):
 * - tipoCobro = "PAQUETE": compra del paquete mensual de cupos al inicio del mes.
 * - tipoCobro = "EXTRAS": suma de los cargos por km extra de viajes FINALIZADOS al cierre del mes (RN-14).
 *
 * NOTA PARA EXTENSIÓN FUTURA (PLANES):
 * Dejamos el campo opcional nombrePlan (por defecto null) por si en la siguiente unidad se agrega
 * el catálogo de Planes (Básico, Grupos, Carga, Personalizado) sin romper las consultas actuales.
 */
@Entity(tableName = "cobros")
data class CobroEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_cobro")
    val idCobro: Long = 0,

    @ColumnInfo(name = "id_empresa")
    val idEmpresa: Long,

    @ColumnInfo(name = "periodo")
    val periodo: String, // Formato "AAAA-MM" (ej: "2026-10")

    @ColumnInfo(name = "tipo_cobro")
    val tipoCobro: String, // "PAQUETE" o "EXTRAS"

    @ColumnInfo(name = "monto")
    val monto: Int, // Monto en pesos chilenos (CLP)

    @ColumnInfo(name = "estado_pago")
    val estadoPago: String = "PENDIENTE", // "PENDIENTE", "PAGADO", "ANULADO"

    @ColumnInfo(name = "fecha_emision")
    val fechaEmision: String,

    @ColumnInfo(name = "fecha_pago")
    val fechaPago: String?
)
