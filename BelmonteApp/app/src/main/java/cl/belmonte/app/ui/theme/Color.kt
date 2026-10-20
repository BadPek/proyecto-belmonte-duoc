package cl.belmonte.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta de colores oficial de Traslados Belmonte SpA.
 *
 * ¿QUÉ HACE ESTE ARCHIVO?
 * Define todas las constantes de color (en formato hexadecimal 0xFFRRGGBB) que usa la aplicación.
 *
 * ¿POR QUÉ SE HACE ASÍ?
 * Centralizar los colores en un solo archivo permite mantener coherencia visual con las maquetas
 * (tonos oscuros corporativos, azul #2563EB, azul marino #002B5B y rojo coral #E84C3D) y facilita
 * cambiar un color en vivo durante la defensa sin buscarlo pantalla por pantalla.
 */

// --- Colores principales corporativos (inspirados en maquetas_belmonte.html) ---
val AzulBelmonte = Color(0xFF2563EB)        // Azul corporativo principal (#2563EB pedido en el caso)
val AzulBelmonteClaro = Color(0xFF60A5FA)   // Variante clara para modo oscuro y bordes activos
val AzulMarinoBelmonte = Color(0xFF002B5B)  // Azul marino de la barra superior y login en la maqueta
val RojoAccionBelmonte = Color(0xFFE84C3D)  // Rojo coral para botones principales e incidencias

// --- Fondos y superficies (estilo oscuro moderno de las maquetas) ---
val FondoOscuro = Color(0xFF0F172A)         // Fondo general de pantalla en tema oscuro
val SuperficieOscura = Color(0xFF1E293B)    // Fondo de tarjetas (Cards) redondeadas en tema oscuro
val SuperficieVarianteOscura = Color(0xFF334155) // Campos de texto y contenedores secundarios

// --- Fondos y superficies (alternativa clara) ---
val FondoClaro = Color(0xFFF8F9FA)          // Fondo gris muy suave de las maquetas HTML
val SuperficieClara = Color(0xFFFFFFFF)     // Tarjetas blancas
val SuperficieVarianteClara = Color(0xFFE2E8F0)

// --- Textos ---
val TextoBlanco = Color(0xFFF8FAFC)
val TextoSecundarioOscuro = Color(0xFF94A3B8)
val TextoOscuro = Color(0xFF1E293B)
val TextoSecundarioClaro = Color(0xFF4A5568)

// --- Colores semánticos para los 5 estados del viaje y botones de navegación ---
val ColorEstadoPendiente = Color(0xFFF59E0B)   // Ámbar: solicitud esperando asignación
val ColorEstadoAsignado = Color(0xFF3B82F6)    // Azul: conductor y vehículo ya asignados
val ColorEstadoEnCurso = Color(0xFF22C55E)     // Verde: viaje actualmente en recorrido
val ColorEstadoFinalizado = Color(0xFF64748B)  // Gris pizarra: viaje completado e histórico
val ColorEstadoCancelado = Color(0xFFEF4444)   // Rojo: viaje anulado (cupo devuelto)

val ColorWaze = Color(0xFF33CCFF)              // Celeste característico del botón Waze de la maqueta
val ColorGoogleMaps = Color(0xFF38A169)        // Verde para abrir ruta en Google Maps
