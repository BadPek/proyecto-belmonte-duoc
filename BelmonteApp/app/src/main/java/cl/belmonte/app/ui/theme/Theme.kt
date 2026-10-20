package cl.belmonte.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Tema visual Material Design 3 de la aplicación BelmonteApp.
 *
 * ¿QUÉ HACE ESTE ARCHIVO?
 * Configura los esquemas de color oscuro (por defecto, tal como se pide en las maquetas con
 * azul #2563EB y tarjetas oscuras redondeadas) y claro, y expone el composable BelmonteAppTheme.
 *
 * ¿POR QUÉ SE HACE ASÍ?
 * Al envolver toda la app dentro de BelmonteAppTheme en MainActivity, todos los componentes
 * de Material 3 (Button, Card, OutlinedTextField, TopAppBar, Scaffold) toman automáticamente
 * los colores corporativos de Belmonte sin tener que pintarlos uno por uno.
 */

// Esquema de colores oscuro (predeterminado según el requerimiento visual)
private val EsquemaColorOscuro = darkColorScheme(
    primary = AzulBelmonte,
    onPrimary = Color.White,
    primaryContainer = AzulMarinoBelmonte,
    onPrimaryContainer = TextoBlanco,
    secondary = RojoAccionBelmonte,
    onSecondary = Color.White,
    tertiary = AzulBelmonteClaro,
    background = FondoOscuro,
    onBackground = TextoBlanco,
    surface = SuperficieOscura,
    onSurface = TextoBlanco,
    surfaceVariant = SuperficieVarianteOscura,
    onSurfaceVariant = TextoSecundarioOscuro,
    error = ColorEstadoCancelado,
    onError = Color.White
)

// Esquema de colores claro (opcional por si se desea alternar en el futuro)
private val EsquemaColorClaro = lightColorScheme(
    primary = AzulBelmonte,
    onPrimary = Color.White,
    primaryContainer = AzulMarinoBelmonte,
    onPrimaryContainer = Color.White,
    secondary = RojoAccionBelmonte,
    onSecondary = Color.White,
    tertiary = AzulMarinoBelmonte,
    background = FondoClaro,
    onBackground = TextoOscuro,
    surface = SuperficieClara,
    onSurface = TextoOscuro,
    surfaceVariant = SuperficieVarianteClara,
    onSurfaceVariant = TextoSecundarioClaro,
    error = ColorEstadoCancelado,
    onError = Color.White
)

/**
 * Composable que aplica el tema de Belmonte SpA a toda la jerarquía de pantallas.
 *
 * @param darkTheme Por defecto es true para mantener el estilo oscuro con azul #2563EB.
 * @param content Contenido de pantallas Compose que heredarán este tema.
 */
@Composable
fun BelmonteAppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) EsquemaColorOscuro else EsquemaColorClaro

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
