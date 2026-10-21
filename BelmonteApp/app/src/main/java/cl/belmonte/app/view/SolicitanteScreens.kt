package cl.belmonte.app.view

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AirportShuttle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.belmonte.app.model.LugaresDePrueba
import cl.belmonte.app.ui.theme.AzulBelmonte
import cl.belmonte.app.ui.theme.AzulBelmonteClaro
import cl.belmonte.app.ui.theme.AzulMarinoBelmonte
import cl.belmonte.app.ui.theme.ColorEstadoAsignado
import cl.belmonte.app.ui.theme.ColorEstadoCancelado
import cl.belmonte.app.ui.theme.ColorEstadoEnCurso
import cl.belmonte.app.ui.theme.ColorEstadoPendiente
import cl.belmonte.app.ui.theme.RojoAccionBelmonte
import cl.belmonte.app.view.components.BadgeEstadoViaje
import cl.belmonte.app.view.components.BelmonteCard
import cl.belmonte.app.view.components.ChipVehiculoBelmonte
import cl.belmonte.app.view.components.LineaRutaTimeline
import cl.belmonte.app.view.components.MapaRutaVisualCard
import cl.belmonte.app.viewmodel.BelmonteViewModel
import kotlin.math.roundToInt

/**
 * Pantallas del rol SOLICITANTE (ROLE_SOLICITANTE — Ej: Daniela Muñoz o Fernanda Castro).
 *
 * Incluye:
 * 1. NuevaSolicitudScreen (CU-02): formulario para pedir Auto, Van o Camión consumiendo 1 cupo.
 * 2. MisViajesSolicitanteScreen: listado de viajes del trabajador y opción de cancelar (devuelve cupo).
 * 3. DetalleSeguimientoScreen (CU-09): seguimiento con datos del conductor/vehículo, llamada directa,
 *    historial de estados y botón para Escanear QR del Vehículo.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaSolicitudScreen(
    viewModel: BelmonteViewModel,
    onBack: () -> Unit,
    onSolicitudCreada: () -> Unit
) {
    val usuario by viewModel.usuarioActual.collectAsState()
    val cupos by viewModel.cuposTrabajador.collectAsState()
    val mensajeFeedback by viewModel.mensajeFeedback.collectAsState()
    val esError by viewModel.esErrorFeedback.collectAsState()

    LaunchedEffect(usuario?.idUsuario) {
        viewModel.recargarCupos()
    }

    // Estados del formulario de nueva solicitud (CU-02)
    var idTipoSeleccionado by remember { mutableIntStateOf(1) } // 1=AUTO, 2=VAN, 3=CAMION
    var nombrePasajero by remember { mutableStateOf(usuario?.nombreCompleto ?: "Daniela Muñoz Pino") }
    var telefonoPasajero by remember { mutableStateOf(usuario?.telefono ?: "+56900000003") }
    var cantidadPasajeros by remember { mutableStateOf("1") }
    var descripcionCarga by remember { mutableStateOf("") }
    var fechaServicio by remember { mutableStateOf("2026-10-22") }
    var horaServicio by remember { mutableStateOf("08:30") }

    // Coordenadas de prueba iniciales: Oficina Las Condes -> Aeropuerto AMB
    val origenInicial = LugaresDePrueba.lista[0]
    val destinoInicial = LugaresDePrueba.lista[1]

    var direccionOrigen by remember { mutableStateOf(origenInicial.direccion) }
    var latOrigen by remember { mutableDoubleStateOf(origenInicial.latitud) }
    var lngOrigen by remember { mutableDoubleStateOf(origenInicial.longitud) }

    var direccionDestino by remember { mutableStateOf(destinoInicial.direccion) }
    var latDestino by remember { mutableDoubleStateOf(destinoInicial.latitud) }
    var lngDestino by remember { mutableDoubleStateOf(destinoInicial.longitud) }

    var numeroVuelo by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    // Cálculo en vivo de distancia Haversine y km extra según el tipo de vehículo elegido (RN-11)
    val resumenCupoActual = cupos.find { it.tipoVehiculo.idTipoVehiculo == idTipoSeleccionado }
    val topeKm = resumenCupoActual?.tipoVehiculo?.kmIncluidos ?: if (idTipoSeleccionado == 3) 60 else 40
    val tarifaKmExtra = resumenCupoActual?.tipoVehiculo?.valorKmExtra ?: 1100
    val distanciaKm = viewModel.calcularDistanciaHaversine(latOrigen, lngOrigen, latDestino, lngDestino)
    val kmExtra = ((distanciaKm - topeKm).coerceAtLeast(0.0) * 100.0).roundToInt() / 100.0
    val cargoExtraEstimado = (kmExtra * tarifaKmExtra).roundToInt()
    val cuposDisponiblesTipo = resumenCupoActual?.disponibles ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Solicitud de Traslado (CU-02)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulMarinoBelmonte,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Selección de tipo de vehículo mostrando cupos disponibles
            BelmonteCard(colorBorde = AzulBelmonte) {
                Text(
                    text = "1. Elige el Tipo de Vehículo (Consume 1 Cupo)",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulBelmonteClaro
                )
                Spacer(modifier = Modifier.height(8.dp))

                cupos.forEach { item ->
                    val seleccionado = item.tipoVehiculo.idTipoVehiculo == idTipoSeleccionado
                    val iconoTipo = when (item.tipoVehiculo.idTipoVehiculo) {
                        1 -> Icons.Default.DirectionsCar
                        2 -> Icons.Default.AirportShuttle
                        else -> Icons.Default.LocalShipping
                    }

                    Surface(
                        onClick = {
                            idTipoSeleccionado = item.tipoVehiculo.idTipoVehiculo
                            if (item.tipoVehiculo.idTipoVehiculo == 3) {
                                cantidadPasajeros = "0"
                            } else if (cantidadPasajeros == "0") {
                                cantidadPasajeros = "1"
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (seleccionado) AzulBelmonte.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            width = if (seleccionado) 2.dp else 1.dp,
                            color = if (seleccionado) AzulBelmonte else Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(iconoTipo, contentDescription = null, tint = AzulBelmonteClaro)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = item.tipoVehiculo.nombre,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Incluye ${item.tipoVehiculo.kmIncluidos} km (Extra: $${item.tipoVehiculo.valorKmExtra}/km)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "${item.disponibles} disp.",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (item.disponibles > 0) ColorEstadoEnCurso else ColorEstadoCancelado
                            )
                        }
                    }
                }
            }

            // 2. Datos del pasajero o carga (si es camión)
            BelmonteCard {
                Text(
                    text = if (idTipoSeleccionado == 3) "2. Datos de Recepción y Carga (Camión)" else "2. Datos del Pasajero",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulBelmonteClaro
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nombrePasajero,
                    onValueChange = { nombrePasajero = it },
                    label = { Text(if (idTipoSeleccionado == 3) "Nombre de quien recibe la carga" else "Nombre completo del pasajero") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = telefonoPasajero,
                        onValueChange = { telefonoPasajero = it },
                        label = { Text("Teléfono") },
                        singleLine = true,
                        modifier = Modifier.weight(1.4f)
                    )
                    if (idTipoSeleccionado != 3) {
                        OutlinedTextField(
                            value = cantidadPasajeros,
                            onValueChange = { cantidadPasajeros = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Pasajeros") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Si el vehículo elegido es CAMION (id=3), pedir descripción de la carga
                if (idTipoSeleccionado == 3) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = descripcionCarga,
                        onValueChange = { descripcionCarga = it },
                        label = { Text("Descripción de la carga (obligatorio en Camión)") },
                        placeholder = { Text("Ej: Repuestos y herramientas para faena (500 kg)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = fechaServicio,
                        onValueChange = { fechaServicio = it },
                        label = { Text("Fecha (AAAA-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = horaServicio,
                        onValueChange = { horaServicio = it },
                        label = { Text("Hora (HH:mm)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Origen y Destino georreferenciados + cálculo Haversine en vivo
            BelmonteCard {
                Text(
                    text = "3. Origen y Destino Georreferenciados",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulBelmonteClaro
                )
                Text(
                    text = "Selecciona un punto rápido de prueba o edita la dirección:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Punto de Origen:", fontWeight = FontWeight.SemiBold)
                LugaresDePrueba.lista.take(4).forEach { lugar ->
                    FilterChip(
                        selected = direccionOrigen == lugar.direccion,
                        onClick = {
                            direccionOrigen = lugar.direccion
                            latOrigen = lugar.latitud
                            lngOrigen = lugar.longitud
                        },
                        label = { Text(lugar.nombreCorto) },
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = direccionOrigen,
                    onValueChange = { direccionOrigen = it },
                    label = { Text("Dirección de Origen") },
                    supportingText = { Text("Coordenadas GPS: $latOrigen, $lngOrigen") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text("Punto de Destino:", fontWeight = FontWeight.SemiBold)
                LugaresDePrueba.lista.drop(1).forEach { lugar ->
                    FilterChip(
                        selected = direccionDestino == lugar.direccion,
                        onClick = {
                            direccionDestino = lugar.direccion
                            latDestino = lugar.latitud
                            lngDestino = lugar.longitud
                        },
                        label = { Text(lugar.nombreCorto) },
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = direccionDestino,
                    onValueChange = { direccionDestino = it },
                    label = { Text("Dirección de Destino") },
                    supportingText = { Text("Coordenadas GPS: $latDestino, $lngDestino") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = numeroVuelo,
                        onValueChange = { numeroVuelo = it },
                        label = { Text("N° Vuelo (opcional)") },
                        placeholder = { Text("Ej: LA452") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = observaciones,
                        onValueChange = { observaciones = it },
                        label = { Text("Observaciones") },
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))

                // Resumen del cálculo automático de distancia y km extra (RN-11) con mapa visual
                Text(
                    text = "Vista Previa de Ruta y Cálculo Haversine (RN-11):",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AzulBelmonteClaro
                )
                Spacer(modifier = Modifier.height(8.dp))
                MapaRutaVisualCard(
                    latOrigen = latOrigen,
                    lngOrigen = lngOrigen,
                    latDestino = latDestino,
                    lngDestino = lngDestino,
                    distanciaKm = distanciaKm
                )
                Spacer(modifier = Modifier.height(8.dp))
                LineaRutaTimeline(
                    origen = direccionOrigen,
                    destino = direccionDestino,
                    distanciaKm = distanciaKm,
                    kmExtra = kmExtra,
                    cargoExtra = cargoExtraEstimado
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tope incluido: $topeKm km • El trabajador no paga nada (consume 1 cupo mensual).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (mensajeFeedback != null && esError) {
                BelmonteCard(colorBorde = ColorEstadoCancelado) {
                    Text(
                        text = mensajeFeedback ?: "",
                        color = ColorEstadoCancelado,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = {
                    val cant = cantidadPasajeros.toIntOrNull() ?: 1
                    viewModel.crearSolicitudViaje(
                        idTipoVehiculo = idTipoSeleccionado,
                        nombrePasajero = nombrePasajero,
                        telefonoPasajero = telefonoPasajero,
                        cantidadPasajeros = cant,
                        descripcionCarga = descripcionCarga,
                        fechaServicio = fechaServicio,
                        horaServicio = horaServicio,
                        direccionOrigen = direccionOrigen,
                        latitudOrigen = latOrigen,
                        longitudOrigen = lngOrigen,
                        direccionDestino = direccionDestino,
                        latitudDestino = latDestino,
                        longitudDestino = lngDestino,
                        numeroVuelo = numeroVuelo,
                        observaciones = observaciones,
                        onExito = { onSolicitudCreada() }
                    )
                },
                enabled = cuposDisponiblesTipo > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RojoAccionBelmonte)
            ) {
                Text(
                    text = if (cuposDisponiblesTipo > 0) {
                        "CONFIRMAR SOLICITUD (CONSUME 1 CUPO)"
                    } else {
                        "SIN CUPOS DISPONIBLES DE ESTE TIPO (RN-07)"
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisViajesSolicitanteScreen(
    viewModel: BelmonteViewModel,
    onBack: () -> Unit,
    onNuevaSolicitud: () -> Unit,
    onVerDetalle: (Long) -> Unit,
    onEscanearQr: (Long) -> Unit
) {
    val usuario by viewModel.usuarioActual.collectAsState()
    val todosLosViajes by viewModel.todosLosViajes.collectAsState()
    val mensajeFeedback by viewModel.mensajeFeedback.collectAsState()
    val esError by viewModel.esErrorFeedback.collectAsState()

    val idSolicitante = usuario?.idUsuario ?: 3L
    val misViajes = todosLosViajes.filter { it.idSolicitante == idSolicitante }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Viajes Solicitados") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onNuevaSolicitud) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva Solicitud", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulMarinoBelmonte,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            mensajeFeedback?.let { msg ->
                BelmonteCard(colorBorde = if (esError) ColorEstadoCancelado else ColorEstadoEnCurso) {
                    Text(
                        text = msg,
                        color = if (esError) ColorEstadoCancelado else ColorEstadoEnCurso,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = onNuevaSolicitud,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulBelmonte)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("PEDIR NUEVO TRASLADO CORPORATIVO")
            }

            if (misViajes.isEmpty()) {
                BelmonteCard {
                    Text("Aún no tienes solicitudes de traslado registradas.")
                }
            }

            misViajes.forEach { viaje ->
                BelmonteCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Viaje #${viaje.idViaje}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            ChipVehiculoBelmonte(
                                idTipoVehiculo = viaje.idTipoVehiculo,
                                textoPersonalizado = viaje.nombreTipoVehiculo
                            )
                        }
                        BadgeEstadoViaje(codigoEstado = viaje.codigoEstado)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Pasajero: ${viaje.nombrePasajero} • ${viaje.fechaServicio} (${viaje.horaServicio})", fontWeight = FontWeight.SemiBold)

                    Spacer(modifier = Modifier.height(8.dp))
                    LineaRutaTimeline(
                        origen = viaje.direccionOrigen,
                        destino = viaje.direccionDestino,
                        distanciaKm = viaje.distanciaKm,
                        kmExtra = viaje.kmExtra,
                        cargoExtra = viaje.cargoExtra
                    )

                    if (!viaje.numeroVuelo.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Vuelo: ${viaje.numeroVuelo}",
                            color = ColorEstadoAsignado,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!viaje.descripcionCarga.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Carga: ${viaje.descripcionCarga}",
                            color = ColorEstadoPendiente,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onVerDetalle(viaje.idViaje) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AzulBelmonte)
                        ) {
                            Text("Ver Seguimiento", fontWeight = FontWeight.Bold)
                        }

                        if (viaje.idVehiculo != null) {
                            OutlinedButton(
                                onClick = { onEscanearQr(viaje.idViaje) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Validar QR")
                            }
                        }
                    }

                    // Cancelación solo desde PENDIENTE (1) o ASIGNADO (2) según RN-02
                    if (viaje.idEstado == 1 || viaje.idEstado == 2) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.cancelarViaje(
                                    idViaje = viaje.idViaje,
                                    motivo = "Cancelado por el solicitante desde Mis Viajes"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, RojoAccionBelmonte)
                        ) {
                            Text(
                                text = "Cancelar Solicitud (Devuelve 1 Cupo)",
                                color = RojoAccionBelmonte,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleSeguimientoScreen(
    idViaje: Long,
    viewModel: BelmonteViewModel,
    onBack: () -> Unit,
    onEscanearQr: (Long) -> Unit
) {
    val context = LocalContext.current
    val viaje by viewModel.observarViajePorId(idViaje).collectAsState(initial = null)
    val historial by viewModel.observarHistorialPorViaje(idViaje).collectAsState(initial = emptyList())
    val conductores by viewModel.conductores.collectAsState()
    val vehiculos by viewModel.vehiculos.collectAsState()
    val todosUsuarios by viewModel.todosLosUsuarios.collectAsState()

    val v = viaje
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seguimiento Viaje #$idViaje (CU-09)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulMarinoBelmonte,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (v == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp)
            ) {
                Text("Cargando datos del viaje #$idViaje...")
            }
            return@Scaffold
        }

        val conductorAsignado = conductores.find { it.conductor.idConductor == v.idConductor }
        val vehiculoAsignado = vehiculos.find { it.idVehiculo == v.idVehiculo }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Resumen general del traslado + Mapa visual
            BelmonteCard(colorBorde = AzulBelmonte) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Viaje #${v.idViaje}",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        ChipVehiculoBelmonte(
                            idTipoVehiculo = v.idTipoVehiculo,
                            textoPersonalizado = v.nombreTipoVehiculo
                        )
                    }
                    BadgeEstadoViaje(codigoEstado = v.codigoEstado)
                }
                Spacer(modifier = Modifier.height(10.dp))
                MapaRutaVisualCard(
                    latOrigen = v.latitudOrigen,
                    lngOrigen = v.longitudOrigen,
                    latDestino = v.latitudDestino,
                    lngDestino = v.longitudDestino,
                    distanciaKm = v.distanciaKm
                )
                Spacer(modifier = Modifier.height(10.dp))
                LineaRutaTimeline(
                    origen = v.direccionOrigen,
                    destino = v.direccionDestino,
                    distanciaKm = v.distanciaKm,
                    kmExtra = v.kmExtra,
                    cargoExtra = v.cargoExtra
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Pasajero: ${v.nombrePasajero} (${v.cantidadPasajeros} pas.) • ${v.fechaServicio} ${v.horaServicio}", fontWeight = FontWeight.SemiBold)
                if (!v.numeroVuelo.isNullOrBlank()) {
                    Text("Vuelo Asociado: ${v.numeroVuelo}", color = AzulBelmonteClaro, fontWeight = FontWeight.Bold)
                }
                if (!v.descripcionCarga.isNullOrBlank()) {
                    Text("Carga: ${v.descripcionCarga}", color = ColorEstadoPendiente, fontWeight = FontWeight.SemiBold)
                }
            }

            // 2. Tarjeta de Conductor y Vehículo asignado (CU-09 — RN-11)
            BelmonteCard {
                Text(
                    text = "Conductor y Vehículo Asignado",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulBelmonteClaro
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (conductorAsignado == null || vehiculoAsignado == null) {
                    Text(
                        text = "Solicitud en cola de despacho. Conductor y vehículo por asignar por la central de Belmonte.",
                        color = ColorEstadoPendiente,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "Chofer: ${conductorAsignado.usuario.nombreCompleto}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Licencia Profesional Clase ${conductorAsignado.conductor.claseLicencia} · Teléfono: ${conductorAsignado.usuario.telefono}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Vehículo: ${vehiculoAsignado.marca} ${vehiculoAsignado.modelo} (${vehiculoAsignado.anio}) - Color ${vehiculoAsignado.color}",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Patente Oficial (QR): ${vehiculoAsignado.patente}",
                        style = MaterialTheme.typography.titleMedium,
                        color = ColorEstadoEnCurso
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón para llamar al conductor mediante Intent telefónico (CU-09)
                    Button(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse("tel:${conductorAsignado.usuario.telefono}")
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulBelmonte)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("LLAMAR AL CONDUCTOR (${conductorAsignado.usuario.telefono})")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón para abrir el lector de código QR del vehículo (Fase 4)
                    Button(
                        onClick = { onEscanearQr(v.idViaje) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RojoAccionBelmonte)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ESCANEAR QR DE LA PATENTE (${vehiculoAsignado.patente})")
                    }
                }
            }

            // 3. Línea de tiempo / Historial de Estados del Viaje (Consulta G del script SQL)
            BelmonteCard {
                Text(
                    text = "Historial de Estados del Viaje (Trazabilidad RN-03)",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulBelmonteClaro
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (historial.isEmpty()) {
                    Text("Sin registros en el historial.")
                } else {
                    historial.forEachIndexed { idx, h ->
                        if (idx > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                        val nombreAutor = todosUsuarios.find { it.idUsuario == h.idUsuario }?.nombreCompleto
                            ?: "Usuario #${h.idUsuario}"
                        val codigoEst = when (h.idEstado) {
                            1 -> "PENDIENTE"
                            2 -> "ASIGNADO"
                            3 -> "EN_CURSO"
                            4 -> "FINALIZADO"
                            5 -> "CANCELADO"
                            else -> "ESTADO ${h.idEstado}"
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BadgeEstadoViaje(codigoEstado = codigoEst)
                            Text(
                                text = h.fechaCambio,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Realizado por: $nombreAutor", fontWeight = FontWeight.SemiBold)
                        if (!h.observacion.isNullOrBlank()) {
                            Text(
                                text = "Nota: ${h.observacion}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
