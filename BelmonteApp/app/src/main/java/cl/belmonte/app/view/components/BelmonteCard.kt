package cl.belmonte.app.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.belmonte.app.ui.theme.ColorEstadoAsignado
import cl.belmonte.app.ui.theme.ColorEstadoCancelado
import cl.belmonte.app.ui.theme.ColorEstadoEnCurso
import cl.belmonte.app.ui.theme.ColorEstadoFinalizado
import cl.belmonte.app.ui.theme.ColorEstadoPendiente

/**
 * Componentes visuales reutilizables de Traslados Belmonte SpA.
 *
 * ¿QUÉ HACE ESTE ARCHIVO?
 * Provee la tarjeta corporativa redondeada (BelmonteCard) y la etiqueta de estado (BadgeEstadoViaje)
 * que se reutilizarán en las pantallas de Solicitante, Admin Central, Conductor y Admin de Empresa.
 *
 * ¿POR QUÉ SE HACE ASÍ?
 * Siguiendo las maquetas (maquetas_belmonte.html), todas las tarjetas tienen bordes redondeados y
 * muestran el estado del viaje con un color distintivo. Al tenerlo en view/components evitamos
 * repetir código de diseño en cada pantalla.
 */

@Composable
fun BelmonteCard(
    modifier: Modifier = Modifier,
    colorBorde: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, colorBorde),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = content
        )
    }
}

/**
 * Etiqueta (Badge) redondeada que muestra el estado del viaje con su color semántico.
 *
 * @param codigoEstado Código oficial del catálogo estados_viaje (PENDIENTE, ASIGNADO, EN_CURSO, FINALIZADO, CANCELADO).
 */
@Composable
fun BadgeEstadoViaje(codigoEstado: String) {
    val colorEstado = when (codigoEstado.uppercase()) {
        "PENDIENTE" -> ColorEstadoPendiente
        "ASIGNADO" -> ColorEstadoAsignado
        "EN_CURSO" -> ColorEstadoEnCurso
        "FINALIZADO" -> ColorEstadoFinalizado
        "CANCELADO" -> ColorEstadoCancelado
        else -> MaterialTheme.colorScheme.primary
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
        color = colorEstado.copy(alpha = 0.2f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, colorEstado)
    ) {
        Text(
            text = textoVisible,
            color = colorEstado,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
