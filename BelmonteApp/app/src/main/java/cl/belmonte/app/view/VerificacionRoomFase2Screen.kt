package cl.belmonte.app.view

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.belmonte.app.ui.theme.AzulBelmonte
import cl.belmonte.app.ui.theme.AzulMarinoBelmonte
import cl.belmonte.app.ui.theme.ColorEstadoCancelado
import cl.belmonte.app.ui.theme.ColorEstadoEnCurso
import cl.belmonte.app.ui.theme.RojoAccionBelmonte
import cl.belmonte.app.view.components.BadgeEstadoViaje
import cl.belmonte.app.view.components.BelmonteCard
import cl.belmonte.app.viewmodel.BelmonteViewModel

/**
 * Pantalla interactiva de prueba para la Fase 2 (Room Completo + Repositorios + Datos de Prueba).
 *
 * ¿QUÉ HACE ESTA PANTALLA?
 * Se conecta mediante BelmonteViewModel a la base de datos SQLite local (Room) y muestra en vivo:
 * 1. El conteo real de registros precargados desde script_belmonte_db_v3.sql (Usuarios, Empresas,
 *    Vehículos, Conductores y Viajes 101 al 107).
 * 2. Los cupos en tiempo real de Daniela Muñoz (Consulta C del script SQL: Asignados - Usados = Disponibles).
 * 3. Un botón de prueba en vivo que inserta una solicitud real en Room (consumiendo 1 cupo y calculando
 *    distancia Haversine) y botones para cancelar viajes pendientes (devolviendo el cupo según RN-07).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificacionRoomFase2Screen(
    viewModel: BelmonteViewModel
) {
    val usuarios by viewModel.todosLosUsuarios.collectAsState()
    val empresas by viewModel.empresas.collectAsState()
    val vehiculos by viewModel.vehiculos.collectAsState()
    val viajes by viewModel.todosLosViajes.collectAsState()
    val cuposDaniela by viewModel.cuposTrabajador.collectAsState()
    val mensajeFeedback by viewModel.mensajeFeedback.collectAsState()
    val esError by viewModel.esErrorFeedback.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "BELMONTE — FASE 2: ROOM LOCAL",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "13 Tablas @Entity · 4 DAOs · 4 Repositorios · Datos SQL v3",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulMarinoBelmonte,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
            // Banner de mensaje si el usuario ejecutó una acción de prueba en Room
            mensajeFeedback?.let { mensaje ->
                BelmonteCard(
                    colorBorde = if (esError) ColorEstadoCancelado else ColorEstadoEnCurso
                ) {
                    Text(
                        text = mensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (esError) ColorEstadoCancelado else ColorEstadoEnCurso
                    )
                }
            }

            // 1. Resumen de tablas precargadas en SQLite (Room)
            BelmonteCard(colorBorde = AzulBelmonte) {
                Text(
                    text = "Estado de la Base de Datos Local (belmonte_local.db)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Empresas cargadas: ${empresas.size} (Minera Andes Sur y Consultora Pacífico)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Usuarios cargados: ${usuarios.size} (4 roles, clave: Password123*)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Flota cargada: ${vehiculos.size} vehículos (${vehiculos.joinToString { it.patente }})",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Viajes en Room: ${viajes.size} traslados (con historial e incidencias)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // 2. Cupos en vivo desde Room (Consulta C del script SQL - Daniela Muñoz, ID=3)
            BelmonteCard {
                Text(
                    text = "Cupos Octubre 2026 — Daniela Muñoz (RN-07)",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(6.dp))
                cuposDaniela.forEach { resumen ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${resumen.tipoVehiculo.nombre} (tope ${resumen.tipoVehiculo.kmIncluidos} km)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Asig: ${resumen.asignados} | Usados: ${resumen.usados} | Disp: ${resumen.disponibles}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        // Inicia sesión como Daniela (id=3) si aún no lo está y crea un viaje de prueba en Room
                        viewModel.iniciarSesion("daniela.munoz@andessur.test", "Password123*") {
                            viewModel.crearSolicitudViaje(
                                idTipoVehiculo = 1, // AUTO
                                nombrePasajero = "Daniela Muñoz (Prueba Room)",
                                telefonoPasajero = "+56900000003",
                                cantidadPasajeros = 2,
                                descripcionCarga = null,
                                fechaServicio = "2026-10-20",
                                horaServicio = "11:30:00",
                                direccionOrigen = "Av. Apoquindo 4501, Las Condes",
                                latitudOrigen = -33.4116000,
                                longitudOrigen = -70.5843000,
                                direccionDestino = "Aeropuerto AMB, Pudahuel",
                                latitudDestino = -33.3930000,
                                longitudDestino = -70.7944000,
                                numeroVuelo = "LA800",
                                observaciones = "Creado en vivo para probar Room y consumo de cupo"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulBelmonte)
                ) {
                    Text("PROBAR INSERTAR VIAJE EN ROOM (CONSUME 1 CUPO AUTO)")
                }
            }

            // 3. Listado en vivo de los viajes almacenados en Room (101 al 107 + los nuevos)
            Text(
                text = "Viajes Precargados en Room (${viajes.size})",
                style = MaterialTheme.typography.titleLarge
            )

            viajes.forEach { viaje ->
                BelmonteCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Viaje #${viaje.idViaje} · ${viaje.nombreTipoVehiculo}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        BadgeEstadoViaje(codigoEstado = viaje.codigoEstado)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pasajero: ${viaje.nombrePasajero}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Fecha: ${viaje.fechaServicio} a las ${viaje.horaServicio}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Origen: ${viaje.direccionOrigen}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Destino: ${viaje.direccionDestino}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Distancia Haversine: ${viaje.distanciaKm} km | Km Extra: ${viaje.kmExtra} km | Cargo Extra: $${viaje.cargoExtra} CLP",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    if (!viaje.descripcionCarga.isNullOrBlank()) {
                        Text(
                            text = "Carga: ${viaje.descripcionCarga}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Si el viaje está PENDIENTE o ASIGNADO, permitir probar la cancelación y devolución de cupo
                    if (viaje.idEstado == 1 || viaje.idEstado == 2) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.cancelarViaje(
                                    idViaje = viaje.idViaje,
                                    motivo = "Cancelado en prueba de Fase 2 (devuelve cupo)"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RojoAccionBelmonte)
                        ) {
                            Text("CANCELAR VIAJE #${viaje.idViaje} (DEVUELVE 1 CUPO)")
                        }
                    }
                }
            }
        }
    }
}
