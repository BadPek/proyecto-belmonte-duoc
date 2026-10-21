package cl.belmonte.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.belmonte.app.qr.EscanearQrScreen
import cl.belmonte.app.view.AsignarViajeScreen
import cl.belmonte.app.view.CobrosEmpresaScreen
import cl.belmonte.app.view.DetalleSeguimientoScreen
import cl.belmonte.app.view.DetalleViajeConductorScreen
import cl.belmonte.app.view.InicioRolScreen
import cl.belmonte.app.view.LoginScreen
import cl.belmonte.app.view.MisCuposEmpresaScreen
import cl.belmonte.app.view.MisViajesSolicitanteScreen
import cl.belmonte.app.view.NuevaSolicitudScreen
import cl.belmonte.app.view.ReasignarViajeScreen
import cl.belmonte.app.view.RepartirCuposScreen
import cl.belmonte.app.view.ReportarIncidenciaScreen
import cl.belmonte.app.view.SeleccionPerfilScreen
import cl.belmonte.app.view.SolicitudesPendientesAdminScreen
import cl.belmonte.app.view.ViajesConductorScreen
import cl.belmonte.app.viewmodel.BelmonteViewModel

/**
 * Grafo de navegación principal (NavHost) de Traslados Belmonte SpA.
 *
 * ¿QUÉ HACE ESTE ARCHIVO?
 * Conecta todas las rutas definidas en Rutas.kt con sus respectivas pantallas @Composable:
 * - Entrada: Selección de Perfil Inicial (quién eres) -> Login exclusivo por rol (CU-01) -> Inicio según Rol.
 * - Solicitante: Nueva Solicitud (CU-02), Mis Viajes, Seguimiento (CU-09) y Escanear QR.
 * - Admin Belmonte: Solicitudes Pendientes, Asignar (CU-03) y Reasignar (CU-10).
 * - Conductor: Viajes del Día (CU-04), Detalle con Iniciar/Finalizar y Maps/Waze (CU-05/06/07), Incidencia (CU-08).
 * - Admin Empresa: Mis Cupos, Repartir Cupos y Cobros del Mes.
 */
