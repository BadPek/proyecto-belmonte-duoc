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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
 * Pantallas del rol ADMINISTRADOR DE EMPRESA CLIENTE (ROLE_ADMIN_EMPRESA — Ej: Tomás Herrera de Minera Andes Sur).
 *
 * Incluye:
 * 1. MisCuposEmpresaScreen: muestra cupos contratados vs repartidos vs usados por tipo y viajes de la empresa.
 * 2. RepartirCuposScreen: reparte cupos del paquete mensual a los trabajadores validando RN-12.
 * 3. CobrosEmpresaScreen: muestra cobros del mes (PAQUETE y EXTRAS) y permite calcular el cierre mensual (RN-14).
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
                title = { Text("Cupos y Viajes de Empresa") },
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
            BelmonteCard(colorBorde = AzulBelmonte) {
                Text(
                    text = empresa?.razonSocial ?: "Empresa Cliente #$idEmpresa",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    text = "RUT: ${empresa?.rutEmpresa ?: ""} · Paquete Mensual Octubre 2026",
                    color = AzulBelmonteClaro
                )
            }

            // Resumen de cupos por tipo de vehículo (Consulta B del script SQL)
            Text(
                text = "Cupos del Paquete Mensual por Tipo (Consulta B SQL)",
                style = MaterialTheme.typography.titleMedium
            )

            cuposEmpresa.forEach { resumen ->
                BelmonteCard {
                    Text(
                        text = "${resumen.tipoVehiculo.nombre} (${resumen.tipoVehiculo.codigo})",
                        style = MaterialTheme.typography.titleMedium,
                        color = AzulBelmonteClaro
                    )
                    Text(
                        text = "Tope por viaje: ${resumen.tipoVehiculo.kmIncluidos} km · Km extra: $${resumen.tipoVehiculo.valorKmExtra} CLP",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Contratados", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${resumen.contratados}", style = MaterialTheme.typography.titleLarge)
                        }
                        Column {
                            Text("Repartidos", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${resumen.repartidos}", style = MaterialTheme.typography.titleLarge, color = AzulBelmonteClaro)
                        }
                        Column {
                            Text("Por repartir", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${resumen.disponiblesPorRepartir}", style = MaterialTheme.typography.titleLarge, color = ColorEstadoEnCurso)
                        }
                        Column {
                            Text("Usados", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${resumen.usadosPorEmpresa}", style = MaterialTheme.typography.titleLarge, color = ColorEstadoPendiente)
                        }
                    }
                }
            }

            Button(
                onClick = onIrARepartir,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulBelmonte)
            ) {
                Text("IR A REPARTIR CUPOS A TRABAJADORES (RN-12)")
            }

            // Listado de viajes realizados por trabajadores de esta empresa (Consulta A del script SQL)
            Text(
                text = "Viajes de ${empresa?.razonSocial ?: "la Empresa"} (${viajesEmpresa.size})",
                style = MaterialTheme.typography.titleMedium
            )

            viajesEmpresa.forEach { viaje ->
                BelmonteCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Viaje #${viaje.idViaje} · ${viaje.nombreTipoVehiculo}",
                            fontWeight = FontWeight.Bold,
                            color = AzulBelmonteClaro
                        )
                        BadgeEstadoViaje(codigoEstado = viaje.codigoEstado)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Pasajero: ${viaje.nombrePasajero}")
                    Text("Fecha: ${viaje.fechaServicio} ${viaje.horaServicio}")
                    Text("Ruta: ${viaje.direccionOrigen} → ${viaje.direccionDestino}")
                    Text(
                        text = "Distancia: ${viaje.distanciaKm} km | Km Extra: ${viaje.kmExtra} km | Cargo Extra: $${viaje.cargoExtra} CLP",
                        color = if (viaje.cargoExtra > 0) ColorEstadoPendiente else ColorEstadoEnCurso,
                        fontWeight = FontWeight.SemiBold
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
            // Por defecto selecciona al solicitante de la empresa (ej: Daniela en Empresa 1)
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

            // 1. Seleccionar Trabajador de la empresa
            BelmonteCard(colorBorde = AzulBelmonte) {
                Text(
                    text = "1. Selecciona el Trabajador de tu Empresa",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulBelmonteClaro
                )
                Spacer(modifier = Modifier.height(8.dp))

                trabajadoresEmpresa.forEach { emp ->
                    val seleccionado = emp.idUsuario == idTrabajadorSeleccionado
                    Surface(
                        onClick = { idTrabajadorSeleccionado = emp.idUsuario },
                        shape = RoundedCornerShape(10.dp),
                        color = if (seleccionado) AzulBelmonte.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(if (seleccionado) 2.dp else 1.dp, if (seleccionado) AzulBelmonte else Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${emp.nombreCompleto} (${emp.rut})", fontWeight = FontWeight.Bold)
                            Text(
                                text = "${emp.email} · Rol: ${emp.rol}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 2. Seleccionar Tipo de Vehículo del paquete mensual
            BelmonteCard {
                Text(
                    text = "2. Selecciona la Línea del Paquete Mensual",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulBelmonteClaro
                )
                Spacer(modifier = Modifier.height(8.dp))

                cuposEmpresa.forEach { linea ->
                    val seleccionado = linea.idCobroCupo == idCobroCupoSeleccionado
                    Surface(
                        onClick = { idCobroCupoSeleccionado = linea.idCobroCupo },
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
                                Text(linea.tipoVehiculo.nombre, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Contratados: ${linea.contratados} | Repartidos: ${linea.repartidos}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${linea.disponiblesPorRepartir} libres",
                                color = ColorEstadoEnCurso,
                                fontWeight = FontWeight.Bold
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
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulBelmonte)
                ) {
                    Text("GUARDAR ASIGNACIÓN DE CUPOS")
                }
            }
        }
    }
}

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
    val mensajeFeedback by viewModel.mensajeFeedback.collectAsState()
    val esError by viewModel.esErrorFeedback.collectAsState()

    val idEmpresa = usuarioActual?.idEmpresa ?: 1L
    val empresa = empresas.find { it.idEmpresa == idEmpresa }
    val cobrosEmpresa = todosLosCobros.filter { it.idEmpresa == idEmpresa }
    val viajesConExtraFinalizados = todosLosViajes.filter {
        it.idEmpresa == idEmpresa && it.idEstado == 4 && it.cargoExtra > 0
    }
    val sumaExtrasMes = viajesConExtraFinalizados.sumOf { it.cargoExtra }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cobros Mensuales de Empresa") },
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

            // 1. Resumen de Cierre de Mes (Consulta F del script SQL y Regla RN-14)
            BelmonteCard(colorBorde = AzulBelmonte) {
                Text(
                    text = "Cierre Mensual de Kilómetros Extra (RN-14)",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulBelmonteClaro
                )
                Text(
                    text = "Empresa: ${empresa?.razonSocial ?: "Empresa #$idEmpresa"}",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("• Viajes FINALIZADOS con km extra en 2026-10: ${viajesConExtraFinalizados.size}")
                viajesConExtraFinalizados.forEach { v ->
                    Text(
                        text = "  - Viaje #${v.idViaje} (${v.nombreTipoVehiculo}): ${v.distanciaKm} km → ${v.kmExtra} km extra = $${v.cargoExtra} CLP",
                        color = ColorEstadoPendiente
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Total acumulado por Km Extra: $$sumaExtrasMes CLP",
                    style = MaterialTheme.typography.titleMedium,
                    color = ColorEstadoEnCurso
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { viewModel.emitirCobroExtrasMes(idEmpresa) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RojoAccionBelmonte)
                ) {
                    Text("EMITIR / ACTUALIZAR COBRO EXTRAS DE OCTUBRE (RN-14)")
                }
            }

            // 2. Listado de cobros emitidos a la empresa (PAQUETE y EXTRAS)
            Text(
                text = "Facturación Registrada (${cobrosEmpresa.size})",
                style = MaterialTheme.typography.titleLarge
            )

            cobrosEmpresa.forEach { cobro ->
                BelmonteCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cobro #${cobro.idCobro} · Tipo: ${cobro.tipoCobro}",
                            style = MaterialTheme.typography.titleMedium,
                            color = AzulBelmonteClaro
                        )
                        Text(
                            text = cobro.estadoPago,
                            color = if (cobro.estadoPago == "PAGADO") ColorEstadoEnCurso else ColorEstadoPendiente,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Periodo: ${cobro.periodo}", fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "Monto Total: $${cobro.monto} CLP",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = "Fecha emisión: ${cobro.fechaEmision} · Fecha pago: ${cobro.fechaPago ?: "Pendiente"}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
