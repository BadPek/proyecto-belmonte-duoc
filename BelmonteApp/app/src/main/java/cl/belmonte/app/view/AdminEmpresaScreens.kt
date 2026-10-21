package cl.belmonte.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AirportShuttle
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.belmonte.app.model.CatalogoPlanesBelmonte
import cl.belmonte.app.model.PlanSuscripcionCatalogo
import cl.belmonte.app.ui.theme.AzulBelmonte
import cl.belmonte.app.ui.theme.AzulBelmonteClaro
import cl.belmonte.app.ui.theme.AzulMarinoBelmonte
import cl.belmonte.app.ui.theme.ColorEstadoCancelado
import cl.belmonte.app.ui.theme.ColorEstadoEnCurso
import cl.belmonte.app.ui.theme.ColorEstadoPendiente
import cl.belmonte.app.ui.theme.FondoOscuro
import cl.belmonte.app.ui.theme.RojoAccionBelmonte
import cl.belmonte.app.ui.theme.SuperficieOscura
import cl.belmonte.app.ui.theme.TextoBlanco
import cl.belmonte.app.ui.theme.TextoSecundarioOscuro
import cl.belmonte.app.view.components.BadgeEstadoViaje
import cl.belmonte.app.view.components.BelmonteCard
import cl.belmonte.app.view.components.ChipVehiculoBelmonte
import cl.belmonte.app.view.components.HeroBannerBelmonte
import cl.belmonte.app.view.components.LineaRutaTimeline
import cl.belmonte.app.viewmodel.BelmonteViewModel

