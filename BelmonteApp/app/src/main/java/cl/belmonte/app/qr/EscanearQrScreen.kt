package cl.belmonte.app.qr

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import cl.belmonte.app.view.components.BelmonteCard
import cl.belmonte.app.view.components.ChipVehiculoBelmonte
import cl.belmonte.app.viewmodel.BelmonteViewModel
import java.util.concurrent.Executors

/**
 * Pantalla del Lector de Código QR con Cámara en Vivo (CameraX + ML Kit Barcode Scanning — Fase 4).
 *
 * ¿QUÉ HACE ESTA PANTALLA?
 * 1. Solicita permiso en tiempo de ejecución para usar la cámara (Manifest.permission.CAMERA).
 * 2. Abre la vista previa real de la cámara trasera con CameraX (PreviewView dentro de AndroidView)
 *    y dibuja un visor óptico moderno con esquinas luminosas y línea láser.
 * 3. Conecta el analizador QrAnalyzer (ML Kit BarcodeScanning) para detectar cualquier código QR
 *    enfocado por la cámara y compararlo contra la patente del vehículo asignado al viaje en Room.
 * 4. Incluye además tarjetas QR interactivas de las 4 patentes de la flota (KR-TW-21, LK-JH-44,
 *    PX-RT-89 y JT-KL-52) para probar tanto con cámara como en 1 clic durante el video.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscanearQrScreen(
    idViaje: Long,
    viewModel: BelmonteViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val todosLosViajes by viewModel.todosLosViajes.collectAsState()
    val vehiculos by viewModel.vehiculos.collectAsState()
    val resultadoQr by viewModel.resultadoQr.collectAsState()

    var idViajeSeleccionado by remember { mutableLongStateOf(idViaje) }
    val viajeActual = todosLosViajes.find { it.idViaje == idViajeSeleccionado }
    val vehiculoEsperado = vehiculos.find { it.idVehiculo == viajeActual?.idVehiculo }

    var patenteManual by remember { mutableStateOf(vehiculoEsperado?.patente ?: "KR-TW-21") }
    var camaraActiva by remember { mutableStateOf(true) }
    var ultimoQrDetectadoCamara by remember { mutableStateOf<String?>(null) }

    // Estado del permiso de cámara en tiempo de ejecución
    var tienePermisoCamara by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val lanzadorPermisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        tienePermisoCamara = concedido
    }

    LaunchedEffect(Unit) {
        if (!tienePermisoCamara) {
            lanzadorPermisoCamara.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Escáner QR de Patente",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "CameraX + ML Kit Barcode Scanning",
                            fontSize = 11.sp,
                            color = AzulBelmonteClaro
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulMarinoBelmonte,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = FondoOscuro
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. SELECTOR DE VIAJE A VERIFICAR
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Selecciona el viaje cuyo vehículo deseas verificar:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextoSecundarioOscuro
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    todosLosViajes.filter { it.idVehiculo != null }.forEach { v ->
                        val autoV = vehiculos.find { it.idVehiculo == v.idVehiculo }
                        val seleccionado = v.idViaje == idViajeSeleccionado
                        FilterChip(
                            selected = seleccionado,
                            onClick = {
                                idViajeSeleccionado = v.idViaje
                                patenteManual = autoV?.patente ?: "KR-TW-21"
                                viewModel.limpiarResultadoQr()
                            },
                            label = {
                                Text(
                                    text = "Viaje #${v.idViaje} (${autoV?.patente ?: "S/P"})",
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
                }
            }

            // 2. TARJETA DEL VEHÍCULO ESPERADO SEGÚN ROOM
            BelmonteCard(colorBorde = AzulBelmonte) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Viaje #$idViajeSeleccionado • ${viajeActual?.nombrePasajero ?: ""}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextoBlanco,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (vehiculoEsperado != null) {
                            Text(
                                text = "${vehiculoEsperado.marca} ${vehiculoEsperado.modelo} (${vehiculoEsperado.color})",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSecundarioOscuro
                            )
                        }
                    }
                    if (vehiculoEsperado != null) {
                        Surface(
                            color = ColorEstadoEnCurso.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, ColorEstadoEnCurso)
                        ) {
                            Text(
                                text = vehiculoEsperado.patente,
                                color = ColorEstadoEnCurso,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // 3. VISOR DE CÁMARA EN VIVO (CAMERAX + ML KIT BARCODE SCANNING)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SuperficieOscura),
                border = BorderStroke(1.5.dp, AzulBelmonteClaro.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = AzulBelmonteClaro
                            )
                            Text(
                                text = "Cámara en Vivo (CameraX + ML Kit)",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextoBlanco,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Surface(
                            color = if (tienePermisoCamara && camaraActiva) {
                                ColorEstadoEnCurso.copy(alpha = 0.2f)
                            } else {
                                ColorEstadoPendiente.copy(alpha = 0.2f)
                            },
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = if (tienePermisoCamara && camaraActiva) "ESCANEANDO" else "PAUSADA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (tienePermisoCamara && camaraActiva) ColorEstadoEnCurso else ColorEstadoPendiente,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (tienePermisoCamara && camaraActiva) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black)
                                .border(1.dp, AzulBelmonteClaro.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Vista previa nativa de CameraX conectada a QrAnalyzer (ML Kit)
                            VistaCamaraQrEnVivo(
                                lifecycleOwner = lifecycleOwner,
                                onQrLeido = { valorQr ->
                                    ultimoQrDetectadoCamara = valorQr
                                    patenteManual = valorQr
                                    viewModel.verificarQrVehiculo(idViajeSeleccionado, valorQr)
                                }
                            )

                            // Marco óptico (Viewfinder) dibujado sobre la cámara con Canvas
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val boxSize = size.minDimension * 0.68f
                                val left = (size.width - boxSize) / 2f
                                val top = (size.height - boxSize) / 2f
                                val right = left + boxSize
                                val bottom = top + boxSize
                                val cornerLen = 30.dp.toPx()
                                val stroke = 4.dp.toPx()
                                val colorMarco = Color(0xFF38BDF8)

                                // Esquina superior izquierda
                                drawLine(colorMarco, Offset(left, top), Offset(left + cornerLen, top), stroke, StrokeCap.Round)
                                drawLine(colorMarco, Offset(left, top), Offset(left, top + cornerLen), stroke, StrokeCap.Round)
                                // Esquina superior derecha
                                drawLine(colorMarco, Offset(right, top), Offset(right - cornerLen, top), stroke, StrokeCap.Round)
                                drawLine(colorMarco, Offset(right, top), Offset(right, top + cornerLen), stroke, StrokeCap.Round)
                                // Esquina inferior izquierda
                                drawLine(colorMarco, Offset(left, bottom), Offset(left + cornerLen, bottom), stroke, StrokeCap.Round)
                                drawLine(colorMarco, Offset(left, bottom), Offset(left, bottom - cornerLen), stroke, StrokeCap.Round)
                                // Esquina inferior derecha
                                drawLine(colorMarco, Offset(right, bottom), Offset(right - cornerLen, bottom), stroke, StrokeCap.Round)
                                drawLine(colorMarco, Offset(right, bottom), Offset(right, bottom - cornerLen), stroke, StrokeCap.Round)

                                // Línea láser central
                                drawLine(
                                    color = RojoAccionBelmonte.copy(alpha = 0.85f),
                                    start = Offset(left + 12f, (top + bottom) / 2f),
                                    end = Offset(right - 12f, (top + bottom) / 2f),
                                    strokeWidth = 2.5.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }

                            // Etiqueta inferior dentro del visor
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 10.dp),
                                color = FondoOscuro.copy(alpha = 0.82f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = ultimoQrDetectadoCamara?.let { "Último QR leído: $it" }
                                        ?: "Apunta la cámara al código QR de la patente",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                    } else if (!tienePermisoCamara) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Permiso de cámara requerido para escanear con CameraX.",
                                color = ColorEstadoPendiente,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { lanzadorPermisoCamara.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = AzulBelmonte)
                            ) {
                                Text("CONCEDER PERMISO DE CÁMARA")
                            }
                        }
                    }
                }
            }

            // 4. RESULTADO DE LA VERIFICACIÓN CONTRA ROOM
            resultadoQr?.let { (coincide, mensaje) ->
                val colorRes = if (coincide) ColorEstadoEnCurso else ColorEstadoCancelado
                BelmonteCard(colorBorde = colorRes) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = colorRes.copy(alpha = 0.18f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (coincide) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = colorRes,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (coincide) {
                                    "VEHÍCULO VERIFICADO CORRECTAMENTE"
                                } else {
                                    "ALERTA: PATENTE NO CORRESPONDE"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = colorRes,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = mensaje,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoBlanco
                            )
                        }
                    }
                }
            }

            // 5. TARJETAS QR INTERACTIVAS DE LAS 4 PATENTES DE LA FLOTA (PARA DEMO EN EMULADOR)
            BelmonteCard {
                Text(
                    text = "Tarjetas QR Oficiales de la Flota (Prueba en 1 Clic)",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextoBlanco,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Toca cualquier tarjeta QR para simular su lectura inmediata por ML Kit o usa los PNG de C:\\dev\\belmonte\\qr_patentes_prueba:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundarioOscuro
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Cuadrícula 2x2 con las 4 patentes del script SQL
                val patentesFlota = listOf(
                    Triple("KR-TW-21", "Toyota Corolla", 1),
                    Triple("LK-JH-44", "Hyundai Staria Van", 2),
                    Triple("PX-RT-89", "Mercedes Vito Van", 2),
                    Triple("JT-KL-52", "Chevrolet NPR Camión", 3)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    patentesFlota.chunked(2).forEach { fila ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            fila.forEach { (patente, modelo, idTipo) ->
                                val esLaEsperada = vehiculoEsperado?.patente == patente
                                TarjetaSimulacionQrPatente(
                                    modifier = Modifier.weight(1f),
                                    patente = patente,
                                    modelo = modelo,
                                    idTipoVehiculo = idTipo,
                                    esLaEsperada = esLaEsperada,
                                    onClick = {
                                        patenteManual = patente
                                        ultimoQrDetectadoCamara = patente
                                        viewModel.verificarQrVehiculo(idViajeSeleccionado, patente)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = patenteManual,
                        onValueChange = { patenteManual = it.uppercase() },
                        label = { Text("Código QR / Patente") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            viewModel.verificarQrVehiculo(idViajeSeleccionado, patenteManual)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RojoAccionBelmonte),
                        modifier = Modifier.height(54.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("VALIDAR", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

/**
 * Componente que inicializa CameraX PreviewView + ImageAnalysis con QrAnalyzer (ML Kit).
 */
