package cl.belmonte.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
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
 * Pantalla Principal Exclusiva según el Rol Autenticado (Inicio / Dashboard).
 *
 * ¿QUÉ HACE ESTA PANTALLA?
 * Presenta únicamente el entorno de trabajo del perfil que inició sesión (sin mezclar opciones
 * ni pestañas de otros roles):
 * - ROLE_SOLICITANTE (Trabajador): ve sus cupos asignados, pide traslados (CU-02), sigue sus viajes (CU-09) y escanea QR.
 * - ROLE_ADMIN_EMPRESA (Admin de Empresa): contrata planes de suscripción mensual, revisa sus cobros y reparte cupos a sus trabajadores.
 * - ROLE_ADMIN (Admin de Belmonte): gestiona el despacho central (asignar/reasignar choferes) y supervisa/emite cobros B2B.
 * - ROLE_CONDUCTOR (Chofer): revisa únicamente sus viajes asignados, inicia/finaliza ruta con Maps/Waze e informa incidencias.
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
        "ROLE_SOLICITANTE" -> "Portal Trabajador / Solicitante"
        "ROLE_CONDUCTOR" -> "Portal Conductor de Flota"
        "ROLE_ADMIN" -> "Portal Administración Belmonte"
        "ROLE_ADMIN_EMPRESA" -> "Portal Administrador de Empresa"
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
        "ROLE_ADMIN" -> Rutas.COBROS_EMPRESA
        "ROLE_ADMIN_EMPRESA" -> Rutas.COBROS_EMPRESA
        else -> Rutas.INICIO_ROL
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
                    if (usuario.rol != "ROLE_ADMIN_EMPRESA") {
                        IconButton(onClick = { onNavigate("escanear_qr/103") }) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Escanear QR",
                                tint = AzulBelmonteClaro
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            viewModel.cerrarSesion()
                            onLogout()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cambiar perfil / Salir",
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
                            text = when (usuario.rol) {
                                "ROLE_ADMIN_EMPRESA" -> "Mis Cupos"
                                "ROLE_ADMIN" -> "Despacho"
                                "ROLE_CONDUCTOR" -> "Hoja Ruta"
                                else -> "Mis Viajes"
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
                    onClick = { onNavigate(rutaAccionSecundaria) },
                    icon = {
                        Icon(
                            imageVector = when (usuario.rol) {
                                "ROLE_SOLICITANTE" -> Icons.Default.AddCircleOutline
                                "ROLE_ADMIN_EMPRESA", "ROLE_ADMIN" -> Icons.AutoMirrored.Filled.ReceiptLong
                                else -> Icons.Default.QrCodeScanner
                            },
                            contentDescription = "Acción"
                        )
                    },
                    label = {
                        Text(
                            text = when (usuario.rol) {
                                "ROLE_SOLICITANTE" -> "Pedir Viaje"
                                "ROLE_ADMIN_EMPRESA" -> "Planes"
                                "ROLE_ADMIN" -> "Facturación"
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
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Cambiar Perfil") },
                    label = { Text("Perfiles", fontSize = 11.sp) },
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
            // 1. HERO BANNER CON DEGRADADO Y DATOS DEL USUARIO AUTENTICADO
            HeroBannerBelmonte(
                etiquetaSuperior = etiquetaRol.uppercase(),
                titulo = "Hola, ${usuario.nombreCompleto}",
                subtitulo = if (empresaDelUsuario != null) {
                    "${empresaDelUsuario.razonSocial} • RUT ${empresaDelUsuario.rutEmpresa}"
                } else {
                    "Personal Interno de Operaciones • Belmonte SpA"
                },
                icono = Icons.Default.Person
            )

            // 2. INDICADORES KPI EXCLUSIVOS SEGÚN EL ROL
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
                            titulo = "Mis Cupos Libres",
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
                            titulo = "En Curso / Pend.",
                            valor = "${misViajes.count { it.idEstado in listOf(1, 2, 3) }}",
                            icono = Icons.Default.Schedule,
                            color = ColorEstadoPendiente
                        )
                    }

                    // Resumen claro de los cupos que la empresa le asignó a este trabajador
                    if (cuposTrabajador.isNotEmpty()) {
                        BelmonteCard(colorBorde = AzulBelmonte) {
                            Text(
                                text = "Mis Cupos Asignados por Mi Empresa (Octubre 2026)",
                                style = MaterialTheme.typography.titleSmall,
                                color = AzulBelmonteClaro,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                cuposTrabajador.forEach { c ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = c.tipoVehiculo.nombre,
                                            fontSize = 11.sp,
                                            color = TextoSecundarioOscuro,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${c.disponibles} disp.",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (c.disponibles > 0) ColorEstadoEnCurso else ColorEstadoPendiente,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = "(de ${c.asignados})",
                                            fontSize = 10.sp,
                                            color = TextoSecundarioOscuro
                                        )
                                    }
                                }
                            }
                        }
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
                            titulo = "Por Iniciar",
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
                            titulo = "Viajes Empresa",
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

            // 3. MÓDULOS DE OPERACIÓN EXCLUSIVOS DE ESTE ROL (SIN MEZCLAR FUNCIONES)
            Text(
                text = when (usuario.rol) {
                    "ROLE_SOLICITANTE" -> "Servicios para el Trabajador"
                    "ROLE_ADMIN_EMPRESA" -> "Administración de Mi Empresa Cliente"
                    "ROLE_ADMIN" -> "Operaciones Centrales de Belmonte SpA"
                    "ROLE_CONDUCTOR" -> "Operación en Terreno del Conductor"
                    else -> "Módulos de Operación"
                },
                style = MaterialTheme.typography.titleMedium,
                color = TextoBlanco,
                fontWeight = FontWeight.ExtraBold
            )

            when (usuario.rol) {
                "ROLE_SOLICITANTE" -> {
                    TarjetaMenuAccionModerna(
                        titulo = "Pedir Nuevo Traslado Corporativo",
                        subtitulo = "CU-02 • Solicita Auto, Van o Camión usando 1 cupo asignado por tu empresa",
                        icono = Icons.Default.AddCircleOutline,
                        colorAcento = RojoAccionBelmonte,
                        badgeTexto = "CU-02",
                        onClick = { onNavigate(Rutas.NUEVA_SOLICITUD) }
                    )
                    TarjetaMenuAccionModerna(
                        titulo = "Mis Viajes y Seguimiento de Chofer",
                        subtitulo = "CU-09 • Revisa quién te recoge, patente, llama al conductor o cancela",
                        icono = Icons.AutoMirrored.Filled.ListAlt,
                        colorAcento = AzulBelmonte,
                        badgeTexto = "CU-09",
                        onClick = { onNavigate(Rutas.MIS_VIAJES) }
                    )
                    TarjetaMenuAccionModerna(
                        titulo = "Validar Patente del Vehículo por QR",
                        subtitulo = "Fase 4 • Escanea con la cámara el código QR al subir al vehículo asignado",
                        icono = Icons.Default.QrCodeScanner,
                        colorAcento = ColorEstadoEnCurso,
                        badgeTexto = "QR CAM",
                        onClick = { onNavigate("escanear_qr/103") }
                    )
                }

                "ROLE_CONDUCTOR" -> {
                    TarjetaMenuAccionModerna(
                        titulo = "Mi Hoja de Ruta del Día",
                        subtitulo = "CU-04 al CU-08 • Iniciar/finalizar viaje, abrir Google Maps/Waze e incidencias",
                        icono = Icons.Default.DirectionsCar,
                        colorAcento = ColorEstadoEnCurso,
                        badgeTexto = "CU-04",
                        onClick = { onNavigate(Rutas.VIAJES_CONDUCTOR) }
                    )
                    TarjetaMenuAccionModerna(
                        titulo = "Verificar QR de Mi Vehículo Asignado",
                        subtitulo = "CU-05 • Cámara en vivo con CameraX + ML Kit para validar patente de turno",
                        icono = Icons.Default.QrCodeScanner,
                        colorAcento = RojoAccionBelmonte,
                        badgeTexto = "QR CAM",
                        onClick = { onNavigate("escanear_qr/103") }
                    )
                }

                "ROLE_ADMIN" -> {
                    TarjetaMenuAccionModerna(
                        titulo = "Despacho Central: Asignar y Reasignar",
                        subtitulo = "CU-03 y CU-10 • Asignar conductor/vehículo a solicitudes o reasignar por incidencia",
                        icono = Icons.Default.PendingActions,
                        colorAcento = ColorEstadoPendiente,
                        badgeTexto = "${todosLosViajes.count { it.idEstado == 1 }} PEND",
                        onClick = { onNavigate(Rutas.SOLICITUDES_PENDIENTES) }
                    )
                    TarjetaMenuAccionModerna(
                        titulo = "Facturación B2B y Cobros a Empresas",
                        subtitulo = "RN-14 • Supervisar paquetes contratados y emitir cobro mensual de km extra",
                        icono = Icons.AutoMirrored.Filled.ReceiptLong,
                        colorAcento = Color(0xFFA855F7),
                        badgeTexto = "COBROS B2B",
                        onClick = { onNavigate(Rutas.COBROS_EMPRESA) }
                    )
                    TarjetaMenuAccionModerna(
                        titulo = "Inspección QR de Flota Belmonte",
                        subtitulo = "Verificar patentes oficiales KR-TW-21, LK-JH-44, PX-RT-89 y JT-KL-52",
                        icono = Icons.Default.QrCodeScanner,
                        colorAcento = AzulBelmonte,
                        badgeTexto = "QR FLOTA",
                        onClick = { onNavigate("escanear_qr/103") }
                    )
                }

                "ROLE_ADMIN_EMPRESA" -> {
                    TarjetaMenuAccionModerna(
                        titulo = "Planes de Suscripción y Facturación",
                        subtitulo = "Contratar Plan Básico (10A+1V), Grupos (10A+4V), Carga (10A+6V+5C) o Personalizado",
                        icono = Icons.AutoMirrored.Filled.ReceiptLong,
                        colorAcento = RojoAccionBelmonte,
                        badgeTexto = "PLANES",
                        onClick = { onNavigate(Rutas.COBROS_EMPRESA) }
                    )
                    TarjetaMenuAccionModerna(
                        titulo = "Cupos Contratados y Viajes de Mi Empresa",
                        subtitulo = "Paquete mensual 2026-10 • Ver cupos contratados, repartidos, usados y viajes",
                        icono = Icons.Default.Business,
                        colorAcento = AzulBelmonte,
                        badgeTexto = "MI EMPRESA",
                        onClick = { onNavigate(Rutas.MIS_CUPOS_EMPRESA) }
                    )
                    TarjetaMenuAccionModerna(
                        titulo = "Repartir Cupos a Mis Trabajadores",
                        subtitulo = "RN-12 • Distribuir cupos de Auto, Van o Camión entre los empleados de mi empresa",
                        icono = Icons.Default.GroupAdd,
                        colorAcento = ColorEstadoEnCurso,
                        badgeTexto = "REPARTIR",
                        onClick = { onNavigate(Rutas.REPARTIR_CUPOS) }
                    )
                }
            }

            // 4. VISTA RÁPIDA DE TRASLADOS FILTRADA ESTRICTAMENTE SEGÚN EL ROL
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
                        text = when (usuario.rol) {
                            "ROLE_SOLICITANTE" -> "Mis Solicitudes Recientes"
                            "ROLE_CONDUCTOR" -> "Mis Viajes Asignados"
                            "ROLE_ADMIN_EMPRESA" -> "Últimos Viajes de Mi Empresa"
                            else -> "Últimas Solicitudes en Central"
                        },
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
                                "ROLE_ADMIN_EMPRESA" -> onNavigate(Rutas.MIS_CUPOS_EMPRESA)
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

            // Botón claro al final para volver al selector de perfiles inicial si se desea cambiar de rol
            OutlinedButton(
                onClick = {
                    viewModel.cerrarSesion()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, RojoAccionBelmonte.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = RojoAccionBelmonte
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "CERRAR SESIÓN / CAMBIAR PERFIL DE INGRESO",
                    color = RojoAccionBelmonte,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
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
