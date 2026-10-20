package cl.belmonte.app.view

import android.content.ActivityNotFoundException
import android.content.Context
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.belmonte.app.ui.theme.AzulBelmonte
import cl.belmonte.app.ui.theme.AzulBelmonteClaro
import cl.belmonte.app.ui.theme.AzulMarinoBelmonte
import cl.belmonte.app.ui.theme.ColorEstadoCancelado
import cl.belmonte.app.ui.theme.ColorEstadoEnCurso
import cl.belmonte.app.ui.theme.ColorEstadoPendiente
import cl.belmonte.app.ui.theme.ColorGoogleMaps
import cl.belmonte.app.ui.theme.ColorWaze
import cl.belmonte.app.ui.theme.RojoAccionBelmonte
import cl.belmonte.app.view.components.BadgeEstadoViaje
import cl.belmonte.app.view.components.BelmonteCard
import cl.belmonte.app.viewmodel.BelmonteViewModel

/**
 * Pantallas del rol CONDUCTOR (ROLE_CONDUCTOR — Ej: Miguel Rojas ID=6 o Pedro Soto ID=7).
 *
 * Incluye:
 * 1. ViajesConductorScreen (CU-04): listado de viajes del día asignados al chofer (Pantalla 2 de maquetas_belmonte.html).
 * 2. DetalleViajeConductorScreen (CU-05, CU-06, CU-07): iniciar/finalizar viaje y navegación externa con
 *    Android Intent a Google Maps y Waze usando coordenadas lat/lng (Pantalla 3 de maquetas_belmonte.html).
 * 3. ReportarIncidenciaScreen (CU-08): formulario para reportar panne, congestión, colisión o inasistencia
 *    (Pantalla 4 de maquetas_belmonte.html).
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViajesConductorScreen(
    viewModel: BelmonteViewModel,
    onBack: () -> Unit,
    onVerDetalleViaje: (Long) -> Unit
) {
    val usuario by viewModel.usuarioActual.collectAsState()
    val todosLosViajes by viewModel.todosLosViajes.collectAsState()
    val vehiculos by viewModel.vehiculos.collectAsState()
    val mensajeFeedback by viewModel.mensajeFeedback.collectAsState()
    val esError by viewModel.esErrorFeedback.collectAsState()

    var mostrarSoloMisViajes by remember { mutableStateOf(true) }

    val idConductorActual = usuario?.idUsuario ?: 6L
    val listaViajes = if (mostrarSoloMisViajes) {
        todosLosViajes.filter { it.idConductor == idConductorActual }
    } else {
        todosLosViajes.filter { it.idConductor != null }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Viajes de Hoy (CU-04)") },
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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = mostrarSoloMisViajes,
                    onClick = { mostrarSoloMisViajes = true },
                    label = { Text("Mis Asignados (${todosLosViajes.count { it.idConductor == idConductorActual }})") }
                )
                FilterChip(
                    selected = !mostrarSoloMisViajes,
                    onClick = { mostrarSoloMisViajes = false },
                    label = { Text("Toda la Flota") }
                )
            }

            if (listaViajes.isEmpty()) {
                BelmonteCard {
                    Text("No tienes servicios asignados para hoy.")
                }
            }

            listaViajes.forEach { viaje ->
                val auto = vehiculos.find { it.idVehiculo == viaje.idVehiculo }
                BelmonteCard(
                    colorBorde = if (viaje.idEstado == 3) ColorEstadoEnCurso else AzulBelmonte
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Viaje #${viaje.idViaje} · ${viaje.nombreTipoVehiculo}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        BadgeEstadoViaje(codigoEstado = viaje.codigoEstado)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Pasajero: ${viaje.nombrePasajero}", fontWeight = FontWeight.Bold)
                    Text("Origen: ${viaje.direccionOrigen}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Destino: ${viaje.direccionDestino}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Fecha y Hora: ${viaje.fechaServicio} — ${viaje.horaServicio} hrs", color = AzulBelmonteClaro)
                    if (auto != null) {
                        Text("Vehículo: ${auto.marca} ${auto.modelo} (${auto.patente})")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onVerDetalleViaje(viaje.idViaje) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulBelmonte)
                    ) {
                        Text("VER DETALLE Y NAVEGACIÓN")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleViajeConductorScreen(
    idViaje: Long,
    viewModel: BelmonteViewModel,
    onBack: () -> Unit,
    onReportarIncidencia: (Long) -> Unit
) {
    val context = LocalContext.current
    val viaje by viewModel.observarViajePorId(idViaje).collectAsState(initial = null)
    val vehiculos by viewModel.vehiculos.collectAsState()
    val mensajeFeedback by viewModel.mensajeFeedback.collectAsState()
    val esError by viewModel.esErrorFeedback.collectAsState()

    // Diálogos de confirmación exigidos por las fichas CU-05 y CU-07
    var mostrarDialogoIniciar by remember { mutableStateOf(false) }
    var mostrarDialogoFinalizar by remember { mutableStateOf(false) }

    val v = viaje
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle Viaje #$idViaje") },
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
                Text("Cargando detalle del viaje #$idViaje...")
            }
            return@Scaffold
        }

        val auto = vehiculos.find { it.idVehiculo == v.idVehiculo }

        // Diálogo de confirmación para Iniciar Viaje (CU-05)
        if (mostrarDialogoIniciar) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoIniciar = false },
                title = { Text("Iniciar Traslado (CU-05)") },
                text = { Text("¿Deseas iniciar este traslado? El estado cambiará a EN CURSO y se registrará la hora real de inicio en el historial.") },
                confirmButton = {
                    Button(
                        onClick = {
                            mostrarDialogoIniciar = false
                            viewModel.iniciarViaje(v.idViaje)
                        }
                    ) {
                        Text("Sí, Iniciar Viaje")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoIniciar = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Diálogo de confirmación para Finalizar Viaje (CU-07)
        if (mostrarDialogoFinalizar) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoFinalizar = false },
                title = { Text("Finalizar Traslado (CU-07)") },
                text = { Text("¿Confirmas que llegaste al destino acordado? El viaje pasará al estado FINALIZADO.") },
                confirmButton = {
                    Button(
                        onClick = {
                            mostrarDialogoFinalizar = false
                            viewModel.finalizarViaje(v.idViaje)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorGoogleMaps)
                    ) {
                        Text("Sí, Finalizar Viaje")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoFinalizar = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

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

            // 1. Encabezado del servicio (inspirado en Pantalla 3 de maquetas_belmonte.html)
            BelmonteCard(colorBorde = AzulBelmonte) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Traslado Corporativo #${v.idViaje}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    BadgeEstadoViaje(codigoEstado = v.codigoEstado)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Pasajero: ${v.nombrePasajero} (${v.cantidadPasajeros} pas.)", fontWeight = FontWeight.Bold)
                Text("Fecha/Hora: ${v.fechaServicio} · ${v.horaServicio} hrs")
                if (auto != null) {
                    Text("Vehículo: ${auto.marca} ${auto.modelo} — Patente: ${auto.patente}", color = AzulBelmonteClaro)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("O", color = RojoAccionBelmonte, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Origen: ${v.direccionOrigen} (${v.latitudOrigen}, ${v.longitudOrigen})")
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("D", color = ColorEstadoEnCurso, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Destino: ${v.direccionDestino} (${v.latitudDestino}, ${v.longitudDestino})")
                }

                if (!v.numeroVuelo.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Vuelo: ${v.numeroVuelo}",
                        color = AzulBelmonteClaro,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (!v.descripcionCarga.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Carga: ${v.descripcionCarga}",
                        color = ColorEstadoPendiente,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (!v.observaciones.isNullOrBlank()) {
                    Text("Observaciones: ${v.observaciones}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (!v.telefonoPasajero.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${v.telefonoPasajero}"))
                            context.startActivity(dialIntent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Llamar Pasajero (${v.telefonoPasajero})")
                    }
                }
            }

            // 2. Integración con Google Maps y Waze mediante Android Intent (CU-06)
            BelmonteCard {
                Text(
                    text = "Navegación Satelital Externa (Intent Maps / Waze — CU-06)",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulBelmonteClaro
                )
                Text(
                    text = "Abre la ruta directamente con las coordenadas GPS del traslado:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            abrirNavegacionConIntent(
                                context = context,
                                latitud = v.latitudOrigen,
                                longitud = v.longitudOrigen,
                                etiqueta = v.direccionOrigen,
                                preferirWaze = false
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulBelmonte)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ir al Origen")
                    }

                    Button(
                        onClick = {
                            abrirNavegacionConIntent(
                                context = context,
                                latitud = v.latitudDestino,
                                longitud = v.longitudDestino,
                                etiqueta = v.direccionDestino,
                                preferirWaze = false
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulBelmonte)
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ir al Destino")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botones de la grilla de acción de la maqueta: Waze + Incidencia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val lat = if (v.idEstado == 2) v.latitudOrigen else v.latitudDestino
                            val lng = if (v.idEstado == 2) v.longitudOrigen else v.longitudDestino
                            val dir = if (v.idEstado == 2) v.direccionOrigen else v.direccionDestino
                            abrirNavegacionConIntent(
                                context = context,
                                latitud = lat,
                                longitud = lng,
                                etiqueta = dir,
                                preferirWaze = true
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorWaze)
                    ) {
                        Text("Waze", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onReportarIncidencia(v.idViaje) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RojoAccionBelmonte)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Incidencia")
                    }
                }
            }

            // 3. Botón principal de Iniciar Viaje (CU-05) o Finalizar Viaje (CU-07)
            when (v.idEstado) {
                2 -> {
                    Button(
                        onClick = { mostrarDialogoIniciar = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulBelmonte)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("INICIAR VIAJE (PASAR A EN CURSO — CU-05)")
                    }
                }

                3 -> {
                    Button(
                        onClick = { mostrarDialogoFinalizar = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorGoogleMaps)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FINALIZAR VIAJE (CU-07)")
                    }
                }

                4 -> {
                    BelmonteCard(colorBorde = ColorEstadoEnCurso) {
                        Text(
                            text = "Este traslado ya se encuentra FINALIZADO.",
                            color = ColorEstadoEnCurso,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Función que construye y lanza un Android Intent implícito para abrir Google Maps o Waze
 * con las coordenadas georreferenciadas del viaje (CU-06 — RN-08 de los Casos de Uso).
 *
 * Si el emulador no tiene instalada una aplicación nativa de mapas, captura
 * ActivityNotFoundException y abre la ubicación en el navegador web (Flujo Alternativo A1).
 */
