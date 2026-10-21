package cl.belmonte.app.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonPinCircle
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.belmonte.app.database.DatosInicialesBelmonte
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
import cl.belmonte.app.viewmodel.BelmonteViewModel

/**
 * Datos descriptivos de cada uno de los 4 portales de usuario para evitar mezclar perfiles.
 */
private data class InfoPortalRol(
    val rol: String,
    val tituloPortal: String,
    val subtituloPortal: String,
    val etiquetaBadge: String,
    val icono: ImageVector,
    val colorAcento: Color,
    val emailPorDefecto: String,
    val cuentasDemo: List<CuentaDemoRol>
)

private data class CuentaDemoRol(
    val nombre: String,
    val cargoOEmpresa: String,
    val email: String,
    val rut: String
)

private val PORTALES_BELMONTE = listOf(
    InfoPortalRol(
        rol = "ROLE_SOLICITANTE",
        tituloPortal = "Trabajador que Ocupará Servicios",
        subtituloPortal = "Solicita traslados (Auto, Van o Camión) con los cupos de tu empresa, sigue tu viaje y valida el QR.",
        etiquetaBadge = "TRABAJADOR / PASAJERO",
        icono = Icons.Default.PersonPinCircle,
        colorAcento = AzulBelmonte,
        emailPorDefecto = "daniela.munoz@andessur.test",
        cuentasDemo = listOf(
            CuentaDemoRol(
                nombre = "Daniela Muñoz Pino",
                cargoOEmpresa = "Trabajadora • Minera Andes Sur SpA (Auto, Van y Camión)",
                email = "daniela.munoz@andessur.test",
                rut = "16473920-1"
            ),
            CuentaDemoRol(
                nombre = "Fernanda Castro Ríos",
                cargoOEmpresa = "Trabajadora • Consultora Pacífico Austral Ltda.",
                email = "fernanda.castro@pacificoaustral.test",
                rut = "18395026-6"
            )
        )
    ),
    InfoPortalRol(
        rol = "ROLE_ADMIN_EMPRESA",
        tituloPortal = "Administrador de Empresa Cliente",
        subtituloPortal = "Contrata planes mensuales (Básico, Grupos, Carga o Personalizado), revisa cobros y reparte cupos.",
        etiquetaBadge = "ADMIN EMPRESA",
        icono = Icons.Default.BusinessCenter,
        colorAcento = Color(0xFFA855F7),
        emailPorDefecto = "tomas.herrera@andessur.test",
        cuentasDemo = listOf(
            CuentaDemoRol(
                nombre = "Tomás Herrera Lagos",
                cargoOEmpresa = "Admin Empresa • Minera Andes Sur SpA",
                email = "tomas.herrera@andessur.test",
                rut = "15392847-9"
            ),
            CuentaDemoRol(
                nombre = "Ignacio Vera Salinas",
                cargoOEmpresa = "Admin Empresa • Consultora Pacífico Austral Ltda.",
                email = "ignacio.vera@pacificoaustral.test",
                rut = "17284561-4"
            )
        )
    ),
    InfoPortalRol(
        rol = "ROLE_ADMIN",
        tituloPortal = "Administrador Central de Belmonte",
        subtituloPortal = "Recepción y despacho de Belmonte: asigna choferes/vehículos, reasigna por incidencias y emite cobros.",
        etiquetaBadge = "ADMIN BELMONTE",
        icono = Icons.Default.SupportAgent,
        colorAcento = ColorEstadoPendiente,
        emailPorDefecto = "valeria.admin@belmonte.test",
        cuentasDemo = listOf(
            CuentaDemoRol(
                nombre = "Valeria Fuentes Rojas",
                cargoOEmpresa = "Administradora / Recepcionista Central • Belmonte SpA",
                email = "valeria.admin@belmonte.test",
                rut = "12845371-7"
            )
        )
    ),
    InfoPortalRol(
        rol = "ROLE_CONDUCTOR",
        tituloPortal = "Conductor de Flota Belmonte",
        subtituloPortal = "Revisa tu hoja de ruta en terreno, inicia/finaliza viajes, abre Google Maps o Waze y reporta incidencias.",
        etiquetaBadge = "CHOFER EN RUTA",
        icono = Icons.Default.DirectionsCar,
        colorAcento = ColorEstadoEnCurso,
        emailPorDefecto = "pedro.soto@belmonte.test",
        cuentasDemo = listOf(
            CuentaDemoRol(
                nombre = "Pedro Soto Morales",
                cargoOEmpresa = "Chofer Licencia A2 • Viaje #103 En Curso (Hyundai Santa Fe)",
                email = "pedro.soto@belmonte.test",
                rut = "14627385-8"
            ),
            CuentaDemoRol(
                nombre = "Miguel Rojas Contreras",
                cargoOEmpresa = "Chofer Licencia A2 • Viaje #102 Asignado (Toyota Camry)",
                email = "miguel.rojas@belmonte.test",
                rut = "13582947-1"
            ),
            CuentaDemoRol(
                nombre = "Jorge Alarcón Bravo",
                cargoOEmpresa = "Chofer Licencia A3 • Van Mercedes-Benz Vito",
                email = "jorge.alarcon@belmonte.test",
                rut = "19473826-9"
            ),
            CuentaDemoRol(
                nombre = "Luis Carrasco Vega",
                cargoOEmpresa = "Chofer Licencia A4 • Camión Hyundai HD65 (Carga)",
                email = "luis.carrasco@belmonte.test",
                rut = "15836204-K"
            )
        )
    )
)

