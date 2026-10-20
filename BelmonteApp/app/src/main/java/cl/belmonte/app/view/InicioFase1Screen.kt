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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.belmonte.app.ui.theme.AzulBelmonte
import cl.belmonte.app.ui.theme.AzulMarinoBelmonte
import cl.belmonte.app.ui.theme.RojoAccionBelmonte
import cl.belmonte.app.view.components.BadgeEstadoViaje
import cl.belmonte.app.view.components.BelmonteCard

/**
 * Pantalla de verificación inicial para la Fase 1.
 *
 * ¿QUÉ HACE ESTA PANTALLA?
 * Muestra el tema visual de Belmonte SpA (fondo oscuro, azul #2563EB, azul marino #002B5B,
 * tarjetas redondeadas y badges de los 5 estados del viaje) para comprobar que el proyecto
 * compila, corre en el emulador y tiene todos los paquetes listos antes de pasar a la Fase 2.
 *
 * ¿POR QUÉ SE HACE ASÍ?
 * Permite verificar visualmente el diseño Material 3 desde el primer commit antes de conectar
 * la base de datos Room y el NavHost completo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioFase1Screen() {
    val estadosDemo = listOf("PENDIENTE", "ASIGNADO", "EN_CURSO", "FINALIZADO", "CANCELADO")
    var indiceEstado by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "TRASLADOS BELMONTE SpA",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Fase 1: Base del Proyecto y Tema Material 3",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta 1: Presentación corporativa y verificación de colores
            BelmonteCard(colorBorde = AzulBelmonte) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Viaje de Muestra #101",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    BadgeEstadoViaje(codigoEstado = estadosDemo[indiceEstado])
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Pasajero: Andrea Lagos Vidal (Minera Andes Sur SpA)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Origen: Isidora Goyenechea 3000, Las Condes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Destino: Aeropuerto AMB, Pudahuel (Vuelo LA452)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { indiceEstado = (indiceEstado + 1) % estadosDemo.size },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulBelmonte)
                ) {
                    Text("PROBAR CAMBIO DE BADGE DE ESTADO")
                }
            }

            // Tarjeta 2: Verificación de estructura de paquetes obligatoria
            BelmonteCard {
                Text(
                    text = "Paquetes Configurados (cl.belmonte.app)",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                val paquetes = listOf(
                    "model → Entidades @Entity de Room",
                    "dao → Interfaces @Dao con consultas SQL",
                    "database → AppDatabase (RoomDatabase) + datos de prueba",
                    "repository → Capa intermedia entre DAO y ViewModel",
                    "viewmodel → Estado reactivo con StateFlow",
                    "view y view.components → Pantallas Compose",
                    "navigation → NavHost y rutas",
                    "qr → Escáner QR con CameraX + ML Kit",
                    "ui.theme → Tema oscuro Material 3 (#2563EB)"
                )
                paquetes.forEach { item ->
                    Text(
                        text = "• $item",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            // Botón de estilo secundario (Rojo coral de las maquetas HTML)
            Button(
                onClick = { indiceEstado = 0 },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RojoAccionBelmonte)
            ) {
                Text("REINICIAR ESTADO A PENDIENTE")
            }
        }
    }
}
