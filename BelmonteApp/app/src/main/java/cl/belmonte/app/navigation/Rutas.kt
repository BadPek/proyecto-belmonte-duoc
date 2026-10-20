package cl.belmonte.app.navigation

/**
 * Definición centralizada de las rutas de navegación de Jetpack Navigation Compose.
 *
 * ¿QUÉ HACE ESTE ARCHIVO?
 * Contiene las constantes con los nombres de cada ruta de pantalla de la aplicación.
 *
 * ¿POR QUÉ SE HACE ASÍ?
 * Evita errores de tipeo al navegar con navController.navigate(...) y deja claro en un solo
 * lugar todas las pantallas que tendrá la app en la Fase 3 y Fase 4.
 */
object Rutas {
    // Pantallas comunes
    const val LOGIN = "login"
    const val INICIO_ROL = "inicio_rol"

    // Pantallas del Solicitante (ROLE_SOLICITANTE)
    const val NUEVA_SOLICITUD = "nueva_solicitud"
    const val MIS_VIAJES = "mis_viajes"
    const val DETALLE_SEGUIMIENTO = "detalle_seguimiento/{idViaje}"
    const val ESCANEAR_QR = "escanear_qr/{idViaje}"

    // Pantallas del Administrador Central Belmonte (ROLE_ADMIN)
    const val SOLICITUDES_PENDIENTES = "solicitudes_pendientes"
    const val ASIGNAR_VIAJE = "asignar_viaje/{idViaje}"
    const val REASIGNAR_VIAJE = "reasignar_viaje/{idViaje}"

    // Pantallas del Conductor (ROLE_CONDUCTOR)
    const val VIAJES_CONDUCTOR = "viajes_conductor"
    const val DETALLE_VIAJE_CONDUCTOR = "detalle_viaje_conductor/{idViaje}"
    const val REPORTAR_INCIDENCIA = "reportar_incidencia/{idViaje}"

    // Pantallas del Administrador de Empresa Cliente (ROLE_ADMIN_EMPRESA)
    const val MIS_CUPOS_EMPRESA = "mis_cupos_empresa"
    const val REPARTIR_CUPOS = "repartir_cupos"
    const val COBROS_EMPRESA = "cobros_empresa"
}