/**
 * PASO 1 AL ABRIR LA APLICACIÓN: Pantalla Inicial de Selección de Perfil.
 *
 * ¿QUÉ HACE ESTA PANTALLA?
 * Pregunta al principio antes de entrar a la aplicación quién es el usuario:
 * - Trabajador que ocupará los servicios (ROLE_SOLICITANTE)
 * - Administrador de Empresa Cliente (ROLE_ADMIN_EMPRESA)
 * - Administrador Central de Belmonte (ROLE_ADMIN)
 * - Conductor de Flota Belmonte (ROLE_CONDUCTOR)
 *
 * Así cada perfil entra únicamente a su portal correspondiente sin mezclar vistas ni opciones.
 */
@Composable
fun SeleccionPerfilScreen(
    onPerfilSeleccionado: (String) -> Unit
) {
    Scaffold(
        containerColor = FondoOscuro
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ENCABEZADO DE BIENVENIDA CORPORATIVO
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                AzulMarinoBelmonte,
                                AzulBelmonte.copy(alpha = 0.85f),
                                FondoOscuro
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.14f),
                        border = BorderStroke(2.dp, AzulBelmonteClaro.copy(alpha = 0.7f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = "Logo Belmonte",
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "BELMONTE SpA",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextoBlanco,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Plataforma de Traslados Corporativos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AzulBelmonteClaro
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, AzulBelmonteClaro.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = ColorEstadoEnCurso,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Paso 1 de 2 • Selección de Portal por Perfil",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextoBlanco
                            )
                        }
                    }
                }
            }

            // PREGUNTA INICIAL Y TARJETAS SEPARADAS POR TIPO DE USUARIO
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "¿Con qué perfil deseas ingresar?",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextoBlanco,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Selecciona tu rol antes de entrar para acceder exclusivamente a tus herramientas sin mezclar funciones:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundarioOscuro
                )

                Spacer(modifier = Modifier.height(2.dp))

                PORTALES_BELMONTE.forEach { portal ->
                    TarjetaSeleccionPortalInicial(
                        portal = portal,
                        onClick = { onPerfilSeleccionado(portal.rol) }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun TarjetaSeleccionPortalInicial(
    portal: InfoPortalRol,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura),
        border = BorderStroke(1.5.dp, portal.colorAcento.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(14.dp),
                color = portal.colorAcento.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, portal.colorAcento.copy(alpha = 0.5f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = portal.icono,
                        contentDescription = portal.tituloPortal,
                        tint = portal.colorAcento,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    color = portal.colorAcento.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = portal.etiquetaBadge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = portal.colorAcento,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = portal.tituloPortal,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextoBlanco,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = portal.subtituloPortal,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundarioOscuro,
                    lineHeight = 16.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Ingresar a este portal",
                tint = portal.colorAcento,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * PASO 2: Pantalla de Inicio de Sesión (CU-01) exclusiva para el perfil seleccionado en el Paso 1.
 *
 * ¿QUÉ HACE ESTA PANTALLA?
 * Muestra el login únicamente para el rol elegido (Trabajador, Admin de Empresa, Admin de Belmonte
 * o Conductor), precargando sus cuentas de prueba específicas y permitiendo volver atrás si desea
 * cambiar de tipo de usuario.
 */
@Composable
fun LoginScreen(
    viewModel: BelmonteViewModel,
    onBackToSeleccionPerfil: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val rolSeleccionado by viewModel.rolPreseleccionado.collectAsState()
    val mensajeFeedback by viewModel.mensajeFeedback.collectAsState()
    val esError by viewModel.esErrorFeedback.collectAsState()

    val portalActual = remember(rolSeleccionado) {
        PORTALES_BELMONTE.find { it.rol == rolSeleccionado } ?: PORTALES_BELMONTE.first()
    }

    var identificador by remember(portalActual.rol) { mutableStateOf(portalActual.emailPorDefecto) }
    var password by remember(portalActual.rol) { mutableStateOf(DatosInicialesBelmonte.CLAVE_PRUEBA) }
    var mostrarPassword by remember { mutableStateOf(false) }

    LaunchedEffect(portalActual.rol) {
        identificador = portalActual.emailPorDefecto
        password = DatosInicialesBelmonte.CLAVE_PRUEBA
    }

    Scaffold(
        containerColor = FondoOscuro
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. HEADER PERSONALIZADO SEGÚN EL PERFIL ELEGIDO EN EL PASO 1
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                AzulMarinoBelmonte,
                                portalActual.colorAcento.copy(alpha = 0.55f),
                                FondoOscuro
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = onBackToSeleccionPerfil,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Cambiar perfil",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cambiar Perfil",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            color = portalActual.colorAcento.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, portalActual.colorAcento)
                        ) {
                            Text(
                                text = portalActual.etiquetaBadge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = portalActual.colorAcento.copy(alpha = 0.22f),
                        border = BorderStroke(2.dp, portalActual.colorAcento)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = portalActual.icono,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = portalActual.tituloPortal,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextoBlanco,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = portalActual.subtituloPortal,
                        style = MaterialTheme.typography.bodySmall,
                        color = AzulBelmonteClaro,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // 2. FORMULARIO DE AUTENTICACIÓN (CU-01) PARA ESTE PERFIL
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SuperficieOscura),
                    border = BorderStroke(1.dp, portalActual.colorAcento.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Credenciales de Acceso (CU-01)",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextoBlanco,
                            fontWeight = FontWeight.ExtraBold
                        )

                        OutlinedTextField(
                            value = identificador,
                            onValueChange = { identificador = it },
                            label = { Text("Correo corporativo o RUT") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = portalActual.colorAcento
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = portalActual.colorAcento,
                                unfocusedBorderColor = TextoSecundarioOscuro.copy(alpha = 0.35f),
                                focusedTextColor = TextoBlanco,
                                unfocusedTextColor = TextoBlanco
                            )
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = portalActual.colorAcento
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                                    Icon(
                                        imageVector = if (mostrarPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Mostrar contraseña",
                                        tint = TextoSecundarioOscuro
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = portalActual.colorAcento,
                                unfocusedBorderColor = TextoSecundarioOscuro.copy(alpha = 0.35f),
                                focusedTextColor = TextoBlanco,
                                unfocusedTextColor = TextoBlanco
                            )
                        )

                        if (mensajeFeedback != null && esError) {
                            Surface(
                                color = ColorEstadoCancelado.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, ColorEstadoCancelado),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = mensajeFeedback ?: "",
                                    color = ColorEstadoCancelado,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                viewModel.iniciarSesion(identificador, password) {
                                    onLoginSuccess()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RojoAccionBelmonte),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Login,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ENTRAR COMO ${portalActual.etiquetaBadge}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // 3. CUENTAS DISPONIBLES EXCLUSIVAMENTE PARA ESTE PERFIL
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cuentas de Prueba de este Perfil",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextoBlanco,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Clave: Password123*",
                            fontSize = 11.sp,
                            color = AzulBelmonteClaro,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    portalActual.cuentasDemo.forEach { cuenta ->
                        val seleccionada = identificador.equals(cuenta.email, ignoreCase = true) ||
                            identificador.equals(cuenta.rut, ignoreCase = true)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    identificador = cuenta.email
                                    password = DatosInicialesBelmonte.CLAVE_PRUEBA
                                    viewModel.iniciarSesion(identificador, password) {
                                        onLoginSuccess()
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (seleccionada) {
                                    portalActual.colorAcento.copy(alpha = 0.16f)
                                } else {
                                    SuperficieOscura
                                }
                            ),
                            border = BorderStroke(
                                width = if (seleccionada) 1.5.dp else 1.dp,
                                color = if (seleccionada) portalActual.colorAcento else portalActual.colorAcento.copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${cuenta.nombre} (${cuenta.rut})",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextoBlanco,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = cuenta.cargoOEmpresa,
                                        fontSize = 11.sp,
                                        color = AzulBelmonteClaro
                                    )
                                    Text(
                                        text = cuenta.email,
                                        fontSize = 11.sp,
                                        color = TextoSecundarioOscuro
                                    )
                                }

                                Surface(
                                    color = portalActual.colorAcento,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Entrar →",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
