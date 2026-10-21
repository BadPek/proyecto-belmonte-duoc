package cl.belmonte.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.belmonte.app.database.DatosInicialesBelmonte
import cl.belmonte.app.navigation.Rutas
import cl.belmonte.app.ui.theme.AzulBelmonte
import cl.belmonte.app.ui.theme.AzulBelmonteClaro
import cl.belmonte.app.ui.theme.AzulMarinoBelmonte
import cl.belmonte.app.ui.theme.ColorEstadoAsignado
import cl.belmonte.app.ui.theme.ColorEstadoEnCurso
import cl.belmonte.app.ui.theme.ColorEstadoFinalizado
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
import cl.belmonte.app.view.components.TarjetaMenuAccionModerna
import cl.belmonte.app.viewmodel.BelmonteViewModel

/**
 * Pantalla Principal Dinámica según Rol (Inicio / Dashboard).
 *
 * ¿QUÉ HACE ESTA PANTALLA?
 * Presenta un dashboard móvil comercial moderno con:
 * - Barra superior corporativa y Barra de Navegación Inferior (NavigationBar de Material 3).
 * - Selector rápido de rol en 1 toque para demostrar los 4 perfiles fluidamente en el video.
 * - HeroBannerBelmonte en degradado con el perfil del usuario autenticado y su empresa.
 * - Tarjetas de indicadores KPI con íconos circulares.
 * - Accesos interactivos (TarjetaMenuAccionModerna) específicos para el rol activo.
 * - Vista rápida de los viajes recientes con conector visual Origen -> Destino (LineaRutaTimeline).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioRolScreen(
    viewModel: BelmonteViewModel,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val usuarioActual by viewModel.usuarioActual.collectAsState()
    val empresas by viewModel.empresas.collectAsState()
    val todosLosViajes by viewModel.todosLosViajes.collectAsState()
    val cuposTrabajador by viewModel.cuposTrabajador.collectAsState()

    val usuario = usuarioActual ?: return
    val empresaDelUsuario = empresas.find { it.idEmpresa == usuario.idEmpresa }

    val etiquetaRol = when (usuario.rol) {
        "ROLE_SOLICITANTE" -> "Trabajador Solicitante"
        "ROLE_CONDUCTOR" -> "Conductor de Flota"
        "ROLE_ADMIN" -> "Recepcionista / Admin Central"
        "ROLE_ADMIN_EMPRESA" -> "Administrador Empresa Cliente"
        else -> usuario.rol
    }

    val rutaListadoPrincipal = when (usuario.rol) {
        "ROLE_SOLICITANTE" -> Rutas.MIS_VIAJES
        "ROLE_CONDUCTOR" -> Rutas.VIAJES_CONDUCTOR
        "ROLE_ADMIN" -> Rutas.SOLICITUDES_PENDIENTES
        "ROLE_ADMIN_EMPRESA" -> Rutas.MIS_CUPOS_EMPRESA
        else -> Rutas.INICIO_ROL
    }

    val rutaAccionSecundaria = when (usuario.rol) {
        "ROLE_SOLICITANTE" -> Rutas.NUEVA_SOLICITUD
        "ROLE_CONDUCTOR" -> "escanear_qr/103"
        "ROLE_ADMIN" -> Rutas.SOLICITUDES_PENDIENTES
        "ROLE_ADMIN_EMPRESA" -> Rutas.COBROS_EMPRESA
        else -> "escanear_qr/103"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = AzulBelmonte.copy(alpha = 0.28f),
                            border = BorderStroke(1.dp, AzulBelmonteClaro)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Belmonte SpA",
                                fontWeight = FontWeight.ExtraBold,
                                color = TextoBlanco,
                                fontSize = 16.sp
                            )
                            Text(
                                text = etiquetaRol,
                                style = MaterialTheme.typography.labelSmall,
                                color = AzulBelmonteClaro
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigate("escanear_qr/103") }) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Escanear QR",
                            tint = AzulBelmonteClaro
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.cerrarSesion()
                            onLogout()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = RojoAccionBelmonte
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulMarinoBelmonte
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SuperficieOscura,
                tonalElevation = 10.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = AzulBelmonteClaro,
                        indicatorColor = AzulBelmonte,
                        unselectedIconColor = TextoSecundarioOscuro,
                        unselectedTextColor = TextoSecundarioOscuro
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigate(rutaListadoPrincipal) },
                    icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = "Gestión") },
                    label = {
                        Text(
                            text = if (usuario.rol == "ROLE_ADMIN_EMPRESA") "Cupos" else "Viajes",
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextoSecundarioOscuro,
                        unselectedTextColor = TextoSecundarioOscuro
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigate(rutaAccionSecundaria) },
                    icon = {
                        Icon(
                            imageVector = when (usuario.rol) {
                                "ROLE_SOLICITANTE" -> Icons.Default.AddCircleOutline
                                "ROLE_ADMIN_EMPRESA" -> Icons.AutoMirrored.Filled.ReceiptLong
                                else -> Icons.Default.QrCodeScanner
                            },
                            contentDescription = "Acción"
                        )
                    },
                    label = {
                        Text(
                            text = when (usuario.rol) {
                                "ROLE_SOLICITANTE" -> "Pedir Viaje"
                                "ROLE_ADMIN_EMPRESA" -> "Cobros"
                                else -> "Lector QR"
                            },
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextoSecundarioOscuro,
                        unselectedTextColor = TextoSecundarioOscuro
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        viewModel.cerrarSesion()
                        onLogout()
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir") },
                    label = { Text("Login", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = RojoAccionBelmonte,
                        unselectedTextColor = RojoAccionBelmonte
                    )
                )
            }
        },
        containerColor = FondoOscuro
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // BARRA DE CAMBIO RÁPIDO DE ROL EN 1 TOQUE (ideal para el video de demostración)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChipCambioRol(
                    etiqueta = "Solicitante",
                    seleccionado = usuario.rol == "ROLE_SOLICITANTE",
                    onClick = {
                        viewModel.iniciarSesion("daniela.munoz@andessur.test", DatosInicialesBelmonte.CLAVE_PRUEBA)
                    }
                )
                ChipCambioRol(
                    etiqueta = "Conductor",
                    seleccionado = usuario.rol == "ROLE_CONDUCTOR",
                    onClick = {
                        viewModel.iniciarSesion("pedro.soto@belmonte.test", DatosInicialesBelmonte.CLAVE_PRUEBA)
                    }
                )
                ChipCambioRol(
                    etiqueta = "Admin Belmonte",
                    seleccionado = usuario.rol == "ROLE_ADMIN",
                    onClick = {
                        viewModel.iniciarSesion("valeria.admin@belmonte.test", DatosInicialesBelmonte.CLAVE_PRUEBA)
                    }
                )
                ChipCambioRol(
                    etiqueta = "Admin Empresa",
                    seleccionado = usuario.rol == "ROLE_ADMIN_EMPRESA",
                    onClick = {
                        viewModel.iniciarSesion("tomas.herrera@andessur.test", DatosInicialesBelmonte.CLAVE_PRUEBA)
                    }
                )
            }

            // 1. HERO BANNER CON DEGRADADO Y DATOS DEL USUARIO
            HeroBannerBelmonte(
                etiquetaSuperior = etiquetaRol,
                titulo = "Hola, ${usuario.nombreCompleto}",
                subtitulo = if (empresaDelUsuario != null) {
                    "${empresaDelUsuario.razonSocial} • RUT ${empresaDelUsuario.rutEmpresa}"
                } else {
                    "Personal Interno de Operaciones • Belmonte SpA"
                },
                icono = Icons.Default.Person
            )

            // 2. INDICADORES KPI CON ÍCONOS CIRCULARES SEGÚN EL ROL
            when (usuario.rol) {
                "ROLE_SOLICITANTE" -> {
                    val misViajes = todosLosViajes.filter { it.idSolicitante == usuario.idUsuario }
                    val disponibles = cuposTrabajador.sumOf { it.disponibles }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiCardModerna(
                            modifier = Modifier.weight(1f),
                            titulo = "Cupos Libres",
                            valor = "$disponibles",
                            icono = Icons.Default.CheckCircle,
                            color = ColorEstadoEnCurso
                        )
                        KpiCardModerna(
                            modifier = Modifier.weight(1f),
                            titulo = "Mis Viajes",
                            valor = "${misViajes.size}",
                            icono = Icons.Default.DirectionsCar,
                            color = AzulBelmonteClaro
                        )
                        KpiCardModerna(
                            modifier = Modifier.weight(1f),
                            titulo = "Activos",
                            valor = "${misViajes.count { it.idEstado in listOf(1, 2, 3) }}",
                            icono = Icons.Default.Schedule,
                            color = ColorEstadoPendiente
                        )
                    }
                }

                "ROLE_CONDUCTOR" -> {
                    val misViajesConductor = todosLosViajes.filter { it.idConductor == usuario.idUsuario }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiCardModerna(
                            modifier = Modifier.weight(1f),
                            titulo = "Asignados",
                            valor = "${misViajesConductor.count { it.idEstado == 2 }}",
                            icono = Icons.Default.Schedule,
                            color = ColorEstadoAsignado
                        )
                        KpiCardModerna(
                            modifier = Modifier.weight(1f),
                            titulo = "En Curso",
                            valor = "${misViajesConductor.count { it.idEstado == 3 }}",
                            icono = Icons.Default.DirectionsCar,
                            color = ColorEstadoEnCurso
                        )
                        KpiCardModerna(
                            modifier = Modifier.weight(1f),
                            titulo = "Finalizados",
                            valor = "${misViajesConductor.count { it.idEstado == 4 }}",
                            icono = Icons.Default.CheckCircle,
                            color = ColorEstadoFinalizado
                        )
                    }
                }

                "ROLE_ADMIN" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiCardModerna(
                            modifier = Modifier.weight(1f),
                            titulo = "Por Asignar",
                            valor = "${todosLosViajes.count { it.idEstado == 1 }}",
                            icono = Icons.Default.PendingActions,
                            color = ColorEstadoPendiente
                        )
                        KpiCardModerna(
                            modifier = Modifier.weight(1f),
                            titulo = "En Ruta",
                            valor = "${todosLosViajes.count { it.idEstado == 3 }}",
                            icono = Icons.Default.DirectionsCar,
                            color = ColorEstadoEnCurso
                        )
                        KpiCardModerna(
                            modifier = Modifier.weight(1f),
                            titulo = "Total Flota",
                            valor = "${todosLosViajes.size}",
                            icono = Icons.Default.CheckCircle,
                            color = AzulBelmonteClaro
                        )
                    }
                }

                "ROLE_ADMIN_EMPRESA" -> {
                    val viajesEmpresa = todosLosViajes.filter { it.idEmpresa == usuario.idEmpresa }
                    val kmExtraTotal = viajesEmpresa.filter { it.idEstado == 4 }.sumOf { it.cargoExtra }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiCardModerna(
                            modifier = Modifier.weight(1f),
                            titulo = "Viajes Mes",
                            valor = "${viajesEmpresa.size}",
                            icono = Icons.Default.DirectionsCar,
                            color = AzulBelmonteClaro
                        )
                        KpiCardModerna(
                            modifier = Modifier.weight(1f),
                            titulo = "Finalizados",
                            valor = "${viajesEmpresa.count { it.idEstado == 4 }}",
                            icono = Icons.Default.CheckCircle,
                            color = ColorEstadoEnCurso
                        )
                        KpiCardModerna(
                            modifier = Modifier.weight(1f),
                            titulo = "Extras CLP",
                            valor = "$$kmExtraTotal",
                            icono = Icons.AutoMirrored.Filled.ReceiptLong,
                            color = ColorEstadoPendiente
                        )
                    }
                }
            }

            // 3. MÓDULOS DE OPERACIÓN DEL ROL (TARJETAS MODERNAS CON ÍCONO Y FLECHA)
            Text(
                text = "Módulos de Operación",
                style = MaterialTheme.typography.titleMedium,
                color = TextoBlanco,
                fontWeight = FontWeight.ExtraBold
            )

            when (usuario.rol) {
                "ROLE_SOLICITANTE" -> {
                    TarjetaMenuAccionModerna(
                        titulo = "Nueva Solicitud de Traslado",
                        subtitulo = "CU-02 • Elige Auto, Van o Camión y calcula distancia GPS",
                        icono = Icons.Default.AddCircleOutline,
                        colorAcento = RojoAccionBelmonte,
                        badgeTexto = "CU-02",
                        onClick = { onNavigate(Rutas.NUEVA_SOLICITUD) }
                    )
                    TarjetaMenuAccionModerna(
                        titulo = "Mis Viajes y Seguimiento",
                        subtitulo = "CU-09 • Revisa estados en tiempo real o cancela solicitudes",
                        icono = Icons.AutoMirrored.Filled.ListAlt,
                        colorAcento = AzulBelmonte,
                        badgeTexto = "CU-09",
                        onClick = { onNavigate(Rutas.MIS_VIAJES) }
                    )
                    TarjetaMenuAccionModerna(
                        titulo = "Validar Vehículo por QR",
                        subtitulo = "Fase 4 • Cámara en vivo con CameraX + ML Kit para verificar patente",
                        icono = Icons.Default.QrCodeScanner,
                        colorAcento = ColorEstadoEnCurso,
                        badgeTexto = "QR CAM",
                        onClick = { onNavigate("escanear_qr/103") }
                    )
                }

                "ROLE_CONDUCTOR" -> {
                    TarjetaMenuAccionModerna(
                        titulo = "Mis Viajes del Día",
                        subtitulo = "CU-04 al CU-08 • Iniciar/finalizar ruta, Google Maps/Waze e incidencias",
                        icono = Icons.Default.DirectionsCar,
                        colorAcento = ColorEstadoEnCurso,
                        badgeTexto = "CU-04",
                        onClick = { onNavigate(Rutas.VIAJES_CONDUCTOR) }
                    )
                    TarjetaMenuAccionModerna(
                        titulo = "Escanear QR de Patente",
                        subtitulo = "CU-05 • Cámara en vivo con CameraX + ML Kit para validar vehículo",
                        icono = Icons.Default.QrCodeScanner,
                        colorAcento = RojoAccionBelmonte,
                        badgeTexto = "QR CAM",
                        onClick = { onNavigate("escanear_qr/103") }
                    )
                }

                "ROLE_ADMIN" -> {
                    TarjetaMenuAccionModerna(
                        titulo = "Bandeja de Solicitudes y Flota",
                        subtitulo = "CU-03 y CU-10 • Asignar chofer/vehículo o reasignar por incidencia",
                        icono = Icons.Default.PendingActions,
                        colorAcento = ColorEstadoPendiente,
                        badgeTexto = "${todosLosViajes.count { it.idEstado == 1 }} PEND",
                        onClick = { onNavigate(Rutas.SOLICITUDES_PENDIENTES) }
                    )
                    TarjetaMenuAccionModerna(
                        titulo = "Verificador QR de Flota",
                        subtitulo = "Inspeccionar código QR de patentes KR-TW-21, LK-JH-44, PX-RT-89 y JT-KL-52",
                        icono = Icons.Default.QrCodeScanner,
                        colorAcento = AzulBelmonte,
                        badgeTexto = "QR CAM",
                        onClick = { onNavigate("escanear_qr/103") }
                    )
                }

                "ROLE_ADMIN_EMPRESA" -> {
                    TarjetaMenuAccionModerna(
                        titulo = "Cupos Contratados por Vehículo",
                        subtitulo = "Paquete mensual 2026-10 • Disponibles, repartidos y consumidos",
                        icono = Icons.Default.Business,
                        colorAcento = AzulBelmonte,
                        onClick = { onNavigate(Rutas.MIS_CUPOS_EMPRESA) }
                    )
                    TarjetaMenuAccionModerna(
                        titulo = "Repartir Cupos a Trabajadores",
                        subtitulo = "RN-08 • Asignar cupos de Auto, Van o Camión a empleados",
                        icono = Icons.Default.GroupAdd,
                        colorAcento = ColorEstadoEnCurso,
                        badgeTexto = "CUPOS",
                        onClick = { onNavigate(Rutas.REPARTIR_CUPOS) }
                    )
                    TarjetaMenuAccionModerna(
                        titulo = "Facturación y Cobros de Extras",
                        subtitulo = "RN-14 • Paquete mensual y liquidación de kilómetros extra",
                        icono = Icons.AutoMirrored.Filled.ReceiptLong,
                        colorAcento = RojoAccionBelmonte,
                        badgeTexto = "COBROS",
                        onClick = { onNavigate(Rutas.COBROS_EMPRESA) }
                    )
                }
            }

            // 4. VISTA RÁPIDA DE VIAJES RECIENTES CON CONECTOR VISUAL ORIGEN -> DESTINO
            val viajesDestacados = when (usuario.rol) {
                "ROLE_SOLICITANTE" -> todosLosViajes.filter { it.idSolicitante == usuario.idUsuario }.take(2)
                "ROLE_CONDUCTOR" -> todosLosViajes.filter { it.idConductor == usuario.idUsuario }.take(2)
                "ROLE_ADMIN_EMPRESA" -> todosLosViajes.filter { it.idEmpresa == usuario.idEmpresa }.take(2)
                else -> todosLosViajes.take(2)
            }

            if (viajesDestacados.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Actividad Reciente de Traslados",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextoBlanco,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Ver todos →",
                        fontSize = 12.sp,
                        color = AzulBelmonteClaro,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigate(rutaListadoPrincipal) }
                    )
                }

                viajesDestacados.forEach { viaje ->
                    BelmonteCard(
                        modifier = Modifier.clickable {
                            when (usuario.rol) {
                                "ROLE_CONDUCTOR" -> onNavigate("detalle_viaje_conductor/${viaje.idViaje}")
                                "ROLE_ADMIN" -> if (viaje.idEstado == 1) {
                                    onNavigate("asignar_viaje/${viaje.idViaje}")
                                } else {
                                    onNavigate("reasignar_viaje/${viaje.idViaje}")
                                }
                                else -> onNavigate("detalle_seguimiento/${viaje.idViaje}")
                            }
                        },
                        colorBorde = when (viaje.idEstado) {
                            1 -> ColorEstadoPendiente
                            2 -> ColorEstadoAsignado
                            3 -> ColorEstadoEnCurso
                            else -> AzulBelmonte
                        }
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
                                Text(
                                    text = "Viaje #${viaje.idViaje}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextoBlanco,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                ChipVehiculoBelmonte(
                                    idTipoVehiculo = viaje.idTipoVehiculo,
                                    textoPersonalizado = viaje.nombreTipoVehiculo
                                )
                            }
                            BadgeEstadoViaje(codigoEstado = viaje.codigoEstado)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

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
}

@Composable
private fun ChipCambioRol(
    etiqueta: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = {
            Text(
                text = etiqueta,
                fontSize = 11.sp,
                fontWeight = if (seleccionado) FontWeight.ExtraBold else FontWeight.Medium
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

@Composable
private fun KpiCardModerna(
    modifier: Modifier = Modifier,
    titulo: String,
    valor: String,
    icono: ImageVector,
    color: Color
) {
    Surface(
        modifier = modifier,
        color = SuperficieOscura,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                modifier = Modifier.size(30.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.18f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icono,
                        contentDescription = titulo,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                color = TextoBlanco,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = titulo,
                fontSize = 11.sp,
                color = TextoSecundarioOscuro,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
