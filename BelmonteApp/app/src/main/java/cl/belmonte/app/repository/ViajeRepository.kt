package cl.belmonte.app.repository

import cl.belmonte.app.dao.CobroDao
import cl.belmonte.app.dao.UsuarioDao
import cl.belmonte.app.dao.VehiculoDao
import cl.belmonte.app.dao.ViajeDao
import cl.belmonte.app.model.HistorialEstadoEntity
import cl.belmonte.app.model.HistorialReasignacionEntity
import cl.belmonte.app.model.IncidenciaEntity
import cl.belmonte.app.model.ViajeEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Repositorio central para la operación de Viajes, Estados, Historial, Reasignaciones e Incidencias.
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Contiene y aplica todas las Reglas de Negocio operativas del caso Belmonte SpA:
 * - RN-02: Transiciones válidas de estado: PENDIENTE (1) -> ASIGNADO (2) -> EN_CURSO (3) -> FINALIZADO (4).
 *          CANCELADO (5) solo se permite desde PENDIENTE (1) o ASIGNADO (2).
 * - RN-03: Cada cambio de estado inserta automáticamente una fila en historial_estados_viaje.
 * - RN-04 y RN-08: Al asignar o reasignar, valida que el vehículo sea del mismo tipo pedido y tenga
 *                  capacidad suficiente para la cantidad de pasajeros.
 * - RN-07: Consume 1 cupo disponible al crear el viaje; no permite pedir si disponibles <= 0;
 *          y devuelve el cupo automáticamente al pasar a CANCELADO.
 * - RN-10: Copia idEmpresa del solicitante al crear el viaje.
 * - RN-11: Calcula distanciaKm con la fórmula de Haversine, kmExtra = max(0, distancia - tope)
 *          y cargoExtra = kmExtra * valorKmExtra.
 */
