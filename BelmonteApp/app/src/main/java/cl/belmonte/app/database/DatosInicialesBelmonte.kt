package cl.belmonte.app.database

import cl.belmonte.app.model.AsignacionCupoEntity
import cl.belmonte.app.model.CobroCupoEntity
import cl.belmonte.app.model.CobroEntity
import cl.belmonte.app.model.ConductorEntity
import cl.belmonte.app.model.EmpresaEntity
import cl.belmonte.app.model.EstadoViajeEntity
import cl.belmonte.app.model.HistorialEstadoEntity
import cl.belmonte.app.model.HistorialReasignacionEntity
import cl.belmonte.app.model.IncidenciaEntity
import cl.belmonte.app.model.TipoVehiculoEntity
import cl.belmonte.app.model.UsuarioEntity
import cl.belmonte.app.model.VehiculoEntity
import cl.belmonte.app.model.ViajeEntity

/**
 * Carga inicial de datos de prueba en la base de datos local Room.
 *
 * ¿QUÉ HACE ESTE OBJETO?
 * Inserta exactamente los mismos datos ficticios de prueba definidos en script_belmonte_db_v3.sql:
 * - 2 empresas cliente (Minera Andes Sur SpA y Consultora Pacífico Austral Ltda.)
 * - 9 usuarios de los 4 roles (clave de prueba de todos: "Password123*")
 * - 4 conductores (IDs 6, 7, 8 y 9)
 * - 3 tipos de vehículo (AUTO, VAN, CAMION) y 4 vehículos (KR-TW-21, LK-JH-44, PX-RT-89, JT-KL-52)
 * - 5 estados de viaje y los viajes 101 al 107 con su historial de estados, incidencias y reasignación
 * - 2 paquetes mensuales de octubre ("2026-10") pagados, sus cupos por tipo y las 7 asignaciones a trabajadores
 *
 * ¿POR QUÉ SE HACE ASÍ?
 * Permite que al abrir la aplicación en el emulador todas las pantallas de los 4 roles tengan
 * información realista de inmediato sin depender de conexión al backend en esta evaluación.
 */
object DatosInicialesBelmonte {

    const val CLAVE_PRUEBA = "Password123*"
    const val PERIODO_ACTUAL = "2026-10"

