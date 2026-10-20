package cl.belmonte.app

import android.app.Application

/**
 * Clase Application global de Traslados Belmonte SpA.
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Se instancia una única vez cuando se abre la aplicación en Android.
 * En la Fase 2 aquí inicializaremos la base de datos local (AppDatabase de Room) y los
 * repositorios a mano, sin usar librerías externas de inyección de dependencias (sin Hilt/Koin).
 *
 * ¿POR QUÉ SE HACE ASÍ?
 * Es la forma estándar que se enseña en clases para compartir una única instancia (Singleton)
 * de la base de datos y de los repositorios hacia los ViewModels mediante ViewModelProvider.Factory.
 */
class BelmonteApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // En la Fase 2 se inicializarán aquí database y repositorios.
    }
}
