package cl.belmonte.app.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AirportShuttle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.belmonte.app.ui.theme.AzulBelmonte
import cl.belmonte.app.ui.theme.AzulBelmonteClaro
import cl.belmonte.app.ui.theme.AzulMarinoBelmonte
import cl.belmonte.app.ui.theme.ColorEstadoAsignado
import cl.belmonte.app.ui.theme.ColorEstadoCancelado
import cl.belmonte.app.ui.theme.ColorEstadoEnCurso
import cl.belmonte.app.ui.theme.ColorEstadoFinalizado
import cl.belmonte.app.ui.theme.ColorEstadoPendiente
import cl.belmonte.app.ui.theme.FondoOscuro
import cl.belmonte.app.ui.theme.RojoAccionBelmonte
import cl.belmonte.app.ui.theme.SuperficieOscura
import cl.belmonte.app.ui.theme.TextoBlanco
import cl.belmonte.app.ui.theme.TextoSecundarioOscuro

/**
 * Sistema de Componentes Visuales Modernos (inspirado en Awesome Jetpack Compose / Composables UI
 * y en las maquetas oficiales maquetas_belmonte.html).
 *
 * ¿QUÉ HACE ESTE ARCHIVO?
 * Reúne todos los bloques visuales reutilizables de la aplicación:
 * 1. BelmonteCard: Tarjeta moderna con franja lateral de acento (border-left de las maquetas) y sombra suave.
 * 2. BadgeEstadoViaje: Píldora semántica con punto luminoso e indicador de estado.
 * 3. HeroBannerBelmonte: Encabezado con degradado corporativo e ícono circular.
 * 4. LineaRutaTimeline: Conector visual Origen -> Destino estilo app de transporte (Uber/Cabify).
 * 5. MapaRutaVisualCard: Vista previa gráfica de mapa con grilla, ruta curva y coordenadas GPS.
 * 6. TarjetaMenuAccionModerna: Botón de menú tipo tarjeta interactiva con ícono, subtítulo y flecha.
 * 7. ChipVehiculoBelmonte: Etiqueta con ícono según el tipo de vehículo (Auto, Van o Camión).
 */

@Composable
fun BelmonteCard(
    modifier: Modifier = Modifier,
    colorBorde: Color = AzulBelmonte.copy(alpha = 0.55f),
    mostrarFranjaLateral: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = SuperficieOscura
        ),
        border = BorderStroke(1.dp, colorBorde.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Franja lateral izquierda de 5.dp idéntica a ".card { border-left: 5px solid }" de maquetas_belmonte.html
                    if (mostrarFranjaLateral) {
                        drawRect(
                            color = colorBorde,
                            topLeft = Offset.Zero,
                            size = size.copy(width = 6.dp.toPx())
                        )
                    }
                }
                .padding(
                    start = if (mostrarFranjaLateral) 18.dp else 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                ),
            content = content
        )
    }
}

/**
 * Etiqueta (Badge) estilo píldora moderna con punto indicador de color según el estado del viaje.
 */