private fun abrirNavegacionConIntent(
    context: Context,
    latitud: Double,
    longitud: Double,
    etiqueta: String,
    preferirWaze: Boolean
) {
    try {
        if (preferirWaze) {
            val wazeUri = Uri.parse("https://waze.com/ul?ll=$latitud,$longitud&navigate=yes")
            val wazeIntent = Intent(Intent.ACTION_VIEW, wazeUri)
            context.startActivity(wazeIntent)
        } else {
            val uriGeo = Uri.parse("geo:$latitud,$longitud?q=$latitud,$longitud(${Uri.encode(etiqueta)})")
            val mapIntent = Intent(Intent.ACTION_VIEW, uriGeo)
            context.startActivity(mapIntent)
        }
    } catch (_: ActivityNotFoundException) {
        // Fallback A1: abrir Google Maps en el navegador si el emulador no tiene app de mapas
        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitud,$longitud")
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportarIncidenciaScreen(
    idViaje: Long,
    viewModel: BelmonteViewModel,
    onBack: () -> Unit
) {
    val incidenciasViaje by viewModel.observarIncidenciasPorViaje(idViaje).collectAsState(initial = emptyList())

    // Catálogo oficial del CHECK SQL en la tabla incidencias
    val tiposIncidencia = listOf(
        "PANNE_MECANICA" to "Panne Mecánica",
        "CONGESTION_CRITICA" to "Congestión Crítica / Retraso",
        "PASAJERO_NO_SE_PRESENTA" to "Pasajero No se Presenta",
        "COLISION" to "Colisión / Accidente",
        "OTRO" to "Otro Imprevisto en Ruta"
    )

    var tipoSeleccionado by remember { mutableStateOf(tiposIncidencia[0].first) }
    var descripcion by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportar Problema (CU-08)") },
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
            // Banner de alerta roja igual a la Pantalla 4 de maquetas_belmonte.html
            BelmonteCard(colorBorde = RojoAccionBelmonte) {
                Text(
                    text = "Reportando incidencia para Viaje #$idViaje.",
                    style = MaterialTheme.typography.titleMedium,
                    color = RojoAccionBelmonte
                )
                Text(
                    text = "Se adjuntará automáticamente la ubicación GPS actual y marca de tiempo para la central de Belmonte.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            BelmonteCard {
                Text(
                    text = "Tipo de Incidencia",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulBelmonteClaro
                )
                Spacer(modifier = Modifier.height(8.dp))

                tiposIncidencia.forEach { (codigo, etiqueta) ->
                    val seleccionado = tipoSeleccionado == codigo
                    Surface(
                        onClick = { tipoSeleccionado = codigo },
                        shape = RoundedCornerShape(10.dp),
                        color = if (seleccionado) RojoAccionBelmonte.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            if (seleccionado) 2.dp else 1.dp,
                            if (seleccionado) RojoAccionBelmonte else Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "$etiqueta ($codigo)",
                            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción Breve") },
                    placeholder = { Text("Escribe los detalles aquí...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        viewModel.reportarIncidencia(
                            idViaje = idViaje,
                            tipoIncidencia = tipoSeleccionado,
                            descripcion = descripcion,
                            onExito = onBack
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RojoAccionBelmonte)
                ) {
                    Text("ENVIAR REPORTE")
                }
            }

            if (incidenciasViaje.isNotEmpty()) {
                BelmonteCard {
                    Text(
                        text = "Incidencias Registradas en este Viaje",
                        style = MaterialTheme.typography.titleMedium,
                        color = AzulBelmonteClaro
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    incidenciasViaje.forEachIndexed { idx, inc ->
                        if (idx > 0) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        }
                        Text("${inc.tipoIncidencia} (${inc.fechaRegistro})", fontWeight = FontWeight.Bold, color = RojoAccionBelmonte)
                        Text(inc.descripcion)
                    }
                }
            }
        }
    }
}