class ViajeRepository(
    private val viajeDao: ViajeDao,
    private val vehiculoDao: VehiculoDao,
    private val cobroDao: CobroDao,
    private val usuarioDao: UsuarioDao
) {

    // --- CONSULTAS REACTIVAS (Flow) ---

    fun obtenerTodosLosViajes(): Flow<List<ViajeEntity>> =
        viajeDao.obtenerTodosLosViajes()

    fun obtenerViajesPorSolicitante(idSolicitante: Long): Flow<List<ViajeEntity>> =
        viajeDao.obtenerViajesPorSolicitante(idSolicitante)

    fun obtenerViajesPorConductor(idConductor: Long): Flow<List<ViajeEntity>> =
        viajeDao.obtenerViajesPorConductor(idConductor)

    fun obtenerSolicitudesPendientes(): Flow<List<ViajeEntity>> =
        viajeDao.obtenerViajesPorEstado(1) // 1 = PENDIENTE

    fun obtenerViajesPorEmpresa(idEmpresa: Long): Flow<List<ViajeEntity>> =
        viajeDao.obtenerViajesPorEmpresa(idEmpresa)

    fun observarViajePorId(idViaje: Long): Flow<ViajeEntity?> =
        viajeDao.observarViajePorId(idViaje)

    suspend fun obtenerViajePorId(idViaje: Long): ViajeEntity? =
        viajeDao.obtenerViajePorId(idViaje)

    fun obtenerHistorialPorViaje(idViaje: Long): Flow<List<HistorialEstadoEntity>> =
        viajeDao.obtenerHistorialPorViaje(idViaje)

    fun obtenerReasignacionesPorViaje(idViaje: Long): Flow<List<HistorialReasignacionEntity>> =
        viajeDao.obtenerReasignacionesPorViaje(idViaje)

    fun obtenerIncidenciasPorViaje(idViaje: Long): Flow<List<IncidenciaEntity>> =
        viajeDao.obtenerIncidenciasPorViaje(idViaje)

    fun obtenerTodasLasIncidencias(): Flow<List<IncidenciaEntity>> =
        viajeDao.obtenerTodasLasIncidencias()

    // --- CÁLCULO DE DISTANCIA HAVERSINE Y KM EXTRA (RN-11) ---

    /**
     * Calcula la distancia en línea recta (en kilómetros, redondeada a 2 decimales) entre dos
     * coordenadas GPS (latitud/longitud) usando la fórmula matemática de Haversine.
     */
    fun calcularDistanciaHaversineKm(
        latOrigen: Double,
        lonOrigen: Double,
        latDestino: Double,
        lonDestino: Double
    ): Double {
        val radioTierraKm = 6371.0
        val dLat = Math.toRadians(latDestino - latOrigen)
        val dLon = Math.toRadians(lonDestino - lonOrigen)

        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(latOrigen)) * cos(Math.toRadians(latDestino)) *
                sin(dLon / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distancia = radioTierraKm * c
        return (distancia * 100.0).roundToInt() / 100.0
    }

    // --- CREAR NUEVA SOLICITUD DE VIAJE (CU-02) ---

    /**
     * Registra una nueva solicitud de traslado corporativo aplicando RN-07, RN-10, RN-11 y RN-03.
     */
    suspend fun crearSolicitudViaje(
        idSolicitante: Long,
        idTipoVehiculo: Int,
        nombrePasajero: String,
        telefonoPasajero: String?,
        cantidadPasajeros: Int,
        descripcionCarga: String?,
        fechaServicio: String,
        horaServicio: String,
        direccionOrigen: String,
        latitudOrigen: Double,
        longitudOrigen: Double,
        direccionDestino: String,
        latitudDestino: Double,
        longitudDestino: Double,
        numeroVuelo: String?,
        observaciones: String?
    ): Result<Long> {
        // 1. Verificar que el solicitante exista y pertenezca a una empresa cliente (RN-05, RN-10)
        val solicitante = usuarioDao.obtenerUsuarioPorId(idSolicitante)
            ?: return Result.failure(IllegalArgumentException("No se encontró el usuario solicitante."))
        val idEmpresa = solicitante.idEmpresa
            ?: return Result.failure(IllegalStateException("El usuario no tiene una empresa asociada (RN-05)."))

        // 2. Obtener el periodo AAAA-MM a partir de fechaServicio (ej: "2026-10-15" -> "2026-10")
        val periodo = if (fechaServicio.length >= 7) fechaServicio.substring(0, 7) else "2026-10"
        val prefijoMes = "$periodo%"

        // 3. Validar disponibilidad de cupo del tipo de vehículo pedido en ese mes (RN-07)
        val cuposAsignados = cobroDao.obtenerCuposAsignadosTrabajadorEnMes(
            idUsuario = idSolicitante,
            idTipoVehiculo = idTipoVehiculo,
            periodo = periodo
        )
        val cuposUsados = viajeDao.contarViajesUsadosTrabajadorEnMes(
            idSolicitante = idSolicitante,
            idTipoVehiculo = idTipoVehiculo,
            prefijoMes = prefijoMes
        )
        val cuposDisponibles = cuposAsignados - cuposUsados

        if (cuposDisponibles <= 0) {
            return Result.failure(
                IllegalStateException(
                    "No tienes cupos disponibles para este tipo de vehículo en el periodo $periodo (Asignados: $cuposAsignados, Usados: $cuposUsados). Regla RN-07."
                )
            )
        }

        // 4. Obtener tarifa y tope de km del tipo de vehículo y calcular distancia / km extra (RN-11)
        val tipoVehiculo = vehiculoDao.obtenerTipoVehiculoPorId(idTipoVehiculo)
            ?: return Result.failure(IllegalArgumentException("Tipo de vehículo inválido."))

        val distanciaKm = calcularDistanciaHaversineKm(
            latOrigen = latitudOrigen,
            lonOrigen = longitudOrigen,
            latDestino = latitudDestino,
            lonDestino = longitudDestino
        )
        val kmExtraBruto = (distanciaKm - tipoVehiculo.kmIncluidos).coerceAtLeast(0.0)
        val kmExtra = (kmExtraBruto * 100.0).roundToInt() / 100.0
        val cargoExtra = (kmExtra * tipoVehiculo.valorKmExtra).roundToInt()

        val ahora = obtenerFechaHoraActual()

        // 5. Insertar el viaje en estado 1 = PENDIENTE
        val nuevoViaje = ViajeEntity(
            idSolicitante = idSolicitante,
            idEmpresa = idEmpresa,
            idTipoVehiculo = idTipoVehiculo,
            idConductor = null,
            idVehiculo = null,
            idEstado = 1, // PENDIENTE
            nombrePasajero = nombrePasajero.trim(),
            telefonoPasajero = telefonoPasajero?.trim()?.ifEmpty { null },
            cantidadPasajeros = if (idTipoVehiculo == 3) 0 else cantidadPasajeros,
            descripcionCarga = if (idTipoVehiculo == 3) descripcionCarga?.trim() else null,
            fechaServicio = fechaServicio.trim(),
            horaServicio = if (horaServicio.length == 5) "${horaServicio.trim()}:00" else horaServicio.trim(),
            direccionOrigen = direccionOrigen.trim(),
            latitudOrigen = latitudOrigen,
            longitudOrigen = longitudOrigen,
            direccionDestino = direccionDestino.trim(),
            latitudDestino = latitudDestino,
            longitudDestino = longitudDestino,
            numeroVuelo = numeroVuelo?.trim()?.ifEmpty { null },
            observaciones = observaciones?.trim()?.ifEmpty { null },
            distanciaKm = distanciaKm,
            kmExtra = kmExtra,
            cargoExtra = cargoExtra,
            creadoEn = ahora
        )

        val idViajeGenerado = viajeDao.insertarViaje(nuevoViaje)

        // 6. Registrar la creación en historial_estados_viaje (RN-03)
        viajeDao.insertarHistorialEstado(
            HistorialEstadoEntity(
                idViaje = idViajeGenerado,
                idEstado = 1, // PENDIENTE
                idUsuario = idSolicitante,
                fechaCambio = ahora,
                observacion = "Solicitud creada desde la app"
            )
        )

        return Result.success(idViajeGenerado)
    }

    // --- ASIGNAR CONDUCTOR Y VEHÍCULO (CU-03) ---

    /**
     * Asigna un conductor y un vehículo del tipo pedido a un viaje PENDIENTE (RN-02, RN-03, RN-04, RN-08).
     */
    suspend fun asignarConductorYVehiculo(
        idViaje: Long,
        idConductor: Long,
        idVehiculo: Long,
        idAdmin: Long
    ): Result<String> {
        val viaje = viajeDao.obtenerViajePorId(idViaje)
            ?: return Result.failure(IllegalArgumentException("No se encontró el viaje #$idViaje."))

        if (viaje.idEstado != 1) {
            return Result.failure(
                IllegalStateException("Solo se pueden asignar viajes que estén en estado PENDIENTE (RN-02).")
            )
        }

        val conductorUsuario = usuarioDao.obtenerUsuarioPorId(idConductor)
            ?: return Result.failure(IllegalArgumentException("No se encontró el conductor seleccionado."))

        val vehiculo = vehiculoDao.obtenerVehiculoPorId(idVehiculo)
            ?: return Result.failure(IllegalArgumentException("No se encontró el vehículo seleccionado."))

        // Regla RN-08: el vehículo debe ser del tipo pedido y soportar la cantidad de pasajeros
        if (vehiculo.idTipoVehiculo != viaje.idTipoVehiculo) {
            return Result.failure(
                IllegalStateException("El vehículo seleccionado no corresponde al tipo pedido en la solicitud (RN-08).")
            )
        }
        if (viaje.cantidadPasajeros > vehiculo.capacidadPasajeros) {
            return Result.failure(
                IllegalStateException(
                    "El vehículo tiene capacidad para ${vehiculo.capacidadPasajeros} pasajeros y el viaje solicita ${viaje.cantidadPasajeros} (RN-08)."
                )
            )
        }

        val ahora = obtenerFechaHoraActual()

        // Actualizar viaje a estado 2 = ASIGNADO
        viajeDao.actualizarViaje(
            viaje.copy(
                idConductor = idConductor,
                idVehiculo = idVehiculo,
                idEstado = 2
            )
        )

        // Guardar trazabilidad en historial_estados_viaje (RN-03)
        viajeDao.insertarHistorialEstado(
            HistorialEstadoEntity(
                idViaje = idViaje,
                idEstado = 2, // ASIGNADO
                idUsuario = idAdmin,
                fechaCambio = ahora,
                observacion = "Asignado a ${conductorUsuario.nombreCompleto} / ${vehiculo.patente}"
            )
        )

        return Result.success("Viaje #$idViaje asignado a ${conductorUsuario.nombreCompleto} (${vehiculo.patente}).")
    }

    // --- REASIGNAR CONDUCTOR Y VEHÍCULO (CU-10) ---

    /**
     * Reasigna el conductor (y vehículo) de un viaje por contingencia o incidencia (CU-10, RN-12).
     */
    suspend fun reasignarViaje(
        idViaje: Long,
        idConductorNuevo: Long,
        idVehiculoNuevo: Long,
        idAdmin: Long,
        motivo: String
    ): Result<String> {
        if (motivo.isBlank()) {
            return Result.failure(IllegalArgumentException("Debe ingresar el motivo de la reasignación."))
        }

        val viaje = viajeDao.obtenerViajePorId(idViaje)
            ?: return Result.failure(IllegalArgumentException("No se encontró el viaje #$idViaje."))

        if (viaje.idEstado != 2 && viaje.idEstado != 3) {
            return Result.failure(
                IllegalStateException("Solo se pueden reasignar viajes en estado ASIGNADO o EN_CURSO.")
            )
        }

        val idConductorAnterior = viaje.idConductor
            ?: return Result.failure(IllegalStateException("El viaje no tenía conductor previo para reasignar."))

        val conductorNuevo = usuarioDao.obtenerUsuarioPorId(idConductorNuevo)
            ?: return Result.failure(IllegalArgumentException("Conductor de reemplazo no encontrado."))

        val vehiculoNuevo = vehiculoDao.obtenerVehiculoPorId(idVehiculoNuevo)
            ?: return Result.failure(IllegalArgumentException("Vehículo de reemplazo no encontrado."))

        if (vehiculoNuevo.idTipoVehiculo != viaje.idTipoVehiculo) {
            return Result.failure(
                IllegalStateException("El vehículo de reemplazo debe ser del mismo tipo pedido en el viaje (RN-08).")
            )
        }

        val ahora = obtenerFechaHoraActual()

        // Actualizar conductor y vehículo en el viaje
        viajeDao.actualizarViaje(
            viaje.copy(
                idConductor = idConductorNuevo,
                idVehiculo = idVehiculoNuevo
            )
        )

        // Registrar auditoría en historial_reasignaciones (CU-10)
        viajeDao.insertarReasignacion(
            HistorialReasignacionEntity(
                idViaje = idViaje,
                idConductorAnterior = idConductorAnterior,
                idConductorNuevo = idConductorNuevo,
                idAdmin = idAdmin,
                motivo = motivo.trim(),
                fechaReasignacion = ahora
            )
        )

        // Registrar también en historial_estados_viaje (RN-03)
        viajeDao.insertarHistorialEstado(
            HistorialEstadoEntity(
                idViaje = idViaje,
                idEstado = viaje.idEstado,
                idUsuario = idAdmin,
                fechaCambio = ahora,
                observacion = "Reasignado a ${conductorNuevo.nombreCompleto} / ${vehiculoNuevo.patente}: ${motivo.trim()}"
            )
        )

        return Result.success("Viaje #$idViaje reasignado exitosamente a ${conductorNuevo.nombreCompleto}.")
    }

    // --- INICIAR VIAJE (CU-05) ---

    /**
     * Cambia el estado del viaje de ASIGNADO (2) a EN_CURSO (3) y registra la hora real de inicio.
     */
    suspend fun iniciarViaje(idViaje: Long, idUsuarioConductor: Long): Result<String> {
        val viaje = viajeDao.obtenerViajePorId(idViaje)
            ?: return Result.failure(IllegalArgumentException("No se encontró el viaje #$idViaje."))

        if (viaje.idEstado != 2) {
            return Result.failure(
                IllegalStateException("El viaje solo puede iniciarse si está en estado ASIGNADO (RN-02).")
            )
        }

        val ahora = obtenerFechaHoraActual()
        viajeDao.actualizarViaje(viaje.copy(idEstado = 3)) // 3 = EN_CURSO
        viajeDao.insertarHistorialEstado(
            HistorialEstadoEntity(
                idViaje = idViaje,
                idEstado = 3,
                idUsuario = idUsuarioConductor,
                fechaCambio = ahora,
                observacion = "Viaje iniciado por el conductor"
            )
        )
        return Result.success("Viaje #$idViaje en curso.")
    }

    // --- FINALIZAR VIAJE (CU-07) ---

    /**
     * Cambia el estado del viaje de EN_CURSO (3) a FINALIZADO (4) y registra la hora real de término.
     */
    suspend fun finalizarViaje(idViaje: Long, idUsuarioConductor: Long): Result<String> {
        val viaje = viajeDao.obtenerViajePorId(idViaje)
            ?: return Result.failure(IllegalArgumentException("No se encontró el viaje #$idViaje."))

        if (viaje.idEstado != 3) {
            return Result.failure(
                IllegalStateException("El viaje solo puede finalizarse si está en estado EN_CURSO (RN-02).")
            )
        }

        val ahora = obtenerFechaHoraActual()
        viajeDao.actualizarViaje(viaje.copy(idEstado = 4)) // 4 = FINALIZADO
        viajeDao.insertarHistorialEstado(
            HistorialEstadoEntity(
                idViaje = idViaje,
                idEstado = 4,
                idUsuario = idUsuarioConductor,
                fechaCambio = ahora,
                observacion = "Viaje finalizado exitosamente"
            )
        )
        return Result.success("Traslado #$idViaje completado con éxito.")
    }

    // --- CANCELAR VIAJE (RN-02 y RN-07) ---

    /**
     * Cancela una solicitud de traslado si aún no se ha iniciado (solo desde PENDIENTE o ASIGNADO).
     * Al pasar a estado CANCELADO (5), el cupo vuelve automáticamente a estar disponible (RN-07).
     */
    suspend fun cancelarViaje(
        idViaje: Long,
        idUsuario: Long,
        motivoCancelacion: String = "Solicitud cancelada por el usuario"
    ): Result<String> {
        val viaje = viajeDao.obtenerViajePorId(idViaje)
            ?: return Result.failure(IllegalArgumentException("No se encontró el viaje #$idViaje."))

        if (viaje.idEstado != 1 && viaje.idEstado != 2) {
            return Result.failure(
                IllegalStateException("Solo se puede cancelar un viaje en estado PENDIENTE o ASIGNADO (RN-02).")
            )
        }

        val ahora = obtenerFechaHoraActual()
        viajeDao.actualizarViaje(viaje.copy(idEstado = 5)) // 5 = CANCELADO
        viajeDao.insertarHistorialEstado(
            HistorialEstadoEntity(
                idViaje = idViaje,
                idEstado = 5,
                idUsuario = idUsuario,
                fechaCambio = ahora,
                observacion = motivoCancelacion.trim()
            )
        )
        return Result.success("Viaje #$idViaje cancelado. El cupo ha sido devuelto a tu cuenta (RN-07).")
    }

    // --- REPORTAR INCIDENCIA EN RUTA (CU-08) ---

    /**
     * Registra una incidencia reportada por el conductor durante un viaje.
     */
    suspend fun reportarIncidencia(
        idViaje: Long,
        idConductor: Long,
        tipoIncidencia: String,
        descripcion: String,
        fotoUrl: String? = "captura_camara_conductor.jpg",
        latitud: Double? = -33.4116000,
        longitud: Double? = -70.5843000
    ): Result<Long> {
        if (descripcion.isBlank()) {
            return Result.failure(IllegalArgumentException("Por favor escribe una descripción breve de la incidencia."))
        }

        val ahora = obtenerFechaHoraActual()
        val idIncidencia = viajeDao.insertarIncidencia(
            IncidenciaEntity(
                idViaje = idViaje,
                idConductor = idConductor,
                tipoIncidencia = tipoIncidencia,
                descripcion = descripcion.trim(),
                fotoUrl = fotoUrl,
                latitud = latitud,
                longitud = longitud,
                fechaRegistro = ahora
            )
        )
        return Result.success(idIncidencia)
    }

    private fun obtenerFechaHoraActual(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }
}
