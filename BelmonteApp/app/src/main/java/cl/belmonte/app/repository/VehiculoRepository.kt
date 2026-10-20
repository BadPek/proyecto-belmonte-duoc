package cl.belmonte.app.repository

import cl.belmonte.app.dao.VehiculoDao
import cl.belmonte.app.dao.ViajeDao
import cl.belmonte.app.model.TipoVehiculoEntity
import cl.belmonte.app.model.VehiculoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para el catálogo de Tipos de Vehículo y la Flota de Belmonte SpA.
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Provee acceso a los tipos de vehículo (AUTO, VAN, CAMION) y a los vehículos físicos,
 * además de incluir la validación de la patente leída por código QR contra el viaje asignado.
 */
class VehiculoRepository(
    private val vehiculoDao: VehiculoDao,
    private val viajeDao: ViajeDao
) {

    fun obtenerTiposVehiculo(): Flow<List<TipoVehiculoEntity>> =
        vehiculoDao.obtenerTiposVehiculo()

    suspend fun obtenerTipoVehiculoPorId(idTipo: Int): TipoVehiculoEntity? =
        vehiculoDao.obtenerTipoVehiculoPorId(idTipo)

    fun obtenerVehiculos(): Flow<List<VehiculoEntity>> =
        vehiculoDao.obtenerVehiculos()

    fun obtenerVehiculosPorTipo(idTipo: Int): Flow<List<VehiculoEntity>> =
        vehiculoDao.obtenerVehiculosPorTipo(idTipo)

    suspend fun obtenerVehiculoPorId(idVehiculo: Long): VehiculoEntity? =
        vehiculoDao.obtenerVehiculoPorId(idVehiculo)

    suspend fun obtenerVehiculoPorPatente(patente: String): VehiculoEntity? =
        vehiculoDao.obtenerVehiculoPorPatente(patente.trim())

    /**
     * Verifica si la patente leída desde el código QR corresponde al vehículo asignado al viaje.
     *
     * @param idViaje ID del viaje del pasajero.
     * @param textoQr Texto leído por la cámara (ej: "KR-TW-21" o "BELMONTE:KR-TW-21").
     * @return Par (esCorrecto: Boolean, mensajeExplicativo: String)
     */
    suspend fun verificarPatenteQrParaViaje(idViaje: Long, textoQr: String): Pair<Boolean, String> {
        val patenteLimpia = textoQr
            .trim()
            .removePrefix("BELMONTE:")
            .removePrefix("PATENTE:")
            .trim()
            .uppercase()

        val viaje = viajeDao.obtenerViajePorId(idViaje)
            ?: return Pair(false, "No se encontró el viaje #$idViaje.")

        val idVehiculoAsignado = viaje.idVehiculo
            ?: return Pair(false, "El viaje #$idViaje aún no tiene un vehículo asignado.")

        val vehiculoAsignado = vehiculoDao.obtenerVehiculoPorId(idVehiculoAsignado)
            ?: return Pair(false, "No se encontró el vehículo asignado en la flota.")

        return if (vehiculoAsignado.patente.equals(patenteLimpia, ignoreCase = true)) {
            Pair(
                true,
                "¡Vehículo confirmado! Patente ${vehiculoAsignado.patente} (${vehiculoAsignado.marca} ${vehiculoAsignado.modelo} - ${vehiculoAsignado.color}) corresponde a tu Viaje #$idViaje."
            )
        } else {
            Pair(
                false,
                "¡Alerta! El QR escaneado ($patenteLimpia) NO corresponde al vehículo asignado a tu Viaje #$idViaje. Tu vehículo asignado es patente ${vehiculoAsignado.patente} (${vehiculoAsignado.marca} ${vehiculoAsignado.modelo})."
            )
        }
    }
}