    /**
     * Verifica si la tabla de usuarios está vacía; si lo está, inserta todos los datos del script SQL.
     */
    suspend fun poblarSiEstaVacia(db: AppDatabase) {
        if (db.usuarioDao().contarUsuarios() > 0) {
            return
        }

        // 1. Empresas cliente
        val empresas = listOf(
            EmpresaEntity(
                idEmpresa = 1,
                rutEmpresa = "76482913-1",
                razonSocial = "Minera Andes Sur SpA",
                giro = "Servicios mineros",
                direccion = "Av. Apoquindo 4501, Las Condes",
                telefono = "+56200000001",
                emailContacto = "contacto@andessur.test",
                activa = true
            ),
            EmpresaEntity(
                idEmpresa = 2,
                rutEmpresa = "77215604-9",
                razonSocial = "Consultora Pacífico Austral Ltda.",
                giro = "Asesorías de gestión",
                direccion = "Av. Providencia 2133, Providencia",
                telefono = "+56200000002",
                emailContacto = "contacto@pacificoaustral.test",
                activa = true
            )
        )
        db.usuarioDao().insertarEmpresas(empresas)

        // 2. Usuarios (9 usuarios de prueba. En local comparamos "Password123*"; en el backend se usa BCrypt)
        val usuarios = listOf(
            UsuarioEntity(
                idUsuario = 1,
                idRol = 1,
                rol = "ROLE_ADMIN",
                idEmpresa = null,
                rut = "12845371-7",
                nombres = "Valeria",
                apellidos = "Fuentes Rojas",
                email = "valeria.admin@belmonte.test",
                passwordHash = CLAVE_PRUEBA,
                telefono = "+56900000001"
            ),
            UsuarioEntity(
                idUsuario = 2,
                idRol = 2,
                rol = "ROLE_ADMIN_EMPRESA",
                idEmpresa = 1,
                rut = "15392847-9",
                nombres = "Tomás",
                apellidos = "Herrera Lagos",
                email = "tomas.herrera@andessur.test",
                passwordHash = CLAVE_PRUEBA,
                telefono = "+56900000002"
            ),
            UsuarioEntity(
                idUsuario = 3,
                idRol = 3,
                rol = "ROLE_SOLICITANTE",
                idEmpresa = 1,
                rut = "16473920-1",
                nombres = "Daniela",
                apellidos = "Muñoz Pino",
                email = "daniela.munoz@andessur.test",
                passwordHash = CLAVE_PRUEBA,
                telefono = "+56900000003"
            ),
            UsuarioEntity(
                idUsuario = 4,
                idRol = 2,
                rol = "ROLE_ADMIN_EMPRESA",
                idEmpresa = 2,
                rut = "17284561-4",
                nombres = "Ignacio",
                apellidos = "Vera Salinas",
                email = "ignacio.vera@pacificoaustral.test",
                passwordHash = CLAVE_PRUEBA,
                telefono = "+56900000004"
            ),
            UsuarioEntity(
                idUsuario = 5,
                idRol = 3,
                rol = "ROLE_SOLICITANTE",
                idEmpresa = 2,
                rut = "18395026-6",
                nombres = "Fernanda",
                apellidos = "Castro Ríos",
                email = "fernanda.castro@pacificoaustral.test",
                passwordHash = CLAVE_PRUEBA,
                telefono = "+56900000005"
            ),
            UsuarioEntity(
                idUsuario = 6,
                idRol = 4,
                rol = "ROLE_CONDUCTOR",
                idEmpresa = null,
                rut = "13582947-1",
                nombres = "Miguel",
                apellidos = "Rojas Contreras",
                email = "miguel.rojas@belmonte.test",
                passwordHash = CLAVE_PRUEBA,
                telefono = "+56900000006"
            ),
            UsuarioEntity(
                idUsuario = 7,
                idRol = 4,
                rol = "ROLE_CONDUCTOR",
                idEmpresa = null,
                rut = "14627385-8",
                nombres = "Pedro",
                apellidos = "Soto Morales",
                email = "pedro.soto@belmonte.test",
                passwordHash = CLAVE_PRUEBA,
                telefono = "+56900000007"
            ),
            UsuarioEntity(
                idUsuario = 8,
                idRol = 4,
                rol = "ROLE_CONDUCTOR",
                idEmpresa = null,
                rut = "19473826-9",
                nombres = "Jorge",
                apellidos = "Alarcón Bravo",
                email = "jorge.alarcon@belmonte.test",
                passwordHash = CLAVE_PRUEBA,
                telefono = "+56900000008"
            ),
            UsuarioEntity(
                idUsuario = 9,
                idRol = 4,
                rol = "ROLE_CONDUCTOR",
                idEmpresa = null,
                rut = "15836204-K",
                nombres = "Luis",
                apellidos = "Carrasco Vega",
                email = "luis.carrasco@belmonte.test",
                passwordHash = CLAVE_PRUEBA,
                telefono = "+56900000009"
            )
        )
        db.usuarioDao().insertarUsuarios(usuarios)

        // 3. Conductores (1:1 con los usuarios 6, 7, 8 y 9)
        val conductores = listOf(
            ConductorEntity(6, "A2", "2028-03-15", "conductores/6.jpg", disponible = false),
            ConductorEntity(7, "A2", "2027-11-30", "conductores/7.jpg", disponible = false),
            ConductorEntity(8, "A3", "2029-06-01", "conductores/8.jpg", disponible = true),
            ConductorEntity(9, "A4", "2028-08-20", "conductores/9.jpg", disponible = true)
        )
        db.usuarioDao().insertarConductores(conductores)

        // 4. Tipos de vehículo
        val tiposVehiculo = listOf(
            TipoVehiculoEntity(
                idTipoVehiculo = 1,
                codigo = "AUTO",
                nombre = "Auto ejecutivo",
                descripcion = "Traslado de personas, hasta 4 pasajeros",
                kmIncluidos = 40,
                valorKmExtra = 1100
            ),
            TipoVehiculoEntity(
                idTipoVehiculo = 2,
                codigo = "VAN",
                nombre = "Van",
                descripcion = "Traslado de grupos, hasta 8 pasajeros",
                kmIncluidos = 40,
                valorKmExtra = 1600
            ),
            TipoVehiculoEntity(
                idTipoVehiculo = 3,
                codigo = "CAMION",
                nombre = "Camión",
                descripcion = "Traslado de materiales y carga",
                kmIncluidos = 60,
                valorKmExtra = 2500
            )
        )
        db.vehiculoDao().insertarTiposVehiculo(tiposVehiculo)

        // 5. Vehículos de la flota
        val vehiculos = listOf(
            VehiculoEntity(1, 1, "KR-TW-21", "Toyota", "Camry", 2023, "Gris plata", 4),
            VehiculoEntity(2, 1, "LK-JH-44", "Hyundai", "Santa Fe", 2023, "Gris grafito", 4),
            VehiculoEntity(3, 2, "PX-RT-89", "Mercedes-Benz", "Vito Tourer", 2024, "Negro", 8),
            VehiculoEntity(4, 3, "JT-KL-52", "Hyundai", "HD65", 2022, "Blanco", 2)
        )
        db.vehiculoDao().insertarVehiculos(vehiculos)

        // 6. Estados del viaje
        val estados = listOf(
            EstadoViajeEntity(1, "PENDIENTE", "Pendiente", "Solicitud creada, sin conductor asignado", 1),
            EstadoViajeEntity(2, "ASIGNADO", "Asignado", "Conductor y vehículo asignados por el admin", 2),
            EstadoViajeEntity(3, "EN_CURSO", "En curso", "El conductor inició el viaje", 3),
            EstadoViajeEntity(4, "FINALIZADO", "Finalizado", "El conductor terminó el viaje", 4),
            EstadoViajeEntity(5, "CANCELADO", "Cancelado", "Solicitud anulada antes de iniciar el viaje", 5)
        )
        db.viajeDao().insertarEstadosViaje(estados)

        // 7. Viajes 101 al 107 (exactamente los del script SQL)
        val viajes = listOf(
            ViajeEntity(
                idViaje = 101,
                idSolicitante = 3,
                idEmpresa = 1,
                idTipoVehiculo = 1,
                idConductor = null,
                idVehiculo = null,
                idEstado = 1,
                nombrePasajero = "Andrea Lagos Vidal",
                telefonoPasajero = "+56900000101",
                cantidadPasajeros = 1,
                descripcionCarga = null,
                fechaServicio = "2026-10-15",
                horaServicio = "07:30:00",
                direccionOrigen = "Isidora Goyenechea 3000, Las Condes",
                latitudOrigen = -33.4149000,
                longitudOrigen = -70.5986000,
                direccionDestino = "Aeropuerto AMB, Pudahuel",
                latitudDestino = -33.3930000,
                longitudDestino = -70.7944000,
                numeroVuelo = "LA452",
                observaciones = "Equipaje grande",
                distanciaKm = 18.34,
                kmExtra = 0.0,
                cargoExtra = 0,
                creadoEn = "2026-10-09 18:20:00"
            ),
            ViajeEntity(
                idViaje = 102,
                idSolicitante = 5,
                idEmpresa = 2,
                idTipoVehiculo = 1,
                idConductor = 6,
                idVehiculo = 1,
                idEstado = 2,
                nombrePasajero = "Rodrigo Paredes Núñez",
                telefonoPasajero = "+56900000102",
                cantidadPasajeros = 1,
                descripcionCarga = null,
                fechaServicio = "2026-10-15",
                horaServicio = "09:00:00",
                direccionOrigen = "Av. Apoquindo 4501, Las Condes",
                latitudOrigen = -33.4116000,
                longitudOrigen = -70.5843000,
                direccionDestino = "Av. Vitacura 2939, Las Condes",
                latitudDestino = -33.4140000,
                longitudDestino = -70.6020000,
                numeroVuelo = null,
                observaciones = null,
                distanciaKm = 1.66,
                kmExtra = 0.0,
                cargoExtra = 0,
                creadoEn = "2026-10-09 19:10:00"
            ),
            ViajeEntity(
                idViaje = 103,
                idSolicitante = 3,
                idEmpresa = 1,
                idTipoVehiculo = 1,
                idConductor = 7,
                idVehiculo = 2,
                idEstado = 3,
                nombrePasajero = "Claudia Ortiz Medina",
                telefonoPasajero = "+56900000103",
                cantidadPasajeros = 2,
                descripcionCarga = null,
                fechaServicio = "2026-10-10",
                horaServicio = "10:15:00",
                direccionOrigen = "Aeropuerto AMB, Terminal Internacional",
                latitudOrigen = -33.3930000,
                longitudOrigen = -70.7944000,
                direccionDestino = "Av. Kennedy 9001, Las Condes",
                latitudDestino = -33.3920000,
                longitudDestino = -70.5460000,
                numeroVuelo = "H2502",
                observaciones = null,
                distanciaKm = 23.06,
                kmExtra = 0.0,
                cargoExtra = 0,
                creadoEn = "2026-10-09 12:00:00"
            ),
            ViajeEntity(
                idViaje = 104,
                idSolicitante = 5,
                idEmpresa = 2,
                idTipoVehiculo = 1,
                idConductor = 6,
                idVehiculo = 1,
                idEstado = 4,
                nombrePasajero = "Sebastián Fuentes Toro",
                telefonoPasajero = "+56900000104",
                cantidadPasajeros = 1,
                descripcionCarga = null,
                fechaServicio = "2026-10-08",
                horaServicio = "15:00:00",
                direccionOrigen = "Av. Libertador B. O'Higgins 1302, Santiago",
                latitudOrigen = -33.4440000,
                longitudOrigen = -70.6530000,
                direccionDestino = "Av. Providencia 2133, Providencia",
                latitudDestino = -33.4230000,
                longitudDestino = -70.6100000,
                numeroVuelo = null,
                observaciones = null,
                distanciaKm = 4.62,
                kmExtra = 0.0,
                cargoExtra = 0,
                creadoEn = "2026-10-07 11:30:00"
            ),
            ViajeEntity(
                idViaje = 105,
                idSolicitante = 3,
                idEmpresa = 1,
                idTipoVehiculo = 2,
                idConductor = 8,
                idVehiculo = 3,
                idEstado = 4,
                nombrePasajero = "Equipo Proyecto Norte",
                telefonoPasajero = "+56900000105",
                cantidadPasajeros = 5,
                descripcionCarga = null,
                fechaServicio = "2026-10-09",
                horaServicio = "18:00:00",
                direccionOrigen = "Av. Apoquindo 4501, Las Condes",
                latitudOrigen = -33.4116000,
                longitudOrigen = -70.5843000,
                direccionDestino = "Aeropuerto AMB, Pudahuel",
                latitudDestino = -33.3930000,
                longitudDestino = -70.7944000,
                numeroVuelo = "JA120",
                observaciones = "Grupo de 5 personas",
                distanciaKm = 19.61,
                kmExtra = 0.0,
                cargoExtra = 0,
                creadoEn = "2026-10-08 10:00:00"
            ),
            ViajeEntity(
                idViaje = 106,
                idSolicitante = 5,
                idEmpresa = 2,
                idTipoVehiculo = 1,
                idConductor = null,
                idVehiculo = null,
                idEstado = 5,
                nombrePasajero = "Rodrigo Paredes Núñez",
                telefonoPasajero = "+56900000102",
                cantidadPasajeros = 1,
                descripcionCarga = null,
                fechaServicio = "2026-10-12",
                horaServicio = "08:00:00",
                direccionOrigen = "Av. Providencia 2133, Providencia",
                latitudOrigen = -33.4230000,
                longitudOrigen = -70.6100000,
                direccionDestino = "Av. Libertador B. O'Higgins 3850, Estación Central",
                latitudDestino = -33.4540000,
                longitudDestino = -70.6880000,
                numeroVuelo = null,
                observaciones = null,
                distanciaKm = 8.02,
                kmExtra = 0.0,
                cargoExtra = 0,
                creadoEn = "2026-10-09 09:00:00"
            ),
            ViajeEntity(
                idViaje = 107,
                idSolicitante = 3,
                idEmpresa = 1,
                idTipoVehiculo = 3,
                idConductor = 9,
                idVehiculo = 4,
                idEstado = 4,
                nombrePasajero = "Recibe: Marcos Díaz Fuentes",
                telefonoPasajero = "+56900000107",
                cantidadPasajeros = 0,
                descripcionCarga = "Repuestos y herramientas para faena (800 kg)",
                fechaServicio = "2026-10-06",
                horaServicio = "08:00:00",
                direccionOrigen = "Av. Pajaritos 4500, Maipú",
                latitudOrigen = -33.4850000,
                longitudOrigen = -70.7550000,
                direccionDestino = "Av. Libertador B. O'Higgins 500, Rancagua",
                latitudDestino = -34.1700000,
                longitudDestino = -70.7400000,
                numeroVuelo = null,
                observaciones = "Cargar en bodega 3",
                distanciaKm = 76.18,
                kmExtra = 16.18,
                cargoExtra = 40450,
                creadoEn = "2026-10-04 10:00:00"
            )
        )
        db.viajeDao().insertarViajes(viajes)

        // 8. Historial de estados de cada viaje
        val historiales = listOf(
            HistorialEstadoEntity(1, 101, 1, 3, "2026-10-09 18:20:00", "Solicitud creada desde la app"),
            HistorialEstadoEntity(2, 102, 1, 5, "2026-10-09 19:10:00", "Solicitud creada desde la app"),
            HistorialEstadoEntity(3, 102, 2, 1, "2026-10-09 20:00:00", "Asignado a Miguel Rojas / KR-TW-21"),
            HistorialEstadoEntity(4, 103, 1, 3, "2026-10-09 12:00:00", "Solicitud creada desde la app"),
            HistorialEstadoEntity(5, 103, 2, 1, "2026-10-09 13:00:00", "Asignado a Jorge Alarcón / KR-TW-21"),
            HistorialEstadoEntity(6, 103, 3, 7, "2026-10-10 10:20:00", "Viaje iniciado por el conductor de reemplazo"),
            HistorialEstadoEntity(7, 104, 1, 5, "2026-10-07 11:30:00", "Solicitud creada desde la app"),
            HistorialEstadoEntity(8, 104, 2, 1, "2026-10-07 12:00:00", "Asignado a Miguel Rojas / KR-TW-21"),
            HistorialEstadoEntity(9, 104, 3, 6, "2026-10-08 15:05:00", null),
            HistorialEstadoEntity(10, 104, 4, 6, "2026-10-08 15:42:00", null),
            HistorialEstadoEntity(11, 105, 1, 3, "2026-10-08 10:00:00", "Solicitud creada desde la app"),
            HistorialEstadoEntity(12, 105, 2, 1, "2026-10-08 11:00:00", "Asignado a Jorge Alarcón / PX-RT-89"),
            HistorialEstadoEntity(13, 105, 3, 8, "2026-10-09 18:03:00", null),
            HistorialEstadoEntity(14, 105, 4, 8, "2026-10-09 19:01:00", null),
            HistorialEstadoEntity(15, 106, 1, 5, "2026-10-09 09:00:00", "Solicitud creada desde la app"),
            HistorialEstadoEntity(16, 106, 5, 5, "2026-10-09 16:30:00", "Reunión suspendida por el cliente"),
            HistorialEstadoEntity(17, 107, 1, 3, "2026-10-04 10:00:00", "Solicitud creada desde la app"),
            HistorialEstadoEntity(18, 107, 2, 1, "2026-10-04 15:00:00", "Asignado a Luis Carrasco / JT-KL-52"),
            HistorialEstadoEntity(19, 107, 3, 9, "2026-10-06 08:05:00", null),
            HistorialEstadoEntity(20, 107, 4, 9, "2026-10-06 10:10:00", null)
        )
        db.viajeDao().insertarHistorialesEstado(historiales)

        // 9. Incidencias en ruta
        val incidencias = listOf(
            IncidenciaEntity(
                idIncidencia = 1,
                idViaje = 103,
                idConductor = 8,
                tipoIncidencia = "PANNE_MECANICA",
                descripcion = "Falla de batería camino al aeropuerto; el vehículo no enciende.",
                fotoUrl = "incidencias/1.jpg",
                latitud = -33.4010000,
                longitud = -70.7300000,
                fechaRegistro = "2026-10-10 08:30:00"
            ),
            IncidenciaEntity(
                idIncidencia = 2,
                idViaje = 105,
                idConductor = 8,
                tipoIncidencia = "CONGESTION_CRITICA",
                descripcion = "Accidente en Costanera Norte; retraso estimado de 20 minutos.",
                fotoUrl = null,
                latitud = -33.4105000,
                longitud = -70.6150000,
                fechaRegistro = "2026-10-09 18:25:00"
            )
        )
        db.viajeDao().insertarIncidencias(incidencias)

        // 10. Historial de reasignación originada por la incidencia #1
        val reasignaciones = listOf(
            HistorialReasignacionEntity(
                idReasignacion = 1,
                idViaje = 103,
                idConductorAnterior = 8,
                idConductorNuevo = 7,
                idAdmin = 1,
                motivo = "Panne mecánica informada por el conductor (incidencia #1). Se cambia también el vehículo por otro auto: LK-JH-44.",
                fechaReasignacion = "2026-10-10 08:40:00"
            )
        )
        db.viajeDao().insertarReasignaciones(reasignaciones)

        // 11. Cobros mensuales (paquetes pagados de octubre 2026-10)
        val cobros = listOf(
            CobroEntity(
                idCobro = 1,
                idEmpresa = 1,
                periodo = "2026-10",
                tipoCobro = "PAQUETE",
                monto = 5000000,
                estadoPago = "PAGADO",
                fechaEmision = "2026-10-01",
                fechaPago = "2026-10-02"
            ),
            CobroEntity(
                idCobro = 2,
                idEmpresa = 2,
                periodo = "2026-10",
                tipoCobro = "PAQUETE",
                monto = 1800000,
                estadoPago = "PAGADO",
                fechaEmision = "2026-10-01",
                fechaPago = "2026-10-03"
            )
        )
        db.cobroDao().insertarCobros(cobros)

        // 12. Detalle de cupos contratados en cada paquete
        val cobroCupos = listOf(
            CobroCupoEntity(1, 1, 1, 30), // Minera Andes Sur: 30 viajes en auto
            CobroCupoEntity(2, 1, 2, 20), // Minera Andes Sur: 20 viajes en van
            CobroCupoEntity(3, 1, 3, 10), // Minera Andes Sur: 10 viajes en camión
            CobroCupoEntity(4, 2, 1, 15), // Consultora Pacífico: 15 viajes en auto
            CobroCupoEntity(5, 2, 2, 5)   // Consultora Pacífico:  5 viajes en van
        )
        db.cobroDao().insertarCobroCupos(cobroCupos)

        // 13. Asignaciones de cupos a trabajadores
        val asignaciones = listOf(
            AsignacionCupoEntity(1, 1, 3, 10, "2026-10-02 09:00:00"), // Daniela: 10 en auto
            AsignacionCupoEntity(2, 2, 3, 5, "2026-10-02 09:00:00"),  // Daniela:  5 en van
            AsignacionCupoEntity(3, 3, 3, 3, "2026-10-02 09:00:00"),  // Daniela:  3 en camión
            AsignacionCupoEntity(4, 1, 2, 5, "2026-10-02 09:05:00"),  // Tomás:    5 en auto
            AsignacionCupoEntity(5, 4, 5, 8, "2026-10-03 10:00:00"),  // Fernanda: 8 en auto
            AsignacionCupoEntity(6, 5, 5, 2, "2026-10-03 10:00:00"),  // Fernanda: 2 en van
            AsignacionCupoEntity(7, 4, 4, 3, "2026-10-03 10:05:00")   // Ignacio:  3 en auto
        )
        db.cobroDao().insertarAsignacionesCupo(asignaciones)
    }
}