/**
 * Pantallas del rol ADMINISTRADOR DE EMPRESA CLIENTE (ROLE_ADMIN_EMPRESA — Ej: Tomás Herrera de Minera Andes Sur).
 *
 * Incluye:
 * 1. MisCuposEmpresaScreen: barras de progreso de cupos contratados vs repartidos vs usados y viajes de la empresa.
 * 2. RepartirCuposScreen: reparte cupos del paquete mensual a los trabajadores validando RN-12.
 * 3. CobrosEmpresaScreen: Catálogo de Planes de Suscripción Mensual (Básico, Grupos, Carga y Personalizado)
 *    + Cierre mensual de kilómetros extra (RN-14) + Facturación registrada.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisCuposEmpresaScreen(
    viewModel: BelmonteViewModel,
    onBack: () -> Unit,
    onIrARepartir: () -> Unit
) {
    val usuario by viewModel.usuarioActual.collectAsState()
    val empresas by viewModel.empresas.collectAsState()
    val cuposEmpresa by viewModel.cuposEmpresa.collectAsState()
    val todosLosViajes by viewModel.todosLosViajes.collectAsState()

    val idEmpresa = usuario?.idEmpresa ?: 1L
    val empresa = empresas.find { it.idEmpresa == idEmpresa }
    val viajesEmpresa = todosLosViajes.filter { it.idEmpresa == idEmpresa }

    LaunchedEffect(idEmpresa) {
        viewModel.recargarCupos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cupos Contratados y Flota") },
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
        },
        containerColor = FondoOscuro
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            HeroBannerBelmonte(
                etiquetaSuperior = "PAQUETE ACTIVO • OCTUBRE 2026",
                titulo = empresa?.razonSocial ?: "Empresa Cliente #$idEmpresa",
                subtitulo = "RUT ${empresa?.rutEmpresa ?: ""} • ${empresa?.giro ?: ""}",
                icono = Icons.Default.Business
            )

            Text(
                text = "Cupos del Paquete Mensual por Tipo (Consulta B SQL)",
                style = MaterialTheme.typography.titleMedium,
                color = TextoBlanco,
                fontWeight = FontWeight.ExtraBold
            )

            cuposEmpresa.forEach { resumen ->
                val progresoReparto = if (resumen.contratados > 0) {
                    (resumen.repartidos.toFloat() / resumen.contratados.toFloat()).coerceIn(0f, 1f)
                } else 0f

                BelmonteCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChipVehiculoBelmonte(
                            idTipoVehiculo = resumen.tipoVehiculo.idTipoVehiculo,
                            textoPersonalizado = "${resumen.tipoVehiculo.nombre} (${resumen.tipoVehiculo.codigo})"
                        )
                        Text(
                            text = "${resumen.disponiblesPorRepartir} libres",
                            color = ColorEstadoEnCurso,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tope incluido: ${resumen.tipoVehiculo.kmIncluidos} km/viaje • Km extra: $${resumen.tipoVehiculo.valorKmExtra} CLP",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundarioOscuro
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progresoReparto },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = AzulBelmonte,
                        trackColor = FondoOscuro
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Contratados", fontSize = 11.sp, color = TextoSecundarioOscuro)
                            Text("${resumen.contratados}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        }
                        Column {
                            Text("Repartidos", fontSize = 11.sp, color = TextoSecundarioOscuro)
                            Text("${resumen.repartidos}", style = MaterialTheme.typography.titleMedium, color = AzulBelmonteClaro, fontWeight = FontWeight.ExtraBold)
                        }
                        Column {
                            Text("Por repartir", fontSize = 11.sp, color = TextoSecundarioOscuro)
                            Text("${resumen.disponiblesPorRepartir}", style = MaterialTheme.typography.titleMedium, color = ColorEstadoEnCurso, fontWeight = FontWeight.ExtraBold)
                        }
                        Column {
                            Text("Usados", fontSize = 11.sp, color = TextoSecundarioOscuro)
                            Text("${resumen.usadosPorEmpresa}", style = MaterialTheme.typography.titleMedium, color = ColorEstadoPendiente, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            Button(
                onClick = onIrARepartir,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulBelmonte)
            ) {
                Text("REPARTIR CUPOS A TRABAJADORES (RN-12)", fontWeight = FontWeight.ExtraBold)
            }

            Text(
                text = "Viajes de ${empresa?.razonSocial ?: "la Empresa"} (${viajesEmpresa.size})",
                style = MaterialTheme.typography.titleMedium,
                color = TextoBlanco,
                fontWeight = FontWeight.ExtraBold
            )

            viajesEmpresa.forEach { viaje ->
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
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            ChipVehiculoBelmonte(
                                idTipoVehiculo = viaje.idTipoVehiculo,
                                textoPersonalizado = viaje.nombreTipoVehiculo
                            )
                        }
                        BadgeEstadoViaje(codigoEstado = viaje.codigoEstado)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Pasajero: ${viaje.nombrePasajero} • ${viaje.fechaServicio} ${viaje.horaServicio}", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LineaRutaTimeline(
                        origen = viaje.direccionOrigen,
                        destino = viaje.direccionDestino,
                        distanciaKm = viaje.distanciaKm,
                        kmExtra = viaje.kmExtra,
                        cargoExtra = viaje.cargoExtra
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepartirCuposScreen(
    viewModel: BelmonteViewModel,
    onBack: () -> Unit
) {
    val usuarioActual by viewModel.usuarioActual.collectAsState()
    val todosUsuarios by viewModel.todosLosUsuarios.collectAsState()
    val cuposEmpresa by viewModel.cuposEmpresa.collectAsState()
    val mensajeFeedback by viewModel.mensajeFeedback.collectAsState()
    val esError by viewModel.esErrorFeedback.collectAsState()

    val idEmpresa = usuarioActual?.idEmpresa ?: 1L
    val trabajadoresEmpresa = todosUsuarios.filter { it.idEmpresa == idEmpresa }

    var idTrabajadorSeleccionado by remember { mutableLongStateOf(0L) }
    var idCobroCupoSeleccionado by remember { mutableLongStateOf(0L) }
    var cantidadViajesTexto by remember { mutableStateOf("10") }

    LaunchedEffect(trabajadoresEmpresa, cuposEmpresa) {
        if (idTrabajadorSeleccionado == 0L && trabajadoresEmpresa.isNotEmpty()) {
            idTrabajadorSeleccionado = trabajadoresEmpresa.firstOrNull { it.rol == "ROLE_SOLICITANTE" }?.idUsuario
                ?: trabajadoresEmpresa.first().idUsuario
        }
        if (idCobroCupoSeleccionado == 0L && cuposEmpresa.isNotEmpty()) {
            idCobroCupoSeleccionado = cuposEmpresa.first().idCobroCupo
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Repartir Cupos a Trabajadores (RN-12)") },
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
        },
        containerColor = FondoOscuro
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

            BelmonteCard(colorBorde = AzulBelmonte) {
                Text(
                    text = "1. Selecciona el Trabajador de tu Empresa",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulBelmonteClaro,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                trabajadoresEmpresa.forEach { emp ->
                    val seleccionado = emp.idUsuario == idTrabajadorSeleccionado
                    Surface(
                        onClick = { idTrabajadorSeleccionado = emp.idUsuario },
                        shape = RoundedCornerShape(12.dp),
                        color = if (seleccionado) AzulBelmonte.copy(alpha = 0.25f) else FondoOscuro,
                        border = BorderStroke(if (seleccionado) 2.dp else 1.dp, if (seleccionado) AzulBelmonte else Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${emp.nombreCompleto} (${emp.rut})", fontWeight = FontWeight.Bold)
                            Text(
                                text = "${emp.email} · Rol: ${emp.rol}",
                                fontSize = 12.sp,
                                color = TextoSecundarioOscuro
                            )
                        }
                    }
                }
            }

            BelmonteCard {
                Text(
                    text = "2. Selecciona la Línea del Paquete Mensual",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulBelmonteClaro,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                cuposEmpresa.forEach { linea ->
                    val seleccionado = linea.idCobroCupo == idCobroCupoSeleccionado
                    Surface(
                        onClick = { idCobroCupoSeleccionado = linea.idCobroCupo },
                        shape = RoundedCornerShape(12.dp),
                        color = if (seleccionado) AzulBelmonte.copy(alpha = 0.25f) else FondoOscuro,
                        border = BorderStroke(if (seleccionado) 2.dp else 1.dp, if (seleccionado) AzulBelmonte else Color.White.copy(alpha = 0.1f)),
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
                                Text(linea.tipoVehiculo.nombre, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Contratados: ${linea.contratados} | Repartidos: ${linea.repartidos}",
                                    fontSize = 12.sp,
                                    color = TextoSecundarioOscuro
                                )
                            }
                            Text(
                                text = "${linea.disponiblesPorRepartir} libres",
                                color = ColorEstadoEnCurso,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = cantidadViajesTexto,
                    onValueChange = { cantidadViajesTexto = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Cantidad de viajes a asignar al trabajador en Octubre") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(3, 5, 10, 12, 50).forEach { valorRapido ->
                        OutlinedButton(
                            onClick = { cantidadViajesTexto = valorRapido.toString() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("$valorRapido")
                        }
                    }
                }
                Text(
                    text = "Tip: Prueba asignar '50' para verificar cómo la regla RN-12 bloquea superar el tope del paquete.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundarioOscuro
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val cant = cantidadViajesTexto.toIntOrNull() ?: 0
                        viewModel.repartirCuposATrabajador(
                            idCobroCupo = idCobroCupoSeleccionado,
                            idTrabajador = idTrabajadorSeleccionado,
                            nuevaCantidad = cant
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulBelmonte)
                ) {
                    Text("GUARDAR ASIGNACIÓN DE CUPOS", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

/**
 * Pantalla de Planes de Suscripción Mensual y Cobros (Básico, Grupos, Carga y Personalizado + Extras RN-14).
 *
 * ¿QUÉ HACE ESTA PANTALLA?
 * 1. Muestra el Catálogo de Planes de Suscripción Mensual tal como se definió en el modelo:
 *    - Plan Básico: 10 viajes en Auto + 1 en Van ($310.000 CLP/mes)
 *    - Plan Grupos: 10 viajes en Auto + 4 en Van ($440.000 CLP/mes)
 *    - Plan Carga & Flota: 10 viajes en Auto + 6 en Van + 5 en Camión ($850.000 CLP/mes)
 *    - Plan Personalizado: lo que la empresa elija con selectores +/- (se calcula automáticamente)
 * 2. Cuando la empresa contrata un plan, se copia al cobro PAQUETE del mes en Room y actualiza
 *    sus cupos disponibles sin perder las asignaciones de los trabajadores.
 * 3. Permite además emitir el cobro EXTRAS de fin de mes por kilómetros extra (RN-14).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CobrosEmpresaScreen(
    viewModel: BelmonteViewModel,
    onBack: () -> Unit
) {
    val usuarioActual by viewModel.usuarioActual.collectAsState()
    val todosLosCobros by viewModel.todosLosCobros.collectAsState()
    val todosLosViajes by viewModel.todosLosViajes.collectAsState()
    val empresas by viewModel.empresas.collectAsState()
    val cuposEmpresa by viewModel.cuposEmpresa.collectAsState()
    val mensajeFeedback by viewModel.mensajeFeedback.collectAsState()
    val esError by viewModel.esErrorFeedback.collectAsState()

    var idEmpresaSeleccionada by remember { mutableLongStateOf(usuarioActual?.idEmpresa ?: 1L) }
    val empresa = empresas.find { it.idEmpresa == idEmpresaSeleccionada }
    val cobrosEmpresa = todosLosCobros.filter { it.idEmpresa == idEmpresaSeleccionada }
    val viajesConExtraFinalizados = todosLosViajes.filter {
        it.idEmpresa == idEmpresaSeleccionada && it.idEstado == 4 && it.cargoExtra > 0
    }
    val sumaExtrasMes = viajesConExtraFinalizados.sumOf { it.cargoExtra }

    // Estados para el Plan Personalizado (lo que la empresa elija -> se calcula en vivo)
    var autosPersonalizado by remember { mutableIntStateOf(10) }
    var vansPersonalizado by remember { mutableIntStateOf(2) }
    var camionesPersonalizado by remember { mutableIntStateOf(1) }
    val precioPersonalizado = CatalogoPlanesBelmonte.calcularPrecioPersonalizado(
        autos = autosPersonalizado,
        vans = vansPersonalizado,
        camiones = camionesPersonalizado
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Planes de Suscripción y Cobros", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                        Text(
                            text = "Catálogo de Paquetes Mensuales y Extras (RN-14)",
                            fontSize = 11.sp,
                            color = AzulBelmonteClaro
                        )
                    }
                },
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
        },
        containerColor = FondoOscuro
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Selector de empresa si entra como Admin Belmonte
            if (empresas.size > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    empresas.forEach { emp ->
                        val sel = emp.idEmpresa == idEmpresaSeleccionada
                        FilterChip(
                            selected = sel,
                            onClick = { idEmpresaSeleccionada = emp.idEmpresa },
                            label = {
                                Text(
                                    text = emp.razonSocial,
                                    fontWeight = if (sel) FontWeight.ExtraBold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AzulBelmonte,
                                selectedLabelColor = Color.White,
                                containerColor = SuperficieOscura,
                                labelColor = TextoSecundarioOscuro
                            )
                        )
                    }
                }
            }

            mensajeFeedback?.let { msg ->
                BelmonteCard(colorBorde = if (esError) ColorEstadoCancelado else ColorEstadoEnCurso) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (esError) ColorEstadoCancelado else ColorEstadoEnCurso
                        )
                        Text(
                            text = msg,
                            color = if (esError) ColorEstadoCancelado else ColorEstadoEnCurso,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // 1. TABLA RESUMEN DEL CATÁLOGO DE PLANES MENSUALES
            HeroBannerBelmonte(
                etiquetaSuperior = "MODELO DE SUSCRIPCIÓN B2B",
                titulo = "Planes Mensuales de Traslado",
                subtitulo = "Cuando ${empresa?.razonSocial ?: "la empresa"} contrata un plan, sus cupos se copian al paquete del mes y las asignaciones a trabajadores se mantienen.",
                icono = Icons.Default.Star
            )

            BelmonteCard(colorBorde = AzulBelmonte) {
                Text(
                    text = "Tabla Comparativa de Planes Mensuales",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextoBlanco,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Encabezado de tabla igual al documento del estudiante
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(FondoOscuro)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Plan", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = AzulBelmonteClaro, modifier = Modifier.weight(0.9f))
                    Text("Incluye al mes", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = AzulBelmonteClaro, modifier = Modifier.weight(1.6f))
                    Text("Precio", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = AzulBelmonteClaro, modifier = Modifier.weight(0.9f))
                }

                FilaTablaPlan("Básico", "10 viajes Auto + 1 Van", "$310.000")
                FilaTablaPlan("Grupos", "10 viajes Auto + 4 Vans", "$440.000")
                FilaTablaPlan("Carga", "10 Auto + 6 Vans + 5 Camiones", "$850.000")
                FilaTablaPlan("Personalizado", "Lo que la empresa elija", "Se calcula")
            }

            // 2. TARJETAS INTERACTIVAS DE LOS 3 PLANES PREDEFINIDOS (BÁSICO, GRUPOS, CARGA)
            Text(
                text = "1. Elige un Plan Predefinido de Suscripción",
                style = MaterialTheme.typography.titleMedium,
                color = TextoBlanco,
                fontWeight = FontWeight.ExtraBold
            )

            CatalogoPlanesBelmonte.planesFijos.forEach { plan ->
                TarjetaPlanFijoSuscripcion(
                    plan = plan,
                    onContratar = {
                        viewModel.contratarPlanSuscripcion(
                            idEmpresa = idEmpresaSeleccionada,
                            nombrePlan = plan.nombre,
                            cuposAuto = plan.cuposAuto,
                            cuposVan = plan.cuposVan,
                            cuposCamion = plan.cuposCamion,
                            precioMensualClp = plan.precioMensualClp
                        )
                    }
                )
            }

            // 3. PLAN PERSONALIZADO (CALCULADORA INTERACTIVA A MEDIDA)
            Text(
                text = "2. O Arma tu Plan Personalizado a Medida",
                style = MaterialTheme.typography.titleMedium,
                color = TextoBlanco,
                fontWeight = FontWeight.ExtraBold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SuperficieOscura),
                border = BorderStroke(1.5.dp, Color(0xFFA855F7).copy(alpha = 0.65f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFA855F7).copy(alpha = 0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = Color(0xFFC084FC)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Plan Personalizado",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextoBlanco,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Elige cuántos Autos, Vans y Camiones necesitas al mes",
                                    fontSize = 11.sp,
                                    color = TextoSecundarioOscuro
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFFA855F7).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "A MEDIDA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFC084FC),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    SelectorCantidadVehiculoPlan(
                        nombre = "Viajes en Auto Ejecutivo",
                        subtitulo = "$${CatalogoPlanesBelmonte.PRECIO_CUPO_AUTO_CLP} CLP por cupo (tope 40 km)",
                        icono = Icons.Default.DirectionsCar,
                        cantidad = autosPersonalizado,
                        onCambiar = { autosPersonalizado = it.coerceIn(0, 100) }
                    )

                    SelectorCantidadVehiculoPlan(
                        nombre = "Viajes en Van Pasajeros",
                        subtitulo = "$${CatalogoPlanesBelmonte.PRECIO_CUPO_VAN_CLP} CLP por cupo (tope 40 km)",
                        icono = Icons.Default.AirportShuttle,
                        cantidad = vansPersonalizado,
                        onCambiar = { vansPersonalizado = it.coerceIn(0, 100) }
                    )

                    SelectorCantidadVehiculoPlan(
                        nombre = "Viajes en Camión de Carga",
                        subtitulo = "$${CatalogoPlanesBelmonte.PRECIO_CUPO_CAMION_CLP} CLP por cupo (tope 60 km)",
                        icono = Icons.Default.LocalShipping,
                        cantidad = camionesPersonalizado,
                        onCambiar = { camionesPersonalizado = it.coerceIn(0, 100) }
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total calculado del Plan Personalizado:",
                                fontSize = 12.sp,
                                color = TextoSecundarioOscuro
                            )
                            Text(
                                text = "$autosPersonalizado Autos • $vansPersonalizado Vans • $camionesPersonalizado Camiones",
                                fontSize = 12.sp,
                                color = AzulBelmonteClaro,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "$$precioPersonalizado CLP",
                            style = MaterialTheme.typography.titleLarge,
                            color = ColorEstadoEnCurso,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.contratarPlanSuscripcion(
                                idEmpresa = idEmpresaSeleccionada,
                                nombrePlan = "Plan Personalizado",
                                cuposAuto = autosPersonalizado,
                                cuposVan = vansPersonalizado,
                                cuposCamion = camionesPersonalizado,
                                precioMensualClp = precioPersonalizado
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA))
                    ) {
                        Text(
                            text = "CONTRATAR PLAN PERSONALIZADO ($$precioPersonalizado)",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            // 4. CIERRE MENSUAL DE KILÓMETROS EXTRA (RN-14)
            BelmonteCard(colorBorde = RojoAccionBelmonte) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = RojoAccionBelmonte
                    )
                    Text(
                        text = "Liquidación de Kilómetros Extra (RN-14)",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextoBlanco,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Viajes FINALIZADOS con km extra en 2026-10: ${viajesConExtraFinalizados.size}",
                    fontSize = 13.sp,
                    color = TextoSecundarioOscuro
                )
                viajesConExtraFinalizados.forEach { v ->
                    Text(
                        text = "• Viaje #${v.idViaje} (${v.nombreTipoVehiculo}): ${v.distanciaKm} km → +${v.kmExtra} km extra = $${v.cargoExtra} CLP",
                        color = ColorEstadoPendiente,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Total acumulado por Km Extra: $$sumaExtrasMes CLP",
                    style = MaterialTheme.typography.titleMedium,
                    color = ColorEstadoEnCurso,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { viewModel.emitirCobroExtrasMes(idEmpresaSeleccionada) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RojoAccionBelmonte)
                ) {
                    Text(
                        text = "EMITIR / ACTUALIZAR COBRO EXTRAS DE OCTUBRE",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // 5. FACTURACIÓN REGISTRADA EN ROOM (PAQUETE Y EXTRAS)
            Text(
                text = "Facturación Registrada en Room (${cobrosEmpresa.size})",
                style = MaterialTheme.typography.titleMedium,
                color = TextoBlanco,
                fontWeight = FontWeight.ExtraBold
            )

            cobrosEmpresa.forEach { cobro ->
                BelmonteCard(
                    colorBorde = if (cobro.estadoPago == "PAGADO") ColorEstadoEnCurso else ColorEstadoPendiente
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cobro #${cobro.idCobro} • ${cobro.tipoCobro}",
                            style = MaterialTheme.typography.titleMedium,
                            color = AzulBelmonteClaro,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Surface(
                            color = if (cobro.estadoPago == "PAGADO") {
                                ColorEstadoEnCurso.copy(alpha = 0.2f)
                            } else {
                                ColorEstadoPendiente.copy(alpha = 0.2f)
                            },
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = cobro.estadoPago,
                                color = if (cobro.estadoPago == "PAGADO") ColorEstadoEnCurso else ColorEstadoPendiente,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Monto Total: $${cobro.monto} CLP",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Periodo: ${cobro.periodo} • Emisión: ${cobro.fechaEmision} • Pago: ${cobro.fechaPago ?: "Pendiente"}",
                        fontSize = 12.sp,
                        color = TextoSecundarioOscuro
                    )
                    if (cobro.tipoCobro == "PAQUETE" && cuposEmpresa.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            cuposEmpresa.forEach { c ->
                                Surface(
                                    color = AzulBelmonte.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${c.contratados} ${c.tipoVehiculo.codigo}",
                                        fontSize = 11.sp,
                                        color = AzulBelmonteClaro,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaTablaPlan(
    nombre: String,
    incluye: String,
    precio: String
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(nombre, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextoBlanco, modifier = Modifier.weight(0.9f))
            Text(incluye, fontSize = 12.sp, color = TextoSecundarioOscuro, modifier = Modifier.weight(1.6f))
            Text(precio, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = ColorEstadoEnCurso, modifier = Modifier.weight(0.9f))
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
    }
}

@Composable
private fun TarjetaPlanFijoSuscripcion(
    plan: PlanSuscripcionCatalogo,
    onContratar: () -> Unit
) {
    val colorDestacado = when (plan.idPlan) {
        1 -> AzulBelmonte
        2 -> ColorEstadoEnCurso
        else -> ColorEstadoPendiente
    }

    BelmonteCard(colorBorde = colorDestacado) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = plan.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextoBlanco,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (plan.etiquetaDestacada != null) {
                        Surface(
                            color = colorDestacado.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = plan.etiquetaDestacada,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colorDestacado,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = plan.descripcionIncluye,
                    fontSize = 12.sp,
                    color = TextoSecundarioOscuro
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${plan.precioMensualClp}",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextoBlanco,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "CLP / mes",
                    fontSize = 10.sp,
                    color = AzulBelmonteClaro
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Píldoras de cupos incluidos por tipo de vehículo
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (plan.cuposAuto > 0) {
                ChipVehiculoBelmonte(idTipoVehiculo = 1, textoPersonalizado = "${plan.cuposAuto} Autos")
            }
            if (plan.cuposVan > 0) {
                ChipVehiculoBelmonte(idTipoVehiculo = 2, textoPersonalizado = "${plan.cuposVan} Vans")
            }
            if (plan.cuposCamion > 0) {
                ChipVehiculoBelmonte(idTipoVehiculo = 3, textoPersonalizado = "${plan.cuposCamion} Camiones")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onContratar,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorDestacado)
        ) {
            Text(
                text = "CONTRATAR ${plan.nombre.uppercase()} ($${plan.precioMensualClp})",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SelectorCantidadVehiculoPlan(
    nombre: String,
    subtitulo: String,
    icono: ImageVector,
    cantidad: Int,
    onCambiar: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = FondoOscuro.copy(alpha = 0.7f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = AzulBelmonteClaro,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextoBlanco
                    )
                    Text(
                        text = subtitulo,
                        fontSize = 11.sp,
                        color = TextoSecundarioOscuro
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = { onCambiar(cantidad - 1) },
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = SuperficieOscura,
                    border = BorderStroke(1.dp, AzulBelmonteClaro.copy(alpha = 0.5f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Menos",
                            tint = TextoBlanco,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = "$cantidad",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextoBlanco,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.width(28.dp)
                )

                Surface(
                    onClick = { onCambiar(cantidad + 1) },
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = AzulBelmonte
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Más",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