@Composable
private fun VistaCamaraQrEnVivo(
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onQrLeido: (String) -> Unit
) {
    val context = LocalContext.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(
                            cameraExecutor,
                            QrAnalyzer { codigoDetectado ->
                                ContextCompat.getMainExecutor(ctx).execute {
                                    onQrLeido(codigoDetectado)
                                }
                            }
                        )
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (_: Exception) {
                    // Si el emulador sólo tiene cámara frontal configurada, intenta con DEFAULT_FRONT_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_FRONT_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (_: Exception) {
                    }
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

/**
 * Tarjeta visual con representación de código QR para probar cada una de las 4 patentes en 1 toque.
 */
@Composable
private fun TarjetaSimulacionQrPatente(
    modifier: Modifier = Modifier,
    patente: String,
    modelo: String,
    idTipoVehiculo: Int,
    esLaEsperada: Boolean,
    onClick: () -> Unit
) {
    val colorBorde = if (esLaEsperada) ColorEstadoEnCurso else AzulBelmonte.copy(alpha = 0.45f)

    Surface(
        modifier = modifier.clickable { onClick() },
        color = FondoOscuro.copy(alpha = 0.8f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (esLaEsperada) 2.dp else 1.dp, colorBorde)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = patente,
                            tint = Color.Black,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                if (esLaEsperada) {
                    Surface(
                        color = ColorEstadoEnCurso.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "ASIGNADO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ColorEstadoEnCurso,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = patente,
                style = MaterialTheme.typography.titleMedium,
                color = TextoBlanco,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = modelo,
                fontSize = 11.sp,
                color = TextoSecundarioOscuro
            )
            ChipVehiculoBelmonte(idTipoVehiculo = idTipoVehiculo)
        }
    }
}
