package cl.belmonte.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Configuración tipográfica de Material Design 3 para BelmonteApp.
 *
 * ¿QUÉ HACE ESTE ARCHIVO?
 * Define los tamaños de letra, grosores (FontWeight) e interlineados para títulos,
 * subtítulos, textos normales y etiquetas de botones o badges.
 *
 * ¿POR QUÉ SE HACE ASÍ?
 * Así todas las pantallas usan MaterialTheme.typography.titleLarge o bodyMedium en vez
 * de poner tamaños fijos distintos en cada Text.
 */
val Typography = Typography(
    // Título grande para encabezados como "BELMONTE" en el Login
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    // Título de sección o pantalla
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    // Título de cada tarjeta (ej: "Viaje #101")
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    // Texto principal del contenido de las tarjetas
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    // Texto secundario (direcciones, fechas, observaciones)
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // Texto de botones y badges de estado
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)
