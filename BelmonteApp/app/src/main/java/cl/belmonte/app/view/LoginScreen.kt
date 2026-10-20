package cl.belmonte.app.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.belmonte.app.model.UsuarioEntity
import cl.belmonte.app.ui.theme.AzulBelmonte
import cl.belmonte.app.ui.theme.AzulBelmonteClaro
import cl.belmonte.app.ui.theme.AzulMarinoBelmonte
import cl.belmonte.app.ui.theme.ColorEstadoCancelado
import cl.belmonte.app.ui.theme.FondoOscuro
import cl.belmonte.app.ui.theme.RojoAccionBelmonte
import cl.belmonte.app.view.components.BelmonteCard
import cl.belmonte.app.viewmodel.BelmonteViewModel

/**
 * Pantalla de Inicio de Sesión (CU-01) — Traslados Belmonte SpA.
 *
 * ¿QUÉ HACE ESTA PANTALLA?
 * Permite al usuario ingresar su Correo o RUT y su Contraseña para autenticarse contra la
 * tabla local "usuarios" de Room. Según el rol del usuario autenticado (ROLE_SOLICITANTE,
 * ROLE_ADMIN, ROLE_CONDUCTOR o ROLE_ADMIN_EMPRESA), navega al menú principal de su perfil.
 *
 * ¿POR QUÉ TIENE BOTONES DE ACCESO RÁPIDO DE DEMOSTRACIÓN ABAJO?
 * Para que durante el video de 7 minutos y la defensa en vivo con el profesor puedan cambiar
 * entre los 4 roles con un solo toque, sin perder tiempo tipeando correos largos en el emulador.
 */
@Composable
fun LoginScreen(
    viewModel: BelmonteViewModel,
    onLoginSuccess: (UsuarioEntity) -> Unit
) {
    var identificador by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }

    val mensajeFeedback by viewModel.mensajeFeedback.collectAsState()
    val esError by viewModel.esErrorFeedback.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(AzulMarinoBelmonte, FondoOscuro)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Ícono y encabezado corporativo (inspirado en Pantalla 1 de maquetas_belmonte.html)
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = "Logo Belmonte",
                tint = AzulBelmonteClaro,
                modifier = Modifier.height(56.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "BELMONTE SpA",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Plataforma de Traslados Corporativos",
                style = MaterialTheme.typography.bodyMedium,
                color = AzulBelmonteClaro,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tarjeta principal de Login
            BelmonteCard(colorBorde = AzulBelmonte) {
                Text(
                    text = "Iniciar Sesión (CU-01)",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ingresa tu correo corporativo o RUT",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = identificador,
                    onValueChange = {
                        identificador = it
                        viewModel.limpiarMensaje()
                    },
                    label = { Text("Correo o RUT (Ej: 16473920-1)") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        viewModel.limpiarMensaje()
                    },
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                            Icon(
                                imageVector = if (mostrarPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Mostrar u ocultar contraseña"
                            )
                        }
                    },
                    visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Mensaje de error si las credenciales fallan
                if (mensajeFeedback != null && esError) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = mensajeFeedback ?: "",
                        color = ColorEstadoCancelado,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón principal de login con el color rojo coral de la maqueta (#E84C3D)
                Button(
                    onClick = {
                        viewModel.iniciarSesion(identificador, password) { usuario ->
                            onLoginSuccess(usuario)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RojoAccionBelmonte)
                ) {
                    Text(
                        text = "INICIAR SESIÓN",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta de acceso rápido para probar los 4 roles del script SQL con un toque
            BelmonteCard {
                Text(
                    text = " Cuentas de Prueba (script_belmonte_db_v3.sql)",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulBelmonteClaro
                )
                Text(
                    text = "Toca un perfil para autocompletar e ingresar (Clave: Password123*)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                val cuentasDemo = listOf(
                    Triple(
                        "1. Solicitante — Daniela Muñoz (Minera Andes Sur)",
                        "daniela.munoz@andessur.test",
                        "Pide viajes, ve sus cupos, seguimiento y escanea QR"
                    ),
                    Triple(
                        "2. Admin Belmonte — Valeria Fuentes",
                        "valeria.admin@belmonte.test",
                        "Ve solicitudes pendientes, asigna y reasigna chofer/auto"
                    ),
                    Triple(
                        "3. Conductor — Miguel Rojas (Viaje #102 Asignado)",
                        "miguel.rojas@belmonte.test",
                        "Inicia/finaliza viajes, navega con Maps/Waze, reporta incidencia"
                    ),
                    Triple(
                        "4. Conductor — Pedro Soto (Viaje #103 En Curso)",
                        "pedro.soto@belmonte.test",
                        "Tiene el viaje #103 en curso tras reasignación por panne"
                    ),
                    Triple(
                        "5. Admin Empresa — Tomás Herrera (Minera Andes Sur)",
                        "tomas.herrera@andessur.test",
                        "Ve cupos contratados/repartidos/usados, reparte cupos y ve cobros"
                    )
                )

                cuentasDemo.forEachIndexed { idx, (titulo, correo, subtitulo) ->
                    if (idx > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            identificador = correo
                            password = "Password123*"
                            viewModel.iniciarSesion(correo, "Password123*") { usuario ->
                                onLoginSuccess(usuario)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = titulo,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = subtitulo,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
