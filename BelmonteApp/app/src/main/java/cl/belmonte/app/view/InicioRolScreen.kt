package cl.belmonte.app.view

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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.belmonte.app.navigation.Rutas
import cl.belmonte.app.ui.theme.AzulBelmonte
import cl.belmonte.app.ui.theme.AzulBelmonteClaro
import cl.belmonte.app.ui.theme.AzulMarinoBelmonte
import cl.belmonte.app.ui.theme.ColorEstadoEnCurso
import cl.belmonte.app.ui.theme.ColorEstadoPendiente
import cl.belmonte.app.ui.theme.RojoAccionBelmonte
import cl.belmonte.app.view.components.BelmonteCard
import cl.belmonte.app.viewmodel.BelmonteViewModel

/**
 * Pantalla de Menú Principal / Inicio dinámico según el rol autenticado.
 *
 * ¿QUÉ HACE ESTA PANTALLA?
 * Muestra la tarjeta de bienvenida del usuario conectado (nombre, RUT, rol y empresa cliente si
 * corresponde) y presenta las opciones de navegación exclusivas de su rol:
 * - ROLE_SOLICITANTE: Nueva Solicitud (CU-02), Mis Viajes y Seguimiento (CU-09), Escanear QR.
 * - ROLE_ADMIN: Solicitudes Pendientes y Asignación (CU-03), Reasignaciones e Incidencias (CU-10).
 * - ROLE_CONDUCTOR: Mis Viajes del Día (CU-04), Iniciar/Finalizar (CU-05/07), Maps/Waze (CU-06), Incidencias (CU-08).
 * - ROLE_ADMIN_EMPRESA: Mis Cupos de Empresa, Repartir Cupos a Trabajadores, Cobros del Mes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioRolScreen(
    viewModel: BelmonteViewModel,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val usuario by viewModel.usuarioActual.collectAsState()
    val empresas by viewModel.empresas.collectAsState()
    val cuposTrabajador by viewModel.cuposTrabajador.collectAsState()
    val solicitudesPendientes by viewModel.solicitudesPendientes.collectAsState()
    val todosLosViajes by viewModel.todosLosViajes.collectAsState()
    val todasLasIncidencias by viewModel.todasLasIncidencias.collectAsState()

    val usuarioActivo = usuario
    if (usuarioActivo == null) {
        LaunchedEffect(Unit) { onLogout() }
        return
    }

    LaunchedEffect(usuarioActivo.idUsuario) {
        viewModel.recargarCupos()
    }

    val nombreEmpresa = empresas.find { it.idEmpresa == usuarioActivo.idEmpresa }?.razonSocial
        ?: "Personal Interno — Belmonte SpA"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hola, ${usuarioActivo.nombres}",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = nombreRolLegible(usuarioActivo.rol),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AzulBelmonteClaro
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.cerrarSesion()
                            onLogout()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = Color.White
                        )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de identificación del perfil autenticado
            BelmonteCard(colorBorde = AzulBelmonte) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = usuarioActivo.nombreCompleto,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "RUT: ${usuarioActivo.rut} · ${usuarioActivo.email}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Organización: $nombreEmpresa",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AzulBelmonteClaro,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Opciones según el rol del usuario
            when (usuarioActivo.rol) {
                "ROLE_SOLICITANTE" -> {
                    // Resumen rápido de cupos disponibles del trabajador
                    BelmonteCard {
                        Text(
                            text = "Mis Cupos Disponibles (Octubre 2026)",
                            style = MaterialTheme.typography.titleMedium,
                            color = AzulBelmonteClaro
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        cuposTrabajador.forEach { cupo ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${cupo.tipoVehiculo.nombre} (tope ${cupo.tipoVehiculo.kmIncluidos} km)",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${cupo.disponibles} disp. (de ${cupo.asignados})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (cupo.disponibles > 0) ColorEstadoEnCurso else RojoAccionBelmonte
                                )
                            }
                        }
                    }

                    BotonMenuRol(
                        titulo = "Nueva Solicitud de Traslado (CU-02)",
                        subtitulo = "Elige Auto, Van o Camión, origen/destino, vuelo o carga",
                        icono = Icons.Default.AddCircleOutline,
                        colorBoton = AzulBelmonte,
                        onClick = { onNavigate(Rutas.NUEVA_SOLICITUD) }
                    )

                    BotonMenuRol(
                        titulo = "Mis Viajes y Seguimiento (CU-09)",
                        subtitulo = "Ver estado de solicitudes, conductor asignado o cancelar viaje",
                        icono = Icons.AutoMirrored.Filled.ListAlt,
                        colorBoton = AzulMarinoBelmonte,
                        onClick = { onNavigate(Rutas.MIS_VIAJES) }
                    )

                    // Busca un viaje asignado/en curso del solicitante para escanear su QR rápido
                    val viajeConAuto = todosLosViajes.firstOrNull {
                        it.idSolicitante == usuarioActivo.idUsuario && it.idVehiculo != null
                    } ?: todosLosViajes.firstOrNull { it.idVehiculo != null }

                    BotonMenuRol(
                        titulo = "Escanear QR del Vehículo",
                        subtitulo = "Confirma con la cámara si la patente corresponde a tu viaje",
                        icono = Icons.Default.QrCodeScanner,
                        colorBoton = RojoAccionBelmonte,
                        onClick = {
                            val idViajeTarget = viajeConAuto?.idViaje ?: 103L
                            onNavigate("escanear_qr/$idViajeTarget")
                        }
                    )
                }

                "ROLE_ADMIN" -> {
                    // Resumen operativo para la recepcionista de Belmonte
                    BelmonteCard {
                        Text(
                            text = "Resumen Operativo Central Belmonte",
                            style = MaterialTheme.typography.titleMedium,
                            color = AzulBelmonteClaro
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Solicitudes PENDIENTES por asignar: ${solicitudesPendientes.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ColorEstadoPendiente,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• Total de viajes registrados en sistema: ${todosLosViajes.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "• Incidencias reportadas en ruta: ${todasLasIncidencias.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RojoAccionBelmonte,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    BotonMenuRol(
                        titulo = "Gestionar Solicitudes, Asignar y Reasignar (CU-03 / CU-10)",
                        subtitulo = "Asignar conductor + vehículo del tipo pedido o reasignar ante incidencias",
                        icono = Icons.Default.PendingActions,
                        colorBoton = AzulBelmonte,
                        onClick = { onNavigate(Rutas.SOLICITUDES_PENDIENTES) }
                    )

                    // Acceso rápido al primer viaje pendiente para demostración inmediata
                    solicitudesPendientes.firstOrNull()?.let { pendiente ->
                        BotonMenuRol(
                            titulo = "Asignar Ahora: Viaje #${pendiente.idViaje} (${pendiente.nombrePasajero})",
                            subtitulo = "Pedido: ${pendiente.nombreTipoVehiculo} · ${pendiente.fechaServicio} ${pendiente.horaServicio}",
                            icono = Icons.Default.AssignmentInd,
                            colorBoton = RojoAccionBelmonte,
                            onClick = { onNavigate("asignar_viaje/${pendiente.idViaje}") }
                        )
                    }
                }

                "ROLE_CONDUCTOR" -> {
                    val misViajes = todosLosViajes.filter { it.idConductor == usuarioActivo.idUsuario }
                    val activos = misViajes.count { it.idEstado == 2 || it.idEstado == 3 }

                    BelmonteCard {
                        Text(
                            text = "Hoja de Ruta en Terreno",
                            style = MaterialTheme.typography.titleMedium,
                            color = AzulBelmonteClaro
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Traslados activos (Asignados / En Curso): $activos",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = ColorEstadoEnCurso
                        )
                        Text(
                            text = "• Total de servicios en tu historial: ${misViajes.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    BotonMenuRol(
                        titulo = "Mis Viajes del Día (CU-04)",
                        subtitulo = "Iniciar/finalizar traslado (CU-05/07), abrir Maps/Waze (CU-06) y reportar incidencias (CU-08)",
                        icono = Icons.Default.DirectionsCar,
                        colorBoton = AzulBelmonte,
                        onClick = { onNavigate(Rutas.VIAJES_CONDUCTOR) }
                    )
                }

                "ROLE_ADMIN_EMPRESA" -> {
                    BotonMenuRol(
                        titulo = "Mis Cupos de Empresa y Viajes Corporativos",
                        subtitulo = "Ver cupos contratados, repartidos y usados por tipo de vehículo",
                        icono = Icons.Default.Business,
                        colorBoton = AzulBelmonte,
                        onClick = { onNavigate(Rutas.MIS_CUPOS_EMPRESA) }
                    )

                    BotonMenuRol(
                        titulo = "Repartir Cupos a Trabajadores (RN-12)",
                        subtitulo = "Asignar viajes de Auto, Van o Camión a empleados de tu empresa",
                        icono = Icons.Default.GroupAdd,
                        colorBoton = AzulMarinoBelmonte,
                        onClick = { onNavigate(Rutas.REPARTIR_CUPOS) }
                    )

                    BotonMenuRol(
                        titulo = "Cobros del Mes (Paquete y Extras RN-14)",
                        subtitulo = "Ver facturación mensual y calcular cargos por kilómetros extra",
                        icono = Icons.AutoMirrored.Filled.ReceiptLong,
                        colorBoton = RojoAccionBelmonte,
                        onClick = { onNavigate(Rutas.COBROS_EMPRESA) }
                    )
                }
            }

            // Botón inferior para cerrar sesión y probar otro rol rápidamente
            Button(
                onClick = {
                    viewModel.cerrarSesion()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CAMBIAR DE USUARIO / CERRAR SESIÓN")
            }
        }
    }
}

@Composable
private fun BotonMenuRol(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    colorBoton: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = colorBoton,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.height(34.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

private fun nombreRolLegible(rol: String): String = when (rol) {
    "ROLE_ADMIN" -> "Administrador / Recepcionista Belmonte"
    "ROLE_ADMIN_EMPRESA" -> "Administrador de Empresa Cliente"
    "ROLE_SOLICITANTE" -> "Solicitante / Pasajero Corporativo"
    "ROLE_CONDUCTOR" -> "Conductor en Terreno Belmonte"
    else -> rol
}
