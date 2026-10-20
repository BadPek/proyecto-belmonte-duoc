package cl.belmonte.app.qr

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.belmonte.app.ui.theme.AzulBelmonte
import cl.belmonte.app.ui.theme.AzulBelmonteClaro
import cl.belmonte.app.ui.theme.AzulMarinoBelmonte
import cl.belmonte.app.ui.theme.ColorEstadoCancelado
import cl.belmonte.app.ui.theme.ColorEstadoEnCurso
import cl.belmonte.app.view.components.BelmonteCard
import cl.belmonte.app.viewmodel.BelmonteViewModel

/**
 * Pantalla de Verificación de Patente por QR conectada a la navegación de la Fase 3.
 * En la Fase 4 se activa aquí mismo la vista previa en vivo de CameraX + ML Kit Barcode Scanning
 * junto con los archivos PNG de cada patente (KR-TW-21, LK-JH-44, PX-RT-89, JT-KL-52).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscanearQrScreen(
    idViaje: Long,
    viewModel: BelmonteViewModel,
    onBack: () -> Unit
) {
    val viaje by viewModel.observarViajePorId(idViaje).collectAsState(initial = null)
    val vehiculos by viewModel.vehiculos.collectAsState()
    val resultadoQr by viewModel.resultadoQr.collectAsState()

    val vehiculoAsignado = vehiculos.find { it.idVehiculo == viaje?.idVehiculo }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Validar QR Vehículo — Viaje #$idViaje") },
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
                    text = "Viaje #$idViaje · Pasajero: ${viaje?.nombrePasajero ?: ""}",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulBelmonteClaro
                )
                Text(
                    text = if (vehiculoAsignado != null) {
                        "Vehículo esperado: ${vehiculoAsignado.marca} ${vehiculoAsignado.modelo} — Patente: ${vehiculoAsignado.patente}"
                    } else {
                        "Este viaje aún no tiene vehículo asignado."
                    },
                    fontWeight = FontWeight.Bold
                )
            }

            resultadoQr?.let { (esCorrecto, mensaje) ->
                BelmonteCard(colorBorde = if (esCorrecto) ColorEstadoEnCurso else ColorEstadoCancelado) {
                    Text(
                        text = if (esCorrecto) "✓ PATENTE CONFIRMADA" else "✗ VEHÍCULO INCORRECTO",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (esCorrecto) ColorEstadoEnCurso else ColorEstadoCancelado
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(mensaje, fontWeight = FontWeight.SemiBold)
                }
            }

            BelmonteCard {
                Text(
                    text = "Simulador Rápido de Lectura de Patentes de Prueba",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulBelmonteClaro
                )
                Text(
                    text = "(En la Fase 4 se activa aquí el visor de cámara en tiempo real con CameraX + ML Kit y los PNG de los 4 QR).",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("KR-TW-21", "LK-JH-44").forEach { pat ->
                        Button(
                            onClick = { viewModel.verificarQrVehiculo(idViaje, pat) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AzulBelmonte)
                        ) {
                            Text("QR $pat")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("PX-RT-89", "JT-KL-52").forEach { pat ->
                        OutlinedButton(
                            onClick = { viewModel.verificarQrVehiculo(idViaje, pat) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("QR $pat")
                        }
                    }
                }
            }
        }
    }
}
