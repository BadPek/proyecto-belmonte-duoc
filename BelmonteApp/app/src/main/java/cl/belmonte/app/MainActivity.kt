package cl.belmonte.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.belmonte.app.ui.theme.BelmonteAppTheme
import cl.belmonte.app.view.VerificacionRoomFase2Screen
import cl.belmonte.app.viewmodel.BelmonteViewModel

/**
 * Actividad principal de BelmonteApp.
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Obtiene la instancia de BelmonteApplication, crea el BelmonteViewModel mediante su
 * ViewModelProvider.Factory manual (sin Hilt/Koin) y muestra la pantalla activa dentro
 * del tema corporativo BelmonteAppTheme.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as BelmonteApplication

        enableEdgeToEdge()
        setContent {
            BelmonteAppTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val belmonteViewModel: BelmonteViewModel = viewModel(
                        factory = BelmonteViewModel.provideFactory(app)
                    )
                    VerificacionRoomFase2Screen(viewModel = belmonteViewModel)
                }
            }
        }
    }
}