@Composable
fun BelmonteNavHost(
    viewModel: BelmonteViewModel,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Rutas.SELECCION_PERFIL
    ) {
        // 0. Paso 1 antes de entrar: ¿Quién eres? (Trabajador, Admin Empresa, Admin Belmonte o Conductor)
        composable(route = Rutas.SELECCION_PERFIL) {
            SeleccionPerfilScreen(
                onPerfilSeleccionado = { rolElegido ->
                    viewModel.seleccionarPerfilInicial(rolElegido)
                    navController.navigate(Rutas.LOGIN)
                }
            )
        }

        // 1. Paso 2: Pantalla de Login exclusiva del perfil elegido (CU-01)
        composable(route = Rutas.LOGIN) {
            LoginScreen(
                viewModel = viewModel,
                onBackToSeleccionPerfil = {
                    viewModel.limpiarMensaje()
                    navController.popBackStack()
                },
                onLoginSuccess = {
                    navController.navigate(Rutas.INICIO_ROL) {
                        popUpTo(Rutas.SELECCION_PERFIL) { inclusive = false }
                    }
                }
            )
        }

        // 2. Menú / Inicio según el rol autenticado (sin mezclar otros roles)
        composable(route = Rutas.INICIO_ROL) {
            InicioRolScreen(
                viewModel = viewModel,
                onNavigate = { rutaDestino ->
                    viewModel.limpiarMensaje()
                    navController.navigate(rutaDestino)
                },
                onLogout = {
                    navController.navigate(Rutas.SELECCION_PERFIL) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // --- PANTALLAS DEL SOLICITANTE (ROLE_SOLICITANTE) ---

        composable(route = Rutas.NUEVA_SOLICITUD) {
            NuevaSolicitudScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSolicitudCreada = {
                    navController.navigate(Rutas.MIS_VIAJES) {
                        popUpTo(Rutas.INICIO_ROL)
                    }
                }
            )
        }

        composable(route = Rutas.MIS_VIAJES) {
            MisViajesSolicitanteScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNuevaSolicitud = {
                    viewModel.limpiarMensaje()
                    navController.navigate(Rutas.NUEVA_SOLICITUD)
                },
                onVerDetalle = { idViaje ->
                    viewModel.limpiarMensaje()
                    navController.navigate("detalle_seguimiento/$idViaje")
                },
                onEscanearQr = { idViaje ->
                    viewModel.limpiarResultadoQr()
                    navController.navigate("escanear_qr/$idViaje")
                }
            )
        }

        composable(
            route = Rutas.DETALLE_SEGUIMIENTO,
            arguments = listOf(navArgument("idViaje") { type = NavType.LongType })
        ) { backStackEntry ->
            val idViaje = backStackEntry.arguments?.getLong("idViaje") ?: 101L
            DetalleSeguimientoScreen(
                idViaje = idViaje,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEscanearQr = { id ->
                    viewModel.limpiarResultadoQr()
                    navController.navigate("escanear_qr/$id")
                }
            )
        }

        composable(
            route = Rutas.ESCANEAR_QR,
            arguments = listOf(navArgument("idViaje") { type = NavType.LongType })
        ) { backStackEntry ->
            val idViaje = backStackEntry.arguments?.getLong("idViaje") ?: 103L
            EscanearQrScreen(
                idViaje = idViaje,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // --- PANTALLAS DEL ADMINISTRADOR CENTRAL BELMONTE (ROLE_ADMIN) ---

        composable(route = Rutas.SOLICITUDES_PENDIENTES) {
            SolicitudesPendientesAdminScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onAsignarViaje = { idViaje ->
                    viewModel.limpiarMensaje()
                    navController.navigate("asignar_viaje/$idViaje")
                },
                onReasignarViaje = { idViaje ->
                    viewModel.limpiarMensaje()
                    navController.navigate("reasignar_viaje/$idViaje")
                }
            )
        }

        composable(
            route = Rutas.ASIGNAR_VIAJE,
            arguments = listOf(navArgument("idViaje") { type = NavType.LongType })
        ) { backStackEntry ->
            val idViaje = backStackEntry.arguments?.getLong("idViaje") ?: 101L
            AsignarViajeScreen(
                idViaje = idViaje,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Rutas.REASIGNAR_VIAJE,
            arguments = listOf(navArgument("idViaje") { type = NavType.LongType })
        ) { backStackEntry ->
            val idViaje = backStackEntry.arguments?.getLong("idViaje") ?: 103L
            ReasignarViajeScreen(
                idViaje = idViaje,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // --- PANTALLAS DEL CONDUCTOR (ROLE_CONDUCTOR) ---

        composable(route = Rutas.VIAJES_CONDUCTOR) {
            ViajesConductorScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onVerDetalleViaje = { idViaje ->
                    viewModel.limpiarMensaje()
                    navController.navigate("detalle_viaje_conductor/$idViaje")
                }
            )
        }

        composable(
            route = Rutas.DETALLE_VIAJE_CONDUCTOR,
            arguments = listOf(navArgument("idViaje") { type = NavType.LongType })
        ) { backStackEntry ->
            val idViaje = backStackEntry.arguments?.getLong("idViaje") ?: 102L
            DetalleViajeConductorScreen(
                idViaje = idViaje,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onReportarIncidencia = { id ->
                    viewModel.limpiarMensaje()
                    navController.navigate("reportar_incidencia/$id")
                }
            )
        }

        composable(
            route = Rutas.REPORTAR_INCIDENCIA,
            arguments = listOf(navArgument("idViaje") { type = NavType.LongType })
        ) { backStackEntry ->
            val idViaje = backStackEntry.arguments?.getLong("idViaje") ?: 102L
            ReportarIncidenciaScreen(
                idViaje = idViaje,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // --- PANTALLAS DEL ADMINISTRADOR DE EMPRESA CLIENTE (ROLE_ADMIN_EMPRESA) ---

        composable(route = Rutas.MIS_CUPOS_EMPRESA) {
            MisCuposEmpresaScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onIrARepartir = {
                    viewModel.limpiarMensaje()
                    navController.navigate(Rutas.REPARTIR_CUPOS)
                }
            )
        }

        composable(route = Rutas.REPARTIR_CUPOS) {
            RepartirCuposScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Rutas.COBROS_EMPRESA) {
            CobrosEmpresaScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
