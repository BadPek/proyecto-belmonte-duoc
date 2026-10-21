package cl.belmonte.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.belmonte.app.BelmonteApplication
import cl.belmonte.app.model.CobroEntity
import cl.belmonte.app.model.ConductorConUsuario
import cl.belmonte.app.model.EmpresaEntity
import cl.belmonte.app.model.HistorialEstadoEntity
import cl.belmonte.app.model.HistorialReasignacionEntity
import cl.belmonte.app.model.IncidenciaEntity
import cl.belmonte.app.model.ResumenCupoEmpresa
import cl.belmonte.app.model.ResumenCupoTrabajador
import cl.belmonte.app.model.TipoVehiculoEntity
import cl.belmonte.app.model.UsuarioEntity
import cl.belmonte.app.model.VehiculoEntity
import cl.belmonte.app.model.ViajeEntity
import cl.belmonte.app.repository.CobroRepository
import cl.belmonte.app.repository.UsuarioRepository
import cl.belmonte.app.repository.VehiculoRepository
import cl.belmonte.app.repository.ViajeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel principal de BelmonteApp (arquitectura MVVM con StateFlow).
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Actúa como puente entre las pantallas Compose (paquete view) y los Repositorios (paquete repository).
 * Mantiene en memoria el usuario que inició sesión, las listas reactivas provenientes de Room y
 * ejecuta en segundo plano (viewModelScope.launch) todas las acciones de los 4 roles.
 *
 * ¿POR QUÉ INCLUYE UN ViewModelProvider.Factory AL FINAL?
 * Porque el profesor pidió explícitamente NO usar Hilt, Dagger ni Koin. Con el Factory manual
 * le pasamos los repositorios creados en BelmonteApplication al constructor del ViewModel.
 */
class BelmonteViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val vehiculoRepository: VehiculoRepository,
    private val cobroRepository: CobroRepository,
    private val viajeRepository: ViajeRepository
) : ViewModel() {

    // --- ESTADO DE SESIÓN Y MENSAJES DE RETROALIMENTACIÓN ---

    private val _usuarioActual = MutableStateFlow<UsuarioEntity?>(null)
    val usuarioActual: StateFlow<UsuarioEntity?> = _usuarioActual.asStateFlow()

    private val _mensajeFeedback = MutableStateFlow<String?>(null)
    val mensajeFeedback: StateFlow<String?> = _mensajeFeedback.asStateFlow()

    private val _esErrorFeedback = MutableStateFlow(false)
    val esErrorFeedback: StateFlow<Boolean> = _esErrorFeedback.asStateFlow()

    // --- LISTAS REACTIVAS DESDE ROOM (StateFlow) ---

    val todosLosUsuarios: StateFlow<List<UsuarioEntity>> =
        usuarioRepository.obtenerTodosLosUsuarios()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val empresas: StateFlow<List<EmpresaEntity>> =
        usuarioRepository.obtenerEmpresas()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conductores: StateFlow<List<ConductorConUsuario>> =
        usuarioRepository.obtenerConductoresConUsuario()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tiposVehiculo: StateFlow<List<TipoVehiculoEntity>> =
        vehiculoRepository.obtenerTiposVehiculo()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vehiculos: StateFlow<List<VehiculoEntity>> =
        vehiculoRepository.obtenerVehiculos()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todosLosViajes: StateFlow<List<ViajeEntity>> =
        viajeRepository.obtenerTodosLosViajes()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val solicitudesPendientes: StateFlow<List<ViajeEntity>> =
        viajeRepository.obtenerSolicitudesPendientes()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todasLasIncidencias: StateFlow<List<IncidenciaEntity>> =
        viajeRepository.obtenerTodasLasIncidencias()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todosLosCobros: StateFlow<List<CobroEntity>> =
        cobroRepository.obtenerTodosLosCobros()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- ESTADO DE CUPOS DEL TRABAJADOR Y DE LA EMPRESA ---

    private val _cuposTrabajador = MutableStateFlow<List<ResumenCupoTrabajador>>(emptyList())
    val cuposTrabajador: StateFlow<List<ResumenCupoTrabajador>> = _cuposTrabajador.asStateFlow()

    private val _cuposEmpresa = MutableStateFlow<List<ResumenCupoEmpresa>>(emptyList())
    val cuposEmpresa: StateFlow<List<ResumenCupoEmpresa>> = _cuposEmpresa.asStateFlow()

    // --- ESTADO DEL LECTOR QR (FASE 4) ---

    private val _resultadoQr = MutableStateFlow<Pair<Boolean, String>?>(null)
    val resultadoQr: StateFlow<Pair<Boolean, String>?> = _resultadoQr.asStateFlow()

    init {
        // Al crear el ViewModel aseguramos que los datos iniciales estén cargados en Room
        viewModelScope.launch {
            usuarioRepository.asegurarDatosIniciales()
            // Precargamos también el resumen de cupos de Daniela (id=3) y Empresa 1 para vista rápida
            _cuposTrabajador.value = cobroRepository.obtenerResumenCuposTrabajador(3)
            _cuposEmpresa.value = cobroRepository.obtenerResumenCuposEmpresa(1)
        }
    }

    // --- FUNCIONES DE AUTENTICACIÓN (CU-01) ---

    /**
     * Valida correo/RUT y contraseña contra la tabla local de usuarios de Room.
     */
    fun iniciarSesion(
        identificador: String,
        password: String,
        onLoginExitoso: (UsuarioEntity) -> Unit = {}
    ) {
        if (identificador.isBlank() || password.isBlank()) {
            mostrarMensaje("Ingresa tu correo (o RUT) y contraseña.", esError = true)
            return
        }

        viewModelScope.launch {
            val usuario = usuarioRepository.login(identificador, password)
            if (usuario != null) {
                _usuarioActual.value = usuario
                recargarCuposSegunUsuario(usuario)
                limpiarMensaje()
                onLoginExitoso(usuario)
            } else {
                mostrarMensaje("Credenciales incorrectas. Recuerda que la clave de prueba es Password123*", esError = true)
            }
        }
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
        _resultadoQr.value = null
        limpiarMensaje()
    }

    /**
     * Actualiza los StateFlow de cupos según el usuario autenticado (o empresa seleccionada).
     */
    fun recargarCupos() {
        val usuario = _usuarioActual.value ?: return
        viewModelScope.launch {
            recargarCuposSegunUsuario(usuario)
        }
    }

    private suspend fun recargarCuposSegunUsuario(usuario: UsuarioEntity) {
        _cuposTrabajador.value = cobroRepository.obtenerResumenCuposTrabajador(usuario.idUsuario)
        usuario.idEmpresa?.let { idEmp ->
            _cuposEmpresa.value = cobroRepository.obtenerResumenCuposEmpresa(idEmp)
        }
    }

    // --- CONSULTAS ESPECÍFICAS POR ID DE VIAJE ---

    fun observarViajePorId(idViaje: Long): Flow<ViajeEntity?> =
        viajeRepository.observarViajePorId(idViaje)

    fun observarHistorialPorViaje(idViaje: Long): Flow<List<HistorialEstadoEntity>> =
        viajeRepository.obtenerHistorialPorViaje(idViaje)

    fun observarIncidenciasPorViaje(idViaje: Long): Flow<List<IncidenciaEntity>> =
        viajeRepository.obtenerIncidenciasPorViaje(idViaje)

    fun observarReasignacionesPorViaje(idViaje: Long): Flow<List<HistorialReasignacionEntity>> =
        viajeRepository.obtenerReasignacionesPorViaje(idViaje)

    fun calcularDistanciaHaversine(
        latOrigen: Double,
        lonOrigen: Double,
        latDestino: Double,
        lonDestino: Double
    ): Double {
        return viajeRepository.calcularDistanciaHaversineKm(latOrigen, lonOrigen, latDestino, lonDestino)
    }

    // --- ACCIONES DEL SOLICITANTE (CU-02 y Cancelación) ---

    fun crearSolicitudViaje(
        idTipoVehiculo: Int,
        nombrePasajero: String,
        telefonoPasajero: String,
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
        observaciones: String?,
        onExito: (Long) -> Unit = {}
    ) {
        val solicitante = _usuarioActual.value
        if (solicitante == null) {
            mostrarMensaje("Debes iniciar sesión para pedir un viaje.", esError = true)
            return
        }
        if (nombrePasajero.isBlank() || direccionOrigen.isBlank() || direccionDestino.isBlank()) {
            mostrarMensaje("Completa el nombre del pasajero, origen y destino.", esError = true)
            return
        }
        if (idTipoVehiculo == 3 && descripcionCarga.isNullOrBlank()) {
            mostrarMensaje("En viajes de camión es obligatorio indicar la descripción de la carga.", esError = true)
            return
        }

        viewModelScope.launch {
            val resultado = viajeRepository.crearSolicitudViaje(
                idSolicitante = solicitante.idUsuario,
                idTipoVehiculo = idTipoVehiculo,
                nombrePasajero = nombrePasajero,
                telefonoPasajero = telefonoPasajero,
                cantidadPasajeros = cantidadPasajeros,
                descripcionCarga = descripcionCarga,
                fechaServicio = fechaServicio,
                horaServicio = horaServicio,
                direccionOrigen = direccionOrigen,
                latitudOrigen = latitudOrigen,
                longitudOrigen = longitudOrigen,
                direccionDestino = direccionDestino,
                latitudDestino = latitudDestino,
                longitudDestino = longitudDestino,
                numeroVuelo = numeroVuelo,
                observaciones = observaciones
            )

            resultado.fold(
                onSuccess = { idViajeCreado ->
                    recargarCuposSegunUsuario(solicitante)
                    mostrarMensaje("Solicitud #$idViajeCreado creada en estado PENDIENTE (1 cupo consumido).", esError = false)
                    onExito(idViajeCreado)
                },
                onFailure = { error ->
                    mostrarMensaje(error.message ?: "No se pudo crear la solicitud.", esError = true)
                }
            )
        }
    }

    fun cancelarViaje(idViaje: Long, motivo: String = "Cancelado desde la app") {
        val idUsuario = _usuarioActual.value?.idUsuario ?: 3L
        viewModelScope.launch {
            val resultado = viajeRepository.cancelarViaje(idViaje, idUsuario, motivo)
            resultado.fold(
                onSuccess = { msg ->
                    _usuarioActual.value?.let { recargarCuposSegunUsuario(it) }
                    mostrarMensaje(msg, esError = false)
                },
                onFailure = { err ->
                    mostrarMensaje(err.message ?: "No se pudo cancelar el viaje.", esError = true)
                }
            )
        }
    }

    // --- ACCIONES DEL ADMINISTRADOR CENTRAL BELMONTE (CU-03 y CU-10) ---

    fun asignarConductorYVehiculo(
        idViaje: Long,
        idConductor: Long,
        idVehiculo: Long,
        onExito: () -> Unit = {}
    ) {
        val idAdmin = _usuarioActual.value?.idUsuario ?: 1L
        viewModelScope.launch {
            val resultado = viajeRepository.asignarConductorYVehiculo(
                idViaje = idViaje,
                idConductor = idConductor,
                idVehiculo = idVehiculo,
                idAdmin = idAdmin
            )
            resultado.fold(
                onSuccess = { msg ->
                    mostrarMensaje(msg, esError = false)
                    onExito()
                },
                onFailure = { err ->
                    mostrarMensaje(err.message ?: "Error al asignar el viaje.", esError = true)
                }
            )
        }
    }

    fun reasignarConductorYVehiculo(
        idViaje: Long,
        idConductorNuevo: Long,
        idVehiculoNuevo: Long,
        motivo: String,
        onExito: () -> Unit = {}
    ) {
        val idAdmin = _usuarioActual.value?.idUsuario ?: 1L
        viewModelScope.launch {
            val resultado = viajeRepository.reasignarViaje(
                idViaje = idViaje,
                idConductorNuevo = idConductorNuevo,
                idVehiculoNuevo = idVehiculoNuevo,
                idAdmin = idAdmin,
                motivo = motivo
            )
            resultado.fold(
                onSuccess = { msg ->
                    mostrarMensaje(msg, esError = false)
                    onExito()
                },
                onFailure = { err ->
                    mostrarMensaje(err.message ?: "Error al reasignar el viaje.", esError = true)
                }
            )
        }
    }

    // --- ACCIONES DEL CONDUCTOR (CU-05, CU-07 y CU-08) ---

    fun iniciarViaje(idViaje: Long) {
        val idConductor = _usuarioActual.value?.idUsuario ?: 6L
        viewModelScope.launch {
            val resultado = viajeRepository.iniciarViaje(idViaje, idConductor)
            resultado.fold(
                onSuccess = { msg -> mostrarMensaje(msg, esError = false) },
                onFailure = { err -> mostrarMensaje(err.message ?: "No se pudo iniciar el viaje.", esError = true) }
            )
        }
    }

    fun finalizarViaje(idViaje: Long) {
        val idConductor = _usuarioActual.value?.idUsuario ?: 6L
        viewModelScope.launch {
            val resultado = viajeRepository.finalizarViaje(idViaje, idConductor)
            resultado.fold(
                onSuccess = { msg -> mostrarMensaje(msg, esError = false) },
                onFailure = { err -> mostrarMensaje(err.message ?: "No se pudo finalizar el viaje.", esError = true) }
            )
        }
    }

    fun reportarIncidencia(
        idViaje: Long,
        tipoIncidencia: String,
        descripcion: String,
        onExito: () -> Unit = {}
    ) {
        val idConductor = _usuarioActual.value?.idUsuario ?: 6L
        viewModelScope.launch {
            val resultado = viajeRepository.reportarIncidencia(
                idViaje = idViaje,
                idConductor = idConductor,
                tipoIncidencia = tipoIncidencia,
                descripcion = descripcion
            )
            resultado.fold(
                onSuccess = {
                    mostrarMensaje("Incidencia reportada exitosamente. Central informada (CU-08).", esError = false)
                    onExito()
                },
                onFailure = { err ->
                    mostrarMensaje(err.message ?: "Error al reportar la incidencia.", esError = true)
                }
            )
        }
    }

    // --- ACCIONES DEL ADMINISTRADOR DE EMPRESA CLIENTE (Cupos y Cobros) ---

    fun repartirCuposATrabajador(
        idCobroCupo: Long,
        idTrabajador: Long,
        nuevaCantidad: Int
    ) {
        viewModelScope.launch {
            val resultado = cobroRepository.repartirCupoATrabajador(
                idCobroCupo = idCobroCupo,
                idUsuario = idTrabajador,
                nuevaCantidadViajes = nuevaCantidad
            )
            resultado.fold(
                onSuccess = { msg ->
                    _usuarioActual.value?.let { recargarCuposSegunUsuario(it) }
                    mostrarMensaje(msg, esError = false)
                },
                onFailure = { err ->
                    mostrarMensaje(err.message ?: "No se pudo repartir el cupo.", esError = true)
                }
            )
        }
    }

    fun emitirCobroExtrasMes(idEmpresa: Long) {
        viewModelScope.launch {
            val resultado = cobroRepository.generarCobroExtrasMes(idEmpresa, "2026-10")
            resultado.fold(
                onSuccess = { msg -> mostrarMensaje(msg, esError = false) },
                onFailure = { err -> mostrarMensaje(err.message ?: "Error al emitir cobro de extras.", esError = true) }
            )
        }
    }

    /**
     * Contrata un Plan de Suscripción Mensual (Básico, Grupos, Carga o Personalizado) y recarga
     * inmediatamente los cupos contratados de la empresa en el StateFlow.
     */
    fun contratarPlanSuscripcion(
        idEmpresa: Long,
        nombrePlan: String,
        cuposAuto: Int,
        cuposVan: Int,
        cuposCamion: Int,
        precioMensualClp: Int
    ) {
        viewModelScope.launch {
            val resultado = cobroRepository.contratarPlanSuscripcionMensual(
                idEmpresa = idEmpresa,
                nombrePlan = nombrePlan,
                cuposAuto = cuposAuto,
                cuposVan = cuposVan,
                cuposCamion = cuposCamion,
                precioMensualClp = precioMensualClp,
                periodo = "2026-10"
            )
            resultado.fold(
                onSuccess = { msg ->
                    _cuposEmpresa.value = cobroRepository.obtenerResumenCuposEmpresa(idEmpresa)
                    _usuarioActual.value?.let { recargarCuposSegunUsuario(it) }
                    mostrarMensaje(msg, esError = false)
                },
                onFailure = { err ->
                    mostrarMensaje(err.message ?: "No se pudo contratar el plan.", esError = true)
                }
            )
        }
    }

    // --- VALIDACIÓN DE CÓDIGO QR DEL VEHÍCULO (FASE 4) ---

    fun verificarQrVehiculo(idViaje: Long, textoQr: String) {
        viewModelScope.launch {
            val resultado = vehiculoRepository.verificarPatenteQrParaViaje(idViaje, textoQr)
            _resultadoQr.value = resultado
            mostrarMensaje(resultado.second, esError = !resultado.first)
        }
    }

    fun limpiarResultadoQr() {
        _resultadoQr.value = null
    }

    fun mostrarMensaje(mensaje: String, esError: Boolean = false) {
        _mensajeFeedback.value = mensaje
        _esErrorFeedback.value = esError
    }

    fun limpiarMensaje() {
        _mensajeFeedback.value = null
        _esErrorFeedback.value = false
    }

    /**
     * Factory manual para instanciar BelmonteViewModel sin usar Hilt ni Koin.
     */
    companion object {
        fun provideFactory(application: BelmonteApplication): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BelmonteViewModel(
                        usuarioRepository = application.usuarioRepository,
                        vehiculoRepository = application.vehiculoRepository,
                        cobroRepository = application.cobroRepository,
                        viajeRepository = application.viajeRepository
                    ) as T
                }
            }
    }
}
