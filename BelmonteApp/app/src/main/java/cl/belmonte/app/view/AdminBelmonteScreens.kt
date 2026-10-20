package cl.belmonte.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.belmonte.app.ui.theme.AzulBelmonte
import cl.belmonte.app.ui.theme.AzulBelmonteClaro
import cl.belmonte.app.ui.theme.AzulMarinoBelmonte
import cl.belmonte.app.ui.theme.ColorEstadoCancelado
import cl.belmonte.app.ui.theme.ColorEstadoEnCurso
import cl.belmonte.app.ui.theme.ColorEstadoPendiente
import cl.belmonte.app.ui.theme.RojoAccionBelmonte
import cl.belmonte.app.view.components.BadgeEstadoViaje
import cl.belmonte.app.view.components.BelmonteCard
import cl.belmonte.app.viewmodel.BelmonteViewModel

/**
 * Pantallas del rol ADMINISTRADOR CENTRAL BELMONTE (ROLE_ADMIN — Ej: Valeria Fuentes).
 *
 * Incluye:
 * 1. SolicitudesPendientesAdminScreen: panel de despacho con solicitudes pendientes, viajes activos e incidencias.
 * 2. AsignarViajeScreen (CU-03): asigna conductor y vehículo del tipo pedido a una solicitud pendiente.
 * 3. ReasignarViajeScreen (CU-10): sustituye conductor/vehículo ante contingencia o incidencia con motivo obligatorio.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudesPendientesAdminScreen(
    viewModel: BelmonteViewModel,
    onBack: () -> Unit,
    onAsignarViaje: (Long) -> Unit,
    onReasignarViaje: (Long) -> Unit
) {
    val todosLosViajes by viewModel.todosLosViajes.collectAsState()
    val empresas by viewModel.empresas.collectAsState()
    val conductores by viewModel.conductores.collectAsState()
    val vehiculos by viewModel.vehiculos.collectAsState()
    val incidencias by viewModel.todasLasIncidencias.collectAsState()
    val mensajeFeedback by viewModel.mensajeFeedback.collectAsState()
    val esError by viewModel.esErrorFeedback.collectAsState()

    // 0 = Pendientes, 1 = Activos (Asignado/En Curso), 2 = Incidencias, 3 = Todos
    var filtroActivo by remember { mutableIntStateOf(0) }

    val viajesFiltrados = when (filtroActivo) {
        0 -> todosLosViajes.filter { it.idEstado == 1 }
        1 -> todosLosViajes.filter { it.idEstado == 2 || it.idEstado == 3 }
        else -> todosLosViajes
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Despacho Central Belmonte") },
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
            mensajeFeedback?.let { msg ->
                BelmonteCard(colorBorde = if (esError) ColorEstadoCancelado else ColorEstadoEnCurso) {
                    Text(
                        text = msg,
                        color = if (esError) ColorEstadoCancelado else ColorEstadoEnCurso,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Pestañas de filtro del panel de despacho
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = filtroActivo == 0,
                    onClick = { filtroActivo = 0 },
                    label = { Text("Pendientes (${todosLosViajes.count { it.idEstado == 1 }})") }
                )
                FilterChip(
                    selected = filtroActivo == 1,
                    onClick = { filtroActivo = 1 },
                    label = { Text("Activos (${todosLosViajes.count { it.idEstado == 2 || it.idEstado == 3 }})") }
                )
                FilterChip(
                    selected = filtroActivo == 2,
                    onClick = { filtroActivo = 2 },
                    label = { Text("Incidencias (${incidencias.size})") }
                )
                FilterChip(
                    selected = filtroActivo == 3,
                    onClick = { filtroActivo = 3 },
                    label = { Text("Todos") }
                )
            }

            if (filtroActivo == 2) {
                // Vista de Incidencias reportadas por los conductores en ruta (CU-08 -> CU-10)
                Text(
                    text = "Alertas de Incidencias en Ruta",
                    style = MaterialTheme.typography.titleLarge
                )
                incidencias.forEach { inc ->
                    val chofer = conductores.find { it.conductor.idConductor == inc.idConductor }
                    val viajeAfectado = todosLosViajes.find { it.idViaje == inc.idViaje }
                    BelmonteCard(colorBorde = RojoAccionBelmonte) {
                        Text(
                            text = "Incidencia #${inc.idIncidencia} · Viaje #${inc.idViaje} (${inc.tipoIncidencia})",
                            style = MaterialTheme.typography.titleMedium,
                            color = RojoAccionBelmonte
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Reportado por: ${chofer?.usuario?.nombreCompleto ?: "Conductor #${inc.idConductor}"}")
                        Text("Fecha: ${inc.fechaRegistro}")
                        Text("Detalle: ${inc.descripcion}", fontWeight = FontWeight.SemiBold)
                        if (inc.latitud != null && inc.longitud != null) {
                            Text(
                                text = "GPS del reporte: ${inc.latitud}, ${inc.longitud}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (viajeAfectado != null && (viajeAfectado.idEstado == 2 || viajeAfectado.idEstado == 3)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onReasignarViaje(inc.idViaje) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RojoAccionBelmonte)
                            ) {
                                Text("REASIGNAR CONDUCTOR AL VIAJE #${inc.idViaje} (CU-10)")
                            }
                        }
                    }
                }
            } else {
                if (viajesFiltrados.isEmpty()) {
                    BelmonteCard {
                        Text("No hay viajes en este filtro actualmente.")
                    }
                }

                viajesFiltrados.forEach { viaje ->
                    val empresaNombre = empresas.find { it.idEmpresa == viaje.idEmpresa }?.razonSocial
                        ?: "Empresa #${viaje.idEmpresa}"
                    val choferNombre = conductores.find { it.conductor.idConductor == viaje.idConductor }
                        ?.usuario?.nombreCompleto ?: "Sin asignar"
                    val vehiculoPatente = vehiculos.find { it.idVehiculo == viaje.idVehiculo }
                        ?.resumenVehiculo ?: "Sin asignar"

                    BelmonteCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Viaje #${viaje.idViaje} · ${viaje.nombreTipoVehiculo}",
                                style = MaterialTheme.typography.titleMedium,
                                color = AzulBelmonteClaro
                            )
                            BadgeEstadoViaje(codigoEstado = viaje.codigoEstado)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Empresa: $empresaNombre", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Pasajero: ${viaje.nombrePasajero} (${viaje.cantidadPasajeros} pas.)")
                        Text("Fecha: ${viaje.fechaServicio} · Hora: ${viaje.horaServicio}")
                        Text("Origen: ${viaje.direccionOrigen}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Destino: ${viaje.direccionDestino}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (viaje.idConductor != null) {
                            Text("Conductor actual: $choferNombre", color = ColorEstadoEnCurso)
                            Text("Vehículo actual: $vehiculoPatente", color = AzulBelmonteClaro)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (viaje.idEstado == 1) {
                            Button(
                                onClick = { onAsignarViaje(viaje.idViaje) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AzulBelmonte)
                            ) {
                                Text("ASIGNAR CONDUCTOR Y VEHÍCULO (CU-03)")
                            }
                        } else if (viaje.idEstado == 2 || viaje.idEstado == 3) {
                            Button(
                                onClick = { onReasignarViaje(viaje.idViaje) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RojoAccionBelmonte)
                            ) {
                                Text("REASIGNAR CONDUCTOR / VEHÍCULO (CU-10)")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignarViajeScreen(
    idViaje: Long,
    viewModel: BelmonteViewModel,
    onBack: () -> Unit
) {
    val viaje by viewModel.observarViajePorId(idViaje).collectAsState(initial = null)
    val conductores by viewModel.conductores.collectAsState()
    val vehiculos by viewModel.vehiculos.collectAsState()
    val mensajeFeedback by viewModel.mensajeFeedback.collectAsState()
    val esError by viewModel.esErrorFeedback.collectAsState()

    val v = viaje
    // Filtrar vehículos del mismo tipo pedido en el viaje (Regla RN-08)
    val vehiculosDelTipo = vehiculos.filter { it.idTipoVehiculo == (v?.idTipoVehiculo ?: 1) }

    var idConductorSeleccionado by remember { mutableLongStateOf(0L) }
    var idVehiculoSeleccionado by remember { mutableLongStateOf(0L) }

    LaunchedEffect(conductores, vehiculosDelTipo) {
        if (idConductorSeleccionado == 0L && conductores.isNotEmpty()) {
            idConductorSeleccionado = conductores.firstOrNull { it.conductor.disponible }?.conductor?.idConductor
                ?: conductores.first().conductor.idConductor
        }
        if (idVehiculoSeleccionado == 0L && vehiculosDelTipo.isNotEmpty()) {
            idVehiculoSeleccionado = vehiculosDelTipo.first().idVehiculo
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asignar Viaje #$idViaje (CU-03)") },
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
            Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                Text("Cargando viaje #$idViaje...")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Resumen de la solicitud pendiente
            BelmonteCard(colorBorde = ColorEstadoPendiente) {
                Text(
                    text = "Solicitud #${v.idViaje} — Tipo pedido: ${v.nombreTipoVehiculo}",
                    style = MaterialTheme.typography.titleMedium,
                    color = ColorEstadoPendiente
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("Pasajero: ${v.nombrePasajero} (${v.cantidadPasajeros} pasajeros)", fontWeight = FontWeight.Bold)
                Text("Fecha/Hora: ${v.fechaServicio} a las ${v.horaServicio}")
                Text("Origen: ${v.direccionOrigen}")
                Text("Destino: ${v.direccionDestino}")
                if (!v.numeroVuelo.isNullOrBlank()) {
                    Text("Vuelo: ${v.numeroVuelo}", color = AzulBelmonteClaro)
                }
            }

            // 2. Selección de Conductor de Belmonte
            BelmonteCard {
                Text(
                    text = "1. Selecciona el Conductor (RN-04)",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulBelmonteClaro
                )
                Spacer(modifier = Modifier.height(8.dp))

                conductores.forEach { item ->
                    val seleccionado = item.conductor.idConductor == idConductorSeleccionado
                    Surface(
                        onClick = { idConductorSeleccionado = item.conductor.idConductor },
                        shape = RoundedCornerShape(10.dp),
                        color = if (seleccionado) AzulBelmonte.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(if (seleccionado) 2.dp else 1.dp, if (seleccionado) AzulBelmonte else Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.usuario.nombreCompleto, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Licencia ${item.conductor.claseLicencia} · Tel: ${item.usuario.telefono}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = if (item.conductor.disponible) "Disponible" else "En turno",
                                color = if (item.conductor.disponible) ColorEstadoEnCurso else ColorEstadoPendiente,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 3. Selección de Vehículo del mismo tipo pedido (RN-08)
            BelmonteCard {
                Text(
                    text = "2. Selecciona Vehículo de tipo ${v.nombreTipoVehiculo} (RN-08)",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulBelmonteClaro
                )
                Spacer(modifier = Modifier.height(8.dp))

                vehiculosDelTipo.forEach { auto ->
                    val seleccionado = auto.idVehiculo == idVehiculoSeleccionado
                    Surface(
                        onClick = { idVehiculoSeleccionado = auto.idVehiculo },
                        shape = RoundedCornerShape(10.dp),
                        color = if (seleccionado) AzulBelmonte.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(if (seleccionado) 2.dp else 1.dp, if (seleccionado) AzulBelmonte else Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${auto.marca} ${auto.modelo} (${auto.anio}) — Patente: ${auto.patente}",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Color: ${auto.color} · Capacidad máx: ${auto.capacidadPasajeros} pasajeros",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (mensajeFeedback != null && esError) {
                BelmonteCard(colorBorde = ColorEstadoCancelado) {
                    Text(mensajeFeedback ?: "", color = ColorEstadoCancelado, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = {
                    viewModel.asignarConductorYVehiculo(
                        idViaje = v.idViaje,
                        idConductor = idConductorSeleccionado,
                        idVehiculo = idVehiculoSeleccionado,
                        onExito = onBack
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulBelmonte)
            ) {
                Text("CONFIRMAR ASIGNACIÓN (PASA A ESTADO ASIGNADO)")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReasignarViajeScreen(
    idViaje: Long,
    viewModel: BelmonteViewModel,
    onBack: () -> Unit
) {
    val viaje by viewModel.observarViajePorId(idViaje).collectAsState(initial = null)
    val reasignaciones by viewModel.observarReasignacionesPorViaje(idViaje).collectAsState(initial = emptyList())
    val incidenciasViaje by viewModel.observarIncidenciasPorViaje(idViaje).collectAsState(initial = emptyList())
    val conductores by viewModel.conductores.collectAsState()
    val vehiculos by viewModel.vehiculos.collectAsState()
    val mensajeFeedback by viewModel.mensajeFeedback.collectAsState()
    val esError by viewModel.esErrorFeedback.collectAsState()

    val v = viaje
    val vehiculosDelTipo = vehiculos.filter { it.idTipoVehiculo == (v?.idTipoVehiculo ?: 1) }

    var idConductorNuevo by remember { mutableLongStateOf(0L) }
    var idVehiculoNuevo by remember { mutableLongStateOf(0L) }
    var motivo by remember {
        mutableStateOf("Reemplazo operativo por contingencia en ruta informada a la central.")
    }

    LaunchedEffect(v, conductores, vehiculosDelTipo) {
        if (v != null && idConductorNuevo == 0L) {
            idConductorNuevo = conductores.firstOrNull { it.conductor.idConductor != v.idConductor }
                ?.conductor?.idConductor ?: (v.idConductor ?: 6L)
        }
        if (v != null && idVehiculoNuevo == 0L) {
            idVehiculoNuevo = vehiculosDelTipo.firstOrNull()?.idVehiculo ?: (v.idVehiculo ?: 1L)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reasignar Viaje #$idViaje (CU-10)") },
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
            Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                Text("Cargando viaje #$idViaje...")
            }
            return@Scaffold
        }

        val choferActual = conductores.find { it.conductor.idConductor == v.idConductor }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BelmonteCard(colorBorde = RojoAccionBelmonte) {
                Text(
                    text = "Viaje #${v.idViaje} (${v.nombreTipoVehiculo}) — Pasajero: ${v.nombrePasajero}",
                    style = MaterialTheme.typography.titleMedium,
                    color = RojoAccionBelmonte
                )
                Text("Conductor actual: ${choferActual?.usuario?.nombreCompleto ?: "Sin conductor"}")
                Text("Origen: ${v.direccionOrigen} → Destino: ${v.direccionDestino}")

                if (incidenciasViaje.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    incidenciasViaje.forEach { inc ->
                        Text(
                            text = "⚠ Incidencia reportada (${inc.tipoIncidencia}): ${inc.descripcion}",
                            color = ColorEstadoPendiente,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Selección de nuevo conductor sustituto
            BelmonteCard {
                Text(
                    text = "1. Selecciona el Conductor de Reemplazo",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulBelmonteClaro
                )
                Spacer(modifier = Modifier.height(8.dp))

                conductores.forEach { item ->
                    val seleccionado = item.conductor.idConductor == idConductorNuevo
                    Surface(
                        onClick = { idConductorNuevo = item.conductor.idConductor },
                        shape = RoundedCornerShape(10.dp),
                        color = if (seleccionado) AzulBelmonte.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(if (seleccionado) 2.dp else 1.dp, if (seleccionado) AzulBelmonte else Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.usuario.nombreCompleto} (Lic. ${item.conductor.claseLicencia})",
                                fontWeight = FontWeight.Bold
                            )
                            if (item.conductor.idConductor == v.idConductor) {
                                Text("Actual", color = ColorEstadoPendiente)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "2. Vehículo Asignado (${v.nombreTipoVehiculo})",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulBelmonteClaro
                )
                Spacer(modifier = Modifier.height(6.dp))

                vehiculosDelTipo.forEach { auto ->
                    val seleccionado = auto.idVehiculo == idVehiculoNuevo
                    Surface(
                        onClick = { idVehiculoNuevo = auto.idVehiculo },
                        shape = RoundedCornerShape(10.dp),
                        color = if (seleccionado) AzulBelmonte.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(if (seleccionado) 2.dp else 1.dp, if (seleccionado) AzulBelmonte else Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "${auto.marca} ${auto.modelo} — Patente: ${auto.patente}",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    label = { Text("Motivo obligatorio de la reasignación (RN-12)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (mensajeFeedback != null && esError) {
                BelmonteCard(colorBorde = ColorEstadoCancelado) {
                    Text(mensajeFeedback ?: "", color = ColorEstadoCancelado, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = {
                    viewModel.reasignarConductorYVehiculo(
                        idViaje = v.idViaje,
                        idConductorNuevo = idConductorNuevo,
                        idVehiculoNuevo = idVehiculoNuevo,
                        motivo = motivo,
                        onExito = onBack
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RojoAccionBelmonte)
            ) {
                Text("CONFIRMAR REASIGNACIÓN DE CONDUCTOR")
            }

            // Historial de reasignaciones de este viaje
            if (reasignaciones.isNotEmpty()) {
                BelmonteCard {
                    Text(
                        text = "Historial de Reasignaciones del Viaje #${v.idViaje}",
                        style = MaterialTheme.typography.titleMedium,
                        color = AzulBelmonteClaro
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    reasignaciones.forEachIndexed { idx, r ->
                        if (idx > 0) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        }
                        val ant = conductores.find { it.conductor.idConductor == r.idConductorAnterior }?.usuario?.nombreCompleto
                        val nue = conductores.find { it.conductor.idConductor == r.idConductorNuevo }?.usuario?.nombreCompleto
                        Text("Fecha: ${r.fechaReasignacion}", fontWeight = FontWeight.Bold)
                        Text("De: $ant → A: $nue", color = ColorEstadoEnCurso)
                        Text("Motivo: ${r.motivo}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
