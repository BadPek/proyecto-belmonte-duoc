package cl.belmonte.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import cl.belmonte.app.ui.theme.BelmonteAppTheme
import cl.belmonte.app.view.InicioFase1Screen

/**
 * Actividad principal (punto de entrada de interfaz gráfica) de BelmonteApp.
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Configura Jetpack Compose mediante setContent, aplica el tema corporativo BelmonteAppTheme
 * y carga la pantalla inicial (en Fase 1 muestra InicioFase1Screen; en Fase 3 cargará el NavHost).
 *
 * ¿POR QUÉ SE HACE ASÍ?
 * En aplicaciones modernas con Jetpack Compose y Navigation Compose se utiliza una sola Activity
 * (arquitectura Single-Activity) donde todas las pantallas son funciones @Composable.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BelmonteAppTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    InicioFase1Screen()
                }
            }
        }
    }
}