@Composable
fun BadgeEstadoViaje(codigoEstado: String) {
    val colorEstado = when (codigoEstado.uppercase()) {
        "PENDIENTE" -> ColorEstadoPendiente
        "ASIGNADO" -> ColorEstadoAsignado
        "EN_CURSO" -> ColorEstadoEnCurso
        "FINALIZADO" -> ColorEstadoFinalizado
        "CANCELADO" -> ColorEstadoCancelado
        else -> AzulBelmonte
    }

    val textoVisible = when (codigoEstado.uppercase()) {
        "PENDIENTE" -> "Pendiente"
        "ASIGNADO" -> "Asignado"
        "EN_CURSO" -> "En Curso"
        "FINALIZADO" -> "Finalizado"
        "CANCELADO" -> "Cancelado"
        else -> codigoEstado
    }

    Surface(
        color = colorEstado.copy(alpha = 0.16f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, colorEstado.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(colorEstado)
            )
            Text(
                text = textoVisible,
                color = colorEstado,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

/**
 * Encabezado Hero con degradado diagonal, ícono circular y etiqueta superior.
 */
@Composable
fun HeroBannerBelmonte(
    etiquetaSuperior: String,
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    colorPrimario: Color = AzulMarinoBelmonte,
    colorSecundario: Color = AzulBelmonte
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(colorPrimario, colorSecundario)
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.16f),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = etiquetaSuperior.uppercase(),
                        color = AzulBelmonteClaro,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextoBlanco,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoBlanco.copy(alpha = 0.85f)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                modifier = Modifier.size(54.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

/**
 * Conector visual de ruta Origen -> Destino estilo Uber / Cabify.
 * Muestra un nodo verde de salida, línea punteada vertical y pin rojo de llegada.
 */
@Composable
fun LineaRutaTimeline(
    origen: String,
    destino: String,
    distanciaKm: Double? = null,
    kmExtra: Double = 0.0,
    cargoExtra: Int = 0
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = FondoOscuro.copy(alpha = 0.65f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(ColorEstadoEnCurso.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ColorEstadoEnCurso)
                        )
                    }
                    Canvas(
                        modifier = Modifier
                            .width(2.dp)
                            .height(26.dp)
                    ) {
                        drawLine(
                            color = TextoSecundarioOscuro.copy(alpha = 0.55f),
                            start = Offset(size.width / 2, 0f),
                            end = Offset(size.width / 2, size.height),
                            strokeWidth = 3f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = RojoAccionBelmonte,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ORIGEN",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorEstadoEnCurso
                    )
                    Text(
                        text = origen,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoBlanco,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "DESTINO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = RojoAccionBelmonte
                    )
                    Text(
                        text = destino,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoBlanco,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (distanciaKm != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = AzulBelmonte.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Route,
                                contentDescription = null,
                                tint = AzulBelmonteClaro,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "$distanciaKm km estimados",
                                fontSize = 11.sp,
                                color = AzulBelmonteClaro,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (kmExtra > 0) {
                        Surface(
                            color = ColorEstadoPendiente.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "+$kmExtra km extra ($$cargoExtra)",
                                fontSize = 11.sp,
                                color = ColorEstadoPendiente,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "0 km extra ($0)",
                            fontSize = 11.sp,
                            color = ColorEstadoEnCurso,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Vista previa gráfica de mapa (inspirada en .map-placeholder de maquetas_belmonte.html).
 * Dibuja con Canvas una cuadrícula urbana, el trazado curvo de la ruta y los marcadores GPS.
 */
@Composable
fun MapaRutaVisualCard(
    latOrigen: Double,
    lngOrigen: Double,
    latDestino: Double,
    lngDestino: Double,
    distanciaKm: Double?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(145.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0A1426))
            .border(1.dp, AzulBelmonte.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 28.dp.toPx()
            val gridColor = Color(0xFF1E293B).copy(alpha = 0.7f)

            var x = 0f
            while (x < size.width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.2f)
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.2f)
                y += step
            }

            drawLine(
                color = Color(0xFF1E3A5F).copy(alpha = 0.55f),
                start = Offset(0f, size.height * 0.85f),
                end = Offset(size.width, size.height * 0.15f),
                strokeWidth = 6f
            )

            val startPt = Offset(size.width * 0.16f, size.height * 0.72f)
            val endPt = Offset(size.width * 0.84f, size.height * 0.28f)
            val path = Path().apply {
                moveTo(startPt.x, startPt.y)
                cubicTo(
                    size.width * 0.42f, size.height * 0.78f,
                    size.width * 0.55f, size.height * 0.20f,
                    endPt.x, endPt.y
                )
            }

            drawPath(
                path = path,
                color = AzulBelmonte.copy(alpha = 0.35f),
                style = Stroke(width = 14f, cap = StrokeCap.Round)
            )
            drawPath(
                path = path,
                color = Color(0xFF38BDF8),
                style = Stroke(
                    width = 6f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                )
            )

            drawCircle(ColorEstadoEnCurso.copy(alpha = 0.3f), radius = 22f, center = startPt)
            drawCircle(ColorEstadoEnCurso, radius = 10f, center = startPt)

            drawCircle(RojoAccionBelmonte.copy(alpha = 0.3f), radius = 24f, center = endPt)
            drawCircle(RojoAccionBelmonte, radius = 10f, center = endPt)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = FondoOscuro.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "VISTA DE RUTA GPS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextoBlanco
                        )
                    }
                }

                if (distanciaKm != null) {
                    Surface(
                        color = AzulBelmonte,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "$distanciaKm km",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = FondoOscuro.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "A: %.4f, %.4f".format(latOrigen, lngOrigen),
                        fontSize = 10.sp,
                        color = ColorEstadoEnCurso,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
                Surface(
                    color = FondoOscuro.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "B: %.4f, %.4f".format(latDestino, lngDestino),
                        fontSize = 10.sp,
                        color = RojoAccionBelmonte,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

/**
 * Chip con ícono y nombre del tipo de vehículo (1 = Auto Ejecutivo, 2 = Van Grupos, 3 = Camión Carga).
 */
@Composable
fun ChipVehiculoBelmonte(idTipoVehiculo: Int, textoPersonalizado: String? = null) {
    val icono = when (idTipoVehiculo) {
        1 -> Icons.Default.DirectionsCar
        2 -> Icons.Default.AirportShuttle
        else -> Icons.Default.LocalShipping
    }
    val nombre = textoPersonalizado ?: when (idTipoVehiculo) {
        1 -> "Auto Ejecutivo"
        2 -> "Van (Grupos)"
        else -> "Camión (Carga)"
    }

    Surface(
        color = AzulBelmonte.copy(alpha = 0.16f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, AzulBelmonte.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = AzulBelmonteClaro,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = nombre,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AzulBelmonteClaro
            )
        }
    }
}

/**
 * Tarjeta interactiva de acción (estilo Awesome Jetpack Compose) con ícono en caja coloreada,
 * título, descripción y flecha indicadora a la derecha.
 */
@Composable
fun TarjetaMenuAccionModerna(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    colorAcento: Color,
    badgeTexto: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieOscura),
        border = BorderStroke(1.dp, colorAcento.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(14.dp),
                color = colorAcento.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, colorAcento.copy(alpha = 0.45f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icono,
                        contentDescription = titulo,
                        tint = colorAcento,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextoBlanco,
                        fontWeight = FontWeight.Bold
                    )
                    if (badgeTexto != null) {
                        Surface(
                            color = colorAcento,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = badgeTexto,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundarioOscuro
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.06f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextoBlanco
                    )
                }
            }
        }
    }
}
