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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
 * Pantalla de Inicio de Sesión (Caso de Uso CU-01: Iniciar Sesión).
 *
 * ¿QUÉ HACE ESTA PANTALLA?
 * Presenta una interfaz móvil moderna (inspirada en maquetas_belmonte.html y Awesome Jetpack Compose)
 * con encabezado en degradado corporativo (#002B5B -> #2563EB), campos con íconos interactivos,
 * botón de acción principal (#E84C3D) y una cuadrícula 2x2 de tarjetas de acceso rápido por rol.
 */
@Composable
fun LoginScreen(
    viewModel: BelmonteViewModel,
    onLoginSuccess: () -> Unit
) {
    val mensajeFeedback by viewModel.mensajeFeedback.collectAsState()
    val esError by viewModel.esErrorFeedback.collectAsState()

    var identificador by remember { mutableStateOf("daniela.munoz@andessur.test") }
    var password by remember { mutableStateOf(DatosInicialesBelmonte.CLAVE_PRUEBA) }
    var mostrarPassword by remember { mutableStateOf(false) }

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
            // 1. HERO HEADER CON DEGRADADO CORPORATIVO (#002B5B -> #2563EB)
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
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(74.dp),
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
                        text = "App de Traslados Corporativos B2B",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AzulBelmonteClaro
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = ColorEstadoEnCurso.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, ColorEstadoEnCurso.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = ColorEstadoEnCurso,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Room Local DB Activa • QR CameraX",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorEstadoEnCurso
                            )
                        }
                    }
                }
            }

            // 2. TARJETA DEL FORMULARIO DE ACCESO (CU-01)
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
                    border = BorderStroke(1.dp, AzulBelmonte.copy(alpha = 0.45f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Iniciar Sesión (CU-01)",
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
                                    tint = AzulBelmonteClaro
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AzulBelmonte,
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
                                    tint = AzulBelmonteClaro
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
                                focusedBorderColor = AzulBelmonte,
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
                                text = "INICIAR SESIÓN",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // 3. CUADRÍCULA 2x2 DE ACCESO RÁPIDO POR ROL (1 CLIC)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Acceso Rápido por Rol (1 Clic)",
                            style = MaterialTheme.typography.titleMedium,
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TarjetaRolDemoModerna(
                            modifier = Modifier.weight(1f),
                            rolEtiqueta = "SOLICITANTE",
                            nombre = "Daniela Muñoz",
                            detalle = "Minera Andes Sur",
                            icono = Icons.Default.PersonPinCircle,
                            colorAcento = AzulBelmonte,
                            onClick = {
                                identificador = "daniela.munoz@andessur.test"
                                password = DatosInicialesBelmonte.CLAVE_PRUEBA
                                viewModel.iniciarSesion(identificador, password) {
                                    onLoginSuccess()
                                }
                            }
                        )
                        TarjetaRolDemoModerna(
                            modifier = Modifier.weight(1f),
                            rolEtiqueta = "CONDUCTOR",
                            nombre = "Pedro Soto",
                            detalle = "Viaje #103 / QR",
                            icono = Icons.Default.DirectionsCar,
                            colorAcento = ColorEstadoEnCurso,
                            onClick = {
                                identificador = "pedro.soto@belmonte.test"
                                password = DatosInicialesBelmonte.CLAVE_PRUEBA
                                viewModel.iniciarSesion(identificador, password) {
                                    onLoginSuccess()
                                }
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TarjetaRolDemoModerna(
                            modifier = Modifier.weight(1f),
                            rolEtiqueta = "ADMIN BELMONTE",
                            nombre = "Valeria Fuentes",
                            detalle = "Asigna y Reasigna",
                            icono = Icons.Default.SupportAgent,
                            colorAcento = ColorEstadoPendiente,
                            onClick = {
                                identificador = "valeria.admin@belmonte.test"
                                password = DatosInicialesBelmonte.CLAVE_PRUEBA
                                viewModel.iniciarSesion(identificador, password) {
                                    onLoginSuccess()
                                }
                            }
                        )
                        TarjetaRolDemoModerna(
                            modifier = Modifier.weight(1f),
                            rolEtiqueta = "ADMIN EMPRESA",
                            nombre = "Tomás Herrera",
                            detalle = "Cupos y Cobros",
                            icono = Icons.Default.BusinessCenter,
                            colorAcento = Color(0xFFA855F7),
                            onClick = {
                                identificador = "tomas.herrera@andessur.test"
                                password = DatosInicialesBelmonte.CLAVE_PRUEBA
                                viewModel.iniciarSesion(identificador, password) {
                                    onLoginSuccess()
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun TarjetaRolDemoModerna(
    modifier: Modifier = Modifier,
    rolEtiqueta: String,
    nombre: String,
    detalle: String,
    icono: ImageVector,
    colorAcento: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura),
        border = BorderStroke(1.dp, colorAcento.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = colorAcento.copy(alpha = 0.18f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icono,
                            contentDescription = rolEtiqueta,
                            tint = colorAcento,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Surface(
                    color = colorAcento.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = rolEtiqueta,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorAcento,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = nombre,
                style = MaterialTheme.typography.titleSmall,
                color = TextoBlanco,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = detalle,
                fontSize = 11.sp,
                color = TextoSecundarioOscuro
            )
        }
    }
}
