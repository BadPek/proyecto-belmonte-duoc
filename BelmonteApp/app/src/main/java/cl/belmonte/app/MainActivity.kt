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
import cl.belmonte.app.navigation.BelmonteNavHost
import cl.belmonte.app.ui.theme.BelmonteAppTheme
import cl.belmonte.app.viewmodel.BelmonteViewModel

/**
 * Actividad principal de BelmonteApp (Arquitectura Single-Activity con Navigation Compose).
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Inicializa el tema corporativo BelmonteAppTheme, obtiene el BelmonteViewModel mediante
 * su ViewModelProvider.Factory manual y lanza el grafo de navegación BelmonteNavHost.
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
                    BelmonteNavHost(viewModel = belmonteViewModel)
                }
            }
        }
    }
}
