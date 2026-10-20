package cl.belmonte.app.repository

import cl.belmonte.app.dao.CobroDao
import cl.belmonte.app.dao.VehiculoDao
import cl.belmonte.app.dao.ViajeDao
import cl.belmonte.app.model.AsignacionCupoEntity
import cl.belmonte.app.model.CobroEntity
import cl.belmonte.app.model.ResumenCupoEmpresa
import cl.belmonte.app.model.ResumenCupoTrabajador
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repositorio para gestionar Paquetes Mensuales, Reparto de Cupos y Cobros de Extras.
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Implementa las Reglas de Negocio de paquetes corporativos:
 * - RN-07: Calcula los cupos disponibles de un trabajador (asignados - usados no cancelados).
 * - RN-12: Valida que el admin de empresa solo pueda repartir cupos de un paquete PAGADO y que
 *          la suma repartida no supere los cupos contratados del tipo de vehículo.
 * - RN-14: Calcula y emite el cobro EXTRAS de fin de mes sumando los cargos de km extra de
 *          los viajes FINALIZADOS de la empresa.
 */
class CobroRepository(
    private val cobroDao: CobroDao,
    private val vehiculoDao: VehiculoDao,
    private val viajeDao: ViajeDao
) {

    fun obtenerCobrosPorEmpresa(idEmpresa: Long): Flow<List<CobroEntity>> =
        cobroDao.obtenerCobrosPorEmpresa(idEmpresa)

    fun obtenerTodosLosCobros(): Flow<List<CobroEntity>> =
        cobroDao.obtenerTodosLosCobros()

    /**
     * Consulta C del script SQL:
     * Retorna para cada tipo de vehículo (AUTO, VAN, CAMION) cuántos cupos tiene asignados
     * el trabajador en el mes, cuántos ha usado (viajes no cancelados) y cuántos le quedan disponibles.
     */
    suspend fun obtenerResumenCuposTrabajador(
        idUsuario: Long,
        periodo: String = "2026-10"
    ): List<ResumenCupoTrabajador> {
        val tipos = vehiculoDao.obtenerTiposVehiculo().first()
        val prefijoMes = "$periodo%"

        return tipos.map { tipo ->
            val asignados = cobroDao.obtenerCuposAsignadosTrabajadorEnMes(
                idUsuario = idUsuario,
                idTipoVehiculo = tipo.idTipoVehiculo,
                periodo = periodo
            )
            val usados = viajeDao.contarViajesUsadosTrabajadorEnMes(
                idSolicitante = idUsuario,
                idTipoVehiculo = tipo.idTipoVehiculo,
                prefijoMes = prefijoMes
            )
            val disponibles = (asignados - usados).coerceAtLeast(0)

            ResumenCupoTrabajador(
                tipoVehiculo = tipo,
                asignados = asignados,
                usados = usados,
                disponibles = disponibles
            )
        }
    }

    /**
     * Consulta B del script SQL:
     * Retorna para una empresa cliente los cupos contratados en su paquete mensual PAGADO,
     * cuántos ya fueron repartidos entre sus trabajadores y cuántos viajes se han realizado.
     */
    suspend fun obtenerResumenCuposEmpresa(
        idEmpresa: Long,
        periodo: String = "2026-10"
    ): List<ResumenCupoEmpresa> {
        val paquete = cobroDao.obtenerPaquetePagadoEmpresa(idEmpresa, periodo) ?: return emptyList()
        val lineasPaquete = cobroDao.obtenerCobroCuposPorCobro(paquete.idCobro)
        val prefijoMes = "$periodo%"

        return lineasPaquete.mapNotNull { linea ->
            val tipo = vehiculoDao.obtenerTipoVehiculoPorId(linea.idTipoVehiculo) ?: return@mapNotNull null
            val repartidos = cobroDao.sumarCuposRepartidosPorLinea(linea.idCobroCupo)
            val usadosEmpresa = viajeDao.contarViajesUsadosEmpresaEnMes(
                idEmpresa = idEmpresa,
                idTipoVehiculo = linea.idTipoVehiculo,
                prefijoMes = prefijoMes
            )
            ResumenCupoEmpresa(
                idCobroCupo = linea.idCobroCupo,
                tipoVehiculo = tipo,
                contratados = linea.cantidadViajes,
                repartidos = repartidos,
                disponiblesPorRepartir = (linea.cantidadViajes - repartidos).coerceAtLeast(0),
                usadosPorEmpresa = usadosEmpresa
            )
        }
    }

    /**
     * Permite al Administrador de Empresa asignar o modificar la cantidad de cupos de un trabajador (RN-12).
     * Valida que la suma total repartida en esa línea del paquete no supere la cantidad contratada.
     */
    suspend fun repartirCupoATrabajador(
        idCobroCupo: Long,
        idUsuario: Long,
        nuevaCantidadViajes: Int
    ): Result<String> {
        if (nuevaCantidadViajes < 0) {
            return Result.failure(IllegalArgumentException("La cantidad de cupos no puede ser negativa."))
        }

        val lineaPaquete = cobroDao.obtenerCobroCupoPorId(idCobroCupo)
            ?: return Result.failure(IllegalArgumentException("No se encontró la línea del paquete."))

        val asignacionActual = cobroDao.obtenerAsignacionTrabajador(idCobroCupo, idUsuario)
        val totalRepartidoActual = cobroDao.sumarCuposRepartidosPorLinea(idCobroCupo)
        val repartidoOtrosTrabajadores = totalRepartidoActual - (asignacionActual?.cantidadViajes ?: 0)

        // Regla RN-12: la suma repartida a los trabajadores no puede superar los cupos contratados
        if (repartidoOtrosTrabajadores + nuevaCantidadViajes > lineaPaquete.cantidadViajes) {
            val maximoPermitido = lineaPaquete.cantidadViajes - repartidoOtrosTrabajadores
            return Result.failure(
                IllegalStateException(
                    "No puedes asignar $nuevaCantidadViajes cupos. Solo quedan $maximoPermitido cupos sin repartir de los ${lineaPaquete.cantidadViajes} contratados en el paquete (RN-12)."
                )
            )
        }

        val ahora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        if (asignacionActual != null) {
            cobroDao.actualizarAsignacionCupo(
                asignacionActual.copy(
                    cantidadViajes = nuevaCantidadViajes,
                    fechaAsignacion = ahora
                )
            )
        } else {
            cobroDao.insertarAsignacionCupo(
                AsignacionCupoEntity(
                    idCobroCupo = idCobroCupo,
                    idUsuario = idUsuario,
                    cantidadViajes = nuevaCantidadViajes,
                    fechaAsignacion = ahora
                )
            )
        }

        return Result.success("Cupos actualizados correctamente ($nuevaCantidadViajes viajes asignados).")
    }

    /**
     * Calcula y genera (o actualiza) el cobro EXTRAS de cierre de mes para una empresa (RN-14).
     * Suma el cargo_extra de todos los viajes FINALIZADOS (id_estado = 4) del periodo.
     */
    suspend fun generarCobroExtrasMes(
        idEmpresa: Long,
        periodo: String = "2026-10"
    ): Result<String> {
        val prefijoMes = "$periodo%"
        val montoExtras = viajeDao.sumarCargoExtraFinalizadosEmpresa(idEmpresa, prefijoMes)
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val cobroExistente = cobroDao.obtenerCobroExtrasEmpresa(idEmpresa, periodo)
        if (cobroExistente != null) {
            cobroDao.actualizarCobro(
                cobroExistente.copy(
                    monto = montoExtras,
                    fechaEmision = fechaHoy
                )
            )
            return Result.success("Cobro EXTRAS de $periodo actualizado por $$montoExtras CLP.")
        } else {
            cobroDao.insertarCobro(
                CobroEntity(
                    idEmpresa = idEmpresa,
                    periodo = periodo,
                    tipoCobro = "EXTRAS",
                    monto = montoExtras,
                    estadoPago = "PENDIENTE",
                    fechaEmision = fechaHoy,
                    fechaPago = null
                )
            )
            return Result.success("Cobro EXTRAS de $periodo emitido por $$montoExtras CLP (RN-14).")
        }
    }
}
