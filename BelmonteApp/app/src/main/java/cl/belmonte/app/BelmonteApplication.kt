package cl.belmonte.app

import android.app.Application
import cl.belmonte.app.database.AppDatabase
import cl.belmonte.app.database.DatosInicialesBelmonte
import cl.belmonte.app.repository.CobroRepository
import cl.belmonte.app.repository.UsuarioRepository
import cl.belmonte.app.repository.VehiculoRepository
import cl.belmonte.app.repository.ViajeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Clase Application global de Traslados Belmonte SpA.
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Crea una sola vez (Singleton manual) la instancia de la base de datos local Room (AppDatabase)
 * y los 4 repositorios (UsuarioRepository, VehiculoRepository, CobroRepository, ViajeRepository).
 * Además, en onCreate() asegura que los datos de prueba del script SQL estén cargados en Room.
 *
 * ¿POR QUÉ SE HACE ASÍ?
 * Tal como se ve en clases (sin usar Hilt, Dagger ni Koin), exponer los repositorios en la clase
 * Application permite que cualquier ViewModel los reciba fácilmente a través de su ViewModelProvider.Factory.
 */
class BelmonteApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Instancia única de la base de datos local Room
    val database: AppDatabase by lazy {
        AppDatabase.obtenerBaseDatos(this)
    }

    // Repositorios creados manualmente a partir de los DAOs de AppDatabase
    val usuarioRepository: UsuarioRepository by lazy {
        UsuarioRepository(
            db = database,
            usuarioDao = database.usuarioDao()
        )
    }

    val vehiculoRepository: VehiculoRepository by lazy {
        VehiculoRepository(
            vehiculoDao = database.vehiculoDao(),
            viajeDao = database.viajeDao()
        )
    }

    val cobroRepository: CobroRepository by lazy {
        CobroRepository(
            cobroDao = database.cobroDao(),
            vehiculoDao = database.vehiculoDao(),
            viajeDao = database.viajeDao()
        )
    }

    val viajeRepository: ViajeRepository by lazy {
        ViajeRepository(
            viajeDao = database.viajeDao(),
            vehiculoDao = database.vehiculoDao(),
            cobroDao = database.cobroDao(),
            usuarioDao = database.usuarioDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        // Precargar los datos de prueba del script SQL al iniciar la aplicación por primera vez
        applicationScope.launch {
            DatosInicialesBelmonte.poblarSiEstaVacia(database)
        }
    }
}
