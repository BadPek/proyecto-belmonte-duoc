# Guía de Defensa en Vivo — Proyecto Traslados Belmonte SpA (DSY1105)

**Integrantes:** Andrés Osorio · Joaquín Henríquez  
**Ubicación del proyecto Android:** `C:\dev\belmonte\BelmonteApp`  
**Ubicación de microservicios Backend:** `C:\dev\belmonte\belmonte-backend`

> [!IMPORTANT]
> **Resumen de las reglas oficiales del profesor para la defensa (Miércoles 28 de Octubre):**
> 1. **Sin apuntes físicos ni digitales y sin IA** durante la presentación, preguntas y programación en vivo.
> 2. **Etapa 1 (Video de máx. 7 min sin audio):** Mientras corre el video, **ambos integrantes** explican en vivo los paquetes, responsabilidades, componentes Compose, dependencias, navegación, Room y lector QR.
> 3. **Etapa 2 (Backend >= 75%):** Se entrega el código fuente y el profesor lo revisa, pero **NO se presenta en vivo ni se hacen preguntas de Backend**.
> 4. **Etapa 3 (3 preguntas técnicas individuales a cada uno):** Exclusivamente sobre **Android y Kotlin** (Room, ViewModel, StateFlow, Compose, Navigation, CameraX/QR).
> 5. **Etapa 4 (Programación en vivo):** Ubicar en qué archivo/clase/función está una funcionalidad, explicarla y **modificarla en vivo** en Android Studio.

---

## Sugerencia de Reparto Equilibrado entre los 2 Integrantes (para el Video y Defensa)

* **Integrante 1 (Andrés Osorio):**
  * **Arquitectura y Base de Datos Local (`model`, `dao`, `database`, `repository`):** Explica cómo las 13 entidades `@Entity` reflejan el modelo SQL v3, cómo funcionan los 4 `@Dao` con KSP, el patrón Singleton de `AppDatabase`, la precarga en `DatosInicialesBelmonte` y las reglas de negocio en `ViajeRepository` / `CobroRepository` (fórmula de Haversine, consumo y devolución de cupos RN-07).
  * **Roles operativos:** Explica el flujo del **Solicitante** (pedir viaje, ver seguimiento) y del **Administrador de Empresa** (ver cupos contratados/repartidos/usados y repartir cupos).
* **Integrante 2 (Joaquín Henríquez):**
  * **Interfaz Compose, Estado y Recursos Nativos (`ui.theme`, `view`, `viewmodel`, `navigation`, `qr`):** Explica `BelmonteApplication` (inyección manual sin Hilt), `BelmonteViewModel` con `StateFlow` y `ViewModelProvider.Factory`, el tema Material 3 oscuro (`#2563EB`), el `NavHost` y el lector de códigos QR con CameraX + ML Kit.
  * **Roles de terreno y despacho:** Explica el flujo del **Admin Central Belmonte** (asignar y reasignar chofer/vehículo) y del **Conductor** (iniciar/finalizar viaje, abrir ruta con `Intent` a Google Maps/Waze y reportar incidencias).

---

## FASE 1: Proyecto Android + Dependencias + Paquetes + Tema Material 3

### 1. Archivos de Configuración y Tema (`C:\dev\belmonte\BelmonteApp`)
| Archivo | ¿Qué hace? | ¿Por qué está ahí? |
|---|---|---|
| `gradle/libs.versions.toml` | Catálogo centralizado de versiones (`Room 2.8.5`, `KSP 2.3.11`, `Kotlin 2.3.20`, `Navigation Compose 2.9.8`, `Lifecycle 2.10.0`, `CameraX 1.5.3`, `ML Kit Barcode 17.3.0`). | Permite administrar todas las librerías en un solo archivo TOML. |
| `app/build.gradle.kts` | Configura `namespace = "cl.belmonte.app"`, `minSdk = 26`, `compileSdk = 36`, activa Compose y KSP (`ksp(libs.androidx.room.compiler)`). | Declara las dependencias del módulo `:app` sin usar `room-ktx` (porque Room 2.8.5 ya trae coroutines y `Flow` integrados en `room-runtime`). |
| `ui/theme/Color.kt`, `Type.kt`, `Theme.kt` | Define la paleta de colores de las maquetas (`AzulBelmonte = 0xFF2563EB`, `AzulMarinoBelmonte = 0xFF002B5B`, `RojoAccionBelmonte = 0xFFE84C3D`, `FondoOscuro = 0xFF0F172A`) y los colores de los 5 estados del viaje. | Aplica Material Design 3 de forma consistente en todas las pantallas. |
| `view/components/BelmonteCard.kt` | Contiene `BelmonteCard` (tarjeta redondeada corporativa) y `BadgeEstadoViaje` (etiqueta coloreada según el estado del viaje). | Componentes reutilizados en todas las pantallas de la app. |

---

## FASE 2: Base de Datos Local Room Completa (Entidades, DAOs, AppDatabase, Repositorios y Datos de Prueba)

### 1. ¿Qué archivos se crearon en la Fase 2 y para qué sirve cada uno?

#### Paquete `model` (13 Entidades `@Entity` + Modelos Auxiliares)
| Archivo | Tabla SQLite (`@Entity`) | ¿Para qué sirve? |
|---|---|---|
| `EmpresaEntity.kt` | `empresas` | Guarda las empresas cliente (*Minera Andes Sur SpA* y *Consultora Pacífico Austral Ltda.*). |
| `UsuarioEntity.kt` | `usuarios` | Guarda los 9 usuarios de los 4 roles (`ROLE_ADMIN`, `ROLE_ADMIN_EMPRESA`, `ROLE_SOLICITANTE`, `ROLE_CONDUCTOR`). En local compara la clave `"Password123*"`; en el backend usa BCrypt. |
| `ConductorEntity.kt` | `conductores` | Relación 1:1 con `usuarios` para los choferes (IDs 6, 7, 8 y 9): guarda clase de licencia (`A2`, `A3`, `A4`), vencimiento y disponibilidad. |
| `TipoVehiculoEntity.kt` | `tipos_vehiculo` | Catálogo de los 3 tipos (`1=AUTO`, `2=VAN`, `3=CAMION`) con sus `km_incluidos` (40, 40 y 60 km) y `valor_km_extra` ($1.100, $1.600 y $2.500). |
| `VehiculoEntity.kt` | `vehiculos` | Los 4 vehículos de la flota con sus patentes para el QR (`KR-TW-21`, `LK-JH-44`, `PX-RT-89`, `JT-KL-52`), marca, modelo y capacidad. |
| `EstadoViajeEntity.kt` | `estados_viaje` | Catálogo de los 5 estados: `1=PENDIENTE`, `2=ASIGNADO`, `3=EN_CURSO`, `4=FINALIZADO`, `5=CANCELADO`. |
| `ViajeEntity.kt` | `viajes` | **Entidad central**: guarda solicitante, empresa, tipo de vehículo, conductor, vehículo, estado, pasajero, carga, fecha/hora, direcciones y coordenadas de origen/destino, vuelo, `distancia_km` (Haversine), `km_extra` y `cargo_extra`. |
| `HistorialEstadoEntity.kt` | `historial_estados_viaje` | Guarda una fila por cada cambio de estado de un viaje (quién lo hizo, cuándo y observación) para cumplir **RN-03**. |
| `HistorialReasignacionEntity.kt` | `historial_reasignaciones` | Guarda los cambios de conductor realizados por el Admin Central con su motivo obligatorio (**CU-10**). |
| `IncidenciaEntity.kt` | `incidencias` | Guarda los reportes de problemas en ruta del conductor (`PANNE_MECANICA`, `CONGESTION_CRITICA`, `COLISION`, `PASAJERO_NO_SE_PRESENTA`, `OTRO`) con descripción y GPS (**CU-08**). |
| `CobroEntity.kt` | `cobros` | Guarda los cobros mensuales a cada empresa (`PAQUETE` al inicio del mes y `EXTRAS` al cierre). |
| `CobroCupoEntity.kt` | `cobro_cupos` | Detalle de cuántos viajes de cada tipo de vehículo incluye un paquete mensual contratado. |
| `AsignacionCupoEntity.kt` | `asignaciones_cupo` | Cuántos cupos de cada tipo le repartió el Admin de Empresa a cada trabajador. |
| `ModelosAuxiliares.kt` | *(Data classes de vista)* | Define `ResumenCupoTrabajador`, `ResumenCupoEmpresa`, `ConductorConUsuario` y `LugaresDePrueba` (coordenadas reales de Santiago y Rancagua). |

#### Paquete `dao` (4 Interfaces `@Dao` con `@Insert`, `@Update`, `@Delete` y `@Query`)
| Archivo | ¿Qué consultas resuelve? |
|---|---|
| `UsuarioDao.kt` | Login local (`login(identificador, password)`), consulta de usuarios por empresa, listado de empresas y conductores. |
| `VehiculoDao.kt` | Consulta de tipos de vehículo, vehículos filtrados por tipo y búsqueda de vehículo por patente (`obtenerVehiculoPorPatente`) para el lector QR. |
| `ViajeDao.kt` | Viajes por solicitante, por conductor, por empresa y por estado; conteo de viajes usados en el mes excluyendo cancelados (`id_estado != 5`); suma de `cargo_extra` de viajes finalizados; historial de estados, reasignaciones e incidencias. |
| `CobroDao.kt` | Cobros por empresa, líneas de cupo de cada paquete (`cobro_cupos`), suma de cupos repartidos (`sumarCuposRepartidosPorLinea`) y suma de cupos asignados a un trabajador en paquetes `PAGADO`. |

#### Paquete `database`
| Archivo | ¿Qué hace? |
|---|---|
| `AppDatabase.kt` | Clase `@Database` con las 13 entidades, patrón Singleton (`@Volatile private var INSTANCIA`) y `fallbackToDestructiveMigration`. |
| `DatosInicialesBelmonte.kt` | Precarga automáticamente todos los datos del script `script_belmonte_db_v3.sql` (empresas 1–2, usuarios 1–9, conductores 6–9, vehículos 1–4, viajes 101–107, 20 historiales, 2 incidencias, 1 reasignación, paquetes y cupos de octubre `2026-10`). |

#### Paquete `repository` (Lógica de negocio entre DAO y ViewModel)
| Archivo | Reglas de negocio que implementa |
|---|---|
| `UsuarioRepository.kt` | Login local, carga inicial garantizada y unión reactiva (`combine`) de `UsuarioEntity` + `ConductorEntity`. |
| `VehiculoRepository.kt` | Consulta de flota por tipo y `verificarPatenteQrParaViaje(idViaje, textoQr)` para confirmar si la patente del QR coincide con el vehículo asignado al viaje. |
| `CobroRepository.kt` | Calcula en tiempo real los cupos del trabajador (`Asignados - Usados = Disponibles`), valida **RN-12** al repartir cupos (no superar los contratados del paquete) y genera el cobro `EXTRAS` del mes (**RN-14**). |
| `ViajeRepository.kt` | Calcula la distancia con la **fórmula de Haversine** y el `kmExtra` / `cargoExtra` (**RN-11**); bloquea solicitudes sin cupo disponible (**RN-07**); devuelve el cupo al cancelar (**RN-07**); valida transiciones de estado (**RN-02**), tipo y capacidad del vehículo (**RN-08**) e inserta cada cambio en `historial_estados_viaje` (**RN-03**). |

#### Paquete `viewmodel` y `BelmonteApplication.kt`
* **`BelmonteApplication.kt`:** Instancia manualmente `database` y los 4 repositorios con `by lazy`.
* **`BelmonteViewModel.kt`:** Expone todos los `StateFlow` hacia las pantallas Compose e incluye `BelmonteViewModel.provideFactory(application)` para inyectar los repositorios sin Hilt/Koin.

---

### 2. Recorrido completo de una funcionalidad real en el código (5 capas)

**Ejemplo para explicar en la defensa: "Crear una Solicitud de Viaje y Consumir 1 Cupo" (CU-02 + RN-07 + RN-11)**

1. **Capa 1 — View (`view/VerificacionRoomFase2Screen.kt` o `NuevaSolicitudScreen.kt`):**  
   El usuario presiona el botón de confirmar solicitud. El `onClick` llama a:
   `viewModel.crearSolicitudViaje(idTipoVehiculo = 1, nombrePasajero = ..., direccionOrigen = ..., latitudOrigen = ..., ...)`
2. **Capa 2 — ViewModel (`viewmodel/BelmonteViewModel.kt`):**  
   La función `crearSolicitudViaje(...)` valida que los textos obligatorios no estén vacíos (y si es camión, que tenga `descripcionCarga`), abre una corrutina con `viewModelScope.launch` y llama a `viajeRepository.crearSolicitudViaje(...)`. Cuando termina, actualiza `_cuposTrabajador` y `_mensajeFeedback`.
3. **Capa 3 — Repository (`repository/ViajeRepository.kt`):**  
   * Consulta en `cobroDao.obtenerCuposAsignadosTrabajadorEnMes(...)` cuántos cupos tiene asignados el trabajador y en `viajeDao.contarViajesUsadosTrabajadorEnMes(...)` cuántos viajes no cancelados lleva en el mes.
   * Si `asignados - usados <= 0`, retorna `Result.failure(...)` (**RN-07: no permitir pedir sin cupo**).
   * Calcula `distanciaKm` con `calcularDistanciaHaversineKm(...)`, resta `tipoVehiculo.kmIncluidos` para obtener `kmExtra` y multiplica por `tipoVehiculo.valorKmExtra` para obtener `cargoExtra` (**RN-11**).
   * Llama a `viajeDao.insertarViaje(nuevoViaje)` y enseguida a `viajeDao.insertarHistorialEstado(...)` (**RN-03**).
4. **Capa 4 — DAO (`dao/ViajeDao.kt` y `dao/CobroDao.kt`):**  
   Ejecuta el `@Insert` en la tabla `viajes` y en `historial_estados_viaje`. Como `obtenerTodosLosViajes()` retorna `Flow<List<ViajeEntity>>`, Room emite automáticamente la nueva lista.
5. **Capa 5 — Database (`database/AppDatabase.kt`):**  
   Persiste los datos físicamente en el archivo SQLite `belmonte_local.db` dentro del dispositivo Android.

---

### 3. Cinco preguntas técnicas probables sobre Room y MVVM (Fase 2)

1. **Pregunta:** *¿Cómo hacen en Room para que al pedir un viaje se consuma un cupo y al cancelar el viaje el cupo se devuelva sin que se desincronicen los datos?*  
   **Respuesta:** En vez de llevar un contador suelto que pueda quedar descuadrado, aplicamos el mismo diseño del script SQL v3: en `CobroDao` consultamos los cupos asignados al trabajador en el mes (`asignaciones_cupo`), y en `ViajeDao.contarViajesUsadosTrabajadorEnMes` contamos sus viajes del mes donde `id_estado != 5` (distinto de `CANCELADO`). Así, `disponibles = asignados - usados`. Cuando se inserta un viaje en estado `PENDIENTE (1)`, `usados` sube en 1 (consume cupo); y cuando un viaje pasa a `CANCELADO (5)`, deja de contarse en `usados` y el cupo vuelve automáticamente.

2. **Pregunta:** *¿Para qué sirve la anotación `@Volatile` y el bloque `synchronized(this)` dentro de `AppDatabase.kt`?*  
   **Respuesta:** Sirven para implementar el patrón **Singleton seguro entre hilos (thread-safe)**. Abrir conexiones a una base de datos SQLite es costoso; `@Volatile` asegura que cualquier hilo vea inmediatamente si la variable `INSTANCIA` ya fue creada, y `synchronized(this)` evita que dos corrutinas creen dos bases de datos al mismo tiempo al iniciar la app.

3. **Pregunta:** *¿Cuál es la diferencia en un `@Dao` entre una función que lleva `suspend` y otra que devuelve `Flow<List<ViajeEntity>>` sin `suspend`?*  
   **Respuesta:** Una función `suspend` (como `insertarViaje` o `login`) ejecuta una sola operación puntual en segundo plano y termina. En cambio, una función que devuelve `Flow<List<ViajeEntity>>` abre un flujo reactivo continuo: Room observa la tabla `viajes` y cada vez que se inserta, actualiza o borra una fila, emite automáticamente la lista actualizada hacia el `StateFlow` del `ViewModel` y redibuja la pantalla en Compose.

4. **Pregunta:** *¿Cómo se calcula el cobro de kilómetros extra cuando un solicitante pide un traslado?*  
   **Respuesta:** En `ViajeRepository.calcularDistanciaHaversineKm` calculamos la distancia en línea recta entre `(latitudOrigen, longitudOrigen)` y `(latitudDestino, longitudDestino)` usando la fórmula trigonométrica de Haversine. Luego consultamos `TipoVehiculoEntity`: si la distancia supera `kmIncluidos` (40 km en auto/van o 60 km en camión), la diferencia es `kmExtra` y se multiplica por `valorKmExtra` para guardar `cargoExtra`.

5. **Pregunta:** *¿Por qué en `BelmonteViewModel` tienen una variable privada `_todosLosViajes` o `_mensajeFeedback` (`MutableStateFlow`) y otra pública `mensajeFeedback` (`StateFlow`)?*  
   **Respuesta:** Por **encapsulamiento de estado** en la arquitectura MVVM. Solo el `ViewModel` tiene permiso para modificar el valor usando el `MutableStateFlow` privado (`_mensajeFeedback.value = ...`), mientras que las pantallas Compose solo pueden leer y observar el `StateFlow` público inmutable con `collectAsState()`.

---

### 4. Dos ejercicios de "Modifica esto en vivo" para practicar (Fase 2 — Room)

#### Ejercicio práctico 1: Agregar una nueva columna a una tabla `@Entity` de Room en vivo
* **Lo que puede pedir el profesor:** *"Agreguen un campo nuevo a la tabla de vehículos o de viajes (por ejemplo, `kilometrajeActual: Int` en `VehiculoEntity` o `centroCosto: String` en `ViajeEntity`) y muéstrenlo en pantalla".*
* **Pasos exactos (toma 1 minuto):**
  1. Abrir `model/VehiculoEntity.kt` y agregar la propiedad con valor por defecto para no romper llamadas existentes:
     ```kotlin
     @ColumnInfo(name = "kilometraje_actual")
     val kilometrajeActual: Int = 15000
     ```
  2. Abrir `database/AppDatabase.kt` y cambiar `version = 1` a **`version = 2`**. (Como ya configuramos `.fallbackToDestructiveMigration(dropAllTables = true)` y la verificación `poblarSiEstaVacia` en `BelmonteApplication`, Room recreará la tabla con la columna nueva y recargará los datos automáticamente sin caerse).
  3. Mostrar el dato en la tarjeta de vehículo en `view/`.

#### Ejercicio práctico 2: Crear una nueva consulta `@Query` en el DAO y llamarla desde el Repository
* **Lo que puede pedir el profesor:** *"Agreguen en `ViajeDao` una consulta SQL que devuelva solo los viajes que tienen kilómetros extra (`km_extra > 0`)".*
* **Pasos exactos:**
  1. Abrir `dao/ViajeDao.kt` y agregar:
     ```kotlin
     @Query("SELECT * FROM viajes WHERE km_extra > 0 ORDER BY cargo_extra DESC")
     fun obtenerViajesConKmExtra(): Flow<List<ViajeEntity>>
     ```
  2. Abrir `repository/ViajeRepository.kt` y exponerla:
     ```kotlin
     fun obtenerViajesConKmExtra(): Flow<List<ViajeEntity>> =
         viajeDao.obtenerViajesConKmExtra()
     ```
  3. Con eso, en los datos de prueba aparecerá inmediatamente el **Viaje #107** (camión a Rancagua con `16.18 km extra` y `$40.450`).

---

## Fase 3: Navegación con Navigation Compose y 100% de Pantallas por Rol

### 1. Tabla de archivos de la Fase 3 y qué hace cada uno

| Archivo | Paquete / Ruta | ¿Qué hace y por qué existe? |
| :--- | :--- | :--- |
| `Rutas.kt` | `cl.belmonte.app.navigation` | Objeto singleton `Rutas` que centraliza todas las constantes de ruta del `NavHost` (`login`, `inicio`, `solicitante/nueva_solicitud`, `conductor/detalle_viaje/{viajeId}`, etc.) y funciones helper como `Rutas.detalleViajeConductor(101)` para construir rutas con argumentos sin errores de tipeo. |
| `BelmonteNavHost.kt` | `cl.belmonte.app.navigation` | Contiene el `NavHost` principal de Jetpack Compose (`BelmonteNavHost`) que conecta las 13 pantallas de la aplicación y extrae los argumentos dinámicos (`viajeId` con `NavType.IntType`) del `backStackEntry`. |
| `LoginScreen.kt` | `cl.belmonte.app.view` | **CU-01 Iniciar Sesión:** Formulario de correo y contraseña conectado a Room (`UsuarioDao.login`) + 4 botones de **Acceso Rápido Demo (1 clic)** para cambiar entre los 4 roles al instante durante el video o la defensa en vivo. |
| `InicioRolScreen.kt` | `cl.belmonte.app.view` | Menú principal dinámico post-login que lee `usuarioActual.rol` (`ROLE_SOLICITANTE`, `ROLE_CONDUCTOR`, `ROLE_ADMIN`, `ROLE_ADMIN_EMPRESA`), muestra el resumen de indicadores en tiempo real y despliega las tarjetas de navegación exclusivas de ese rol. |
| `SolicitanteScreens.kt` | `cl.belmonte.app.view` | Contiene las 3 pantallas del **Trabajador Solicitante**: `NuevaSolicitudScreen` (**CU-02**, elige tipo de vehículo, verifica cupos disponibles, elige direcciones/coordenadas de Santiago o Rancagua y calcula km extra Haversine en vivo), `MisViajesSolicitanteScreen` (lista con botón de cancelar para `PENDIENTE`/`ASIGNADO`) y `DetalleSeguimientoScreen` (**CU-09**, bitácora completa de `HistorialEstadoEntity`, reasignaciones e incidencias). |
| `AdminBelmonteScreens.kt` | `cl.belmonte.app.view` | Contiene las 3 pantallas de la **Recepcionista / Admin Belmonte (`ROLE_ADMIN`)**: `SolicitudesPendientesAdminScreen` (bandeja de viajes pendientes y en curso), `AsignarViajeScreen` (**CU-03**, filtra vehículos del tipo solicitado y conductores con licencia vigente para despachar el viaje) y `ReasignarViajeScreen` (**CU-10**, cambia conductor/vehículo dejando auditoría en `historial_reasignaciones`). |
| `ConductorScreens.kt` | `cl.belmonte.app.view` | Contiene las 3 pantallas del **Conductor (`ROLE_CONDUCTOR`)**: `ViajesConductorScreen` (**CU-04**, filtra los viajes asignados al chofer logueado), `DetalleViajeConductorScreen` (**CU-05 Iniciar Viaje**, **CU-06 Finalizar Viaje**, **CU-07 Abrir Ruta en Google Maps / Waze** con `Intent.ACTION_VIEW` y botón para escanear QR de patente) y `ReportarIncidenciaScreen` (**CU-08**, registra incidente en ruta y opcionalmente marca `requiereReasignacion = true`). |
| `AdminEmpresaScreens.kt` | `cl.belmonte.app.view` | Contiene las 3 pantallas del **Administrador de Empresa Cliente (`ROLE_ADMIN_EMPRESA`)**: `MisCuposEmpresaScreen` (muestra el paquete contratado `2026-10` con barras de progreso de cupos contratados vs distribuidos vs consumidos), `RepartirCuposScreen` (**RN-12**, asigna o incrementa cupos a trabajadores de su empresa validando el tope contratado) y `CobrosEmpresaScreen` (**RN-14**, muestra paquetes mensuales, detalle de viajes con km extra y botón para generar el cobro `EXTRAS` de fin de mes). |
| `EscanearQrScreen.kt` | `cl.belmonte.app.qr` | Pantalla del lector de código QR de patente del vehículo asignado. Valida la patente leída contra `VehiculoDao.obtenerPorPatente` y contra el `vehiculoId` del viaje antes de iniciar el traslado (**CU-05**). |

---

### 2. Recorrido por las 5 capas de una pantalla con navegación (Ejemplo: Conductor abre el detalle del Viaje #102 e inicia el viaje)

1. **Capa `view` + `navigation` (`ViajesConductorScreen.kt` → `BelmonteNavHost.kt`):**
   - El conductor Roberto Soto toca la tarjeta del **Viaje #102**.
   - Se ejecuta `navController.navigate(Rutas.detalleViajeConductor(102))`, que navega a la ruta `"conductor/detalle_viaje/102"`.
   - En `BelmonteNavHost.kt`, el bloque `composable(route = Rutas.CONDUCTOR_DETALLE_VIAJE, arguments = listOf(navArgument("viajeId") { type = NavType.IntType }))` extrae `val viajeId = backStackEntry.arguments?.getInt("viajeId")` y abre `DetalleViajeConductorScreen(viajeId = 102)`.
2. **Capa `view` (`DetalleViajeConductorScreen.kt`):**
   - Observa `viewModel.todosLosViajes.collectAsState()` y muestra el estado actual (`ASIGNADO`), origen, destino y patente `LK-JH-44`.
   - Cuando el conductor presiona **"INICIAR VIAJE (PASAR A EN_CURSO)"**, la vista llama a `viewModel.iniciarViaje(102)`.
3. **Capa `viewmodel` (`BelmonteViewModel.kt`):**
   - `iniciarViaje(viajeId)` lanza `viewModelScope.launch` y llama a `viajeRepository.iniciarViaje(viajeId, usuarioId)`.
4. **Capa `repository` (`ViajeRepository.kt`):**
   - Verifica que el viaje esté en estado `2 (ASIGNADO)` (**RN-06**), actualiza el estado a `3 (EN_CURSO)` con la hora real de inicio e inserta el registro de auditoría en `historial_estados`.
5. **Capa `dao` + Reactividad hacia la UI (`ViajeDao.kt`):**
   - Como `ViajeDao.obtenerTodosLosViajes()` retorna `Flow<List<ViajeEntity>>`, apenas Room actualiza la fila del viaje #102 a `estadoId = 3`, `DetalleViajeConductorScreen` se recompone automáticamente: el badge cambia de **ASIGNADO** a **EN_CURSO** y el botón cambia automáticamente a **"FINALIZAR VIAJE (PASAR A FINALIZADO)"** sin recargar la pantalla.

---

### 3. Cinco preguntas técnicas probables del profesor (Fase 3 — Compose, Navegación e Intents)

1. **Pregunta:** *¿Cómo pasan el `id` de un viaje desde la lista de viajes hacia la pantalla de detalle o de asignación en Navigation Compose?*  
   **Respuesta:** Definimos la ruta con un placeholder en `Rutas.kt`, por ejemplo `"conductor/detalle_viaje/{viajeId}"`. En `BelmonteNavHost.kt` declaramos el argumento con `navArgument("viajeId") { type = NavType.IntType }`, lo leemos desde `backStackEntry.arguments?.getInt("viajeId")` y al navegar llamamos a `navController.navigate("conductor/detalle_viaje/$viajeId")`.

2. **Pregunta:** *¿Por qué al hacer login usan `popUpTo(Rutas.LOGIN) { inclusive = true }` en el `navController.navigate`?*  
   **Respuesta:** Para eliminar la pantalla de Login de la pila de navegación (`back stack`). Así, cuando el usuario ya entró al menú principal y presiona el botón físico "Atrás" del teléfono, no vuelve a la pantalla de Login a menos que presione explícitamente "Cerrar Sesión".

3. **Pregunta:** *¿Cómo implementaron el Caso de Uso CU-07 para que el conductor abra la ruta hacia el destino en Google Maps o Waze?*  
   **Respuesta:** En `ConductorScreens.kt` (función `abrirNavegacionExterna`) usamos un `Intent` implícito de Android (`Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=lat,lng(direccion)"))`). Primero intentamos abrir el paquete de Google Maps (`com.google.android.apps.maps`), y si el emulador no tiene la app instalada, tenemos un bloque `try/catch` de respaldo que abre la ruta directamente en el navegador con `https://www.google.com/maps/dir/?api=1&destination=...` para que nunca falle.

4. **Pregunta:** *¿Para qué sirve `collectAsState()` en las pantallas de Jetpack Compose?*  
   **Respuesta:** Convierte un `StateFlow` del `ViewModel` en un `State<T>` observable por Jetpack Compose. Cada vez que el `StateFlow` emite un nuevo valor (por ejemplo, cuando cambia la lista de viajes en Room), Compose detecta el cambio y ejecuta la **recomposición** únicamente de los componentes visuales que leen ese estado.

5. **Pregunta:** *¿Cuál es la diferencia entre guardar un dato con `remember { mutableStateOf("") }` en la pantalla vs guardarlo en un `StateFlow` dentro del `ViewModel`?*  
   **Respuesta:** `remember { mutableStateOf(...) }` guarda estado **efímero de la interfaz** (como el texto que el usuario está escribiendo en un `OutlinedTextField` o qué pestaña/chip está seleccionado). En cambio, el `StateFlow` del `ViewModel` guarda el **estado de negocio y los datos de Room**, sobrevive a cambios de configuración y comunica la vista con los repositorios.

---

### 4. Dos ejercicios de "Modifica esto en vivo" para practicar (Fase 3 — Pantallas y Navegación)

#### Ejercicio práctico 1: Agregar una validación nueva o un campo visual a un formulario Compose
* **Lo que puede pedir el profesor:** *"En la pantalla de Nueva Solicitud (`SolicitanteScreens.kt`), no permitan enviar la solicitud si las observaciones tienen menos de 5 caracteres (o agreguen un botón rápido para otro destino como 'Aeropuerto AMB')".*
* **Pasos exactos (toma 1 minuto):**
  1. Abrir `view/SolicitanteScreens.kt` y buscar `NuevaSolicitudScreen`.
  2. Para agregar un destino rápido, en la lista `lugaresRapidos` agregar una línea:
     ```kotlin
     LugarPredefinido("Aeropuerto SCL", "Av. Armando Cortínez Oriente s/n, Pudahuel", -33.3930, -70.7858)
     ```
  3. O para validar un campo antes de guardar, en el `onClick` del `Button` `"CONFIRMAR Y ENVIAR SOLICITUD"`, agregar un `if` simple antes de `viewModel.crearSolicitudViaje(...)`.

#### Ejercicio práctico 2: Agregar un botón en el menú principal que navegue a una pantalla nueva
* **Lo que puede pedir el profesor:** *"Agreguen una pantalla simple de 'Acerca de Belmonte / Integrantes' y un botón en el inicio para abrirla".*
* **Pasos exactos:**
  1. En `navigation/Rutas.kt`, agregar `const val ACERCA_DE = "acerca_de"`.
  2. En `navigation/BelmonteNavHost.kt`, agregar un bloque `composable(Rutas.ACERCA_DE) { ... }` con un `BelmonteCard` que muestre los nombres de los integrantes.
  3. En `view/InicioRolScreen.kt`, agregar un `OutlinedButton(onClick = { onNavigate(Rutas.ACERCA_DE) }) { Text("Acerca de") }`.

---

## Fase 4: Lector de Códigos QR con CameraX + ML Kit Barcode Scanning + Rediseño UI Moderno

### 1. Tabla de archivos de la Fase 4 y qué hace cada uno

| Archivo | Paquete / Ruta | ¿Qué hace y por qué existe? |
| :--- | :--- | :--- |
| `QrAnalyzer.kt` | `cl.belmonte.app.qr` | Implementa `ImageAnalysis.Analyzer` de CameraX. Recibe cada fotograma (`ImageProxy`) de la cámara, lo transforma en `InputImage` de Google ML Kit (`BarcodeScanning`) configurado con `Barcode.FORMAT_QR_CODE` y, al detectar una patente, notifica a la pantalla Compose y cierra el `imageProxy`. |
| `EscanearQrScreen.kt` | `cl.belmonte.app.qr` | Pantalla del escáner QR que solicita el permiso `Manifest.permission.CAMERA` en tiempo de ejecución, muestra la vista previa en vivo con `PreviewView` dentro de `AndroidView`, dibuja el marco óptico (esquinas luminosas y línea láser) con `Canvas` y valida la patente contra Room (`VehiculoRepository.verificarPatenteQrParaViaje`). Incluye además las 4 tarjetas QR interactivas para demostración en emulador. |
| `BelmonteCard.kt` | `cl.belmonte.app.view.components` | Sistema de diseño moderno de Jetpack Compose: `BelmonteCard` con franja lateral de acento (`drawBehind`), `HeroBannerBelmonte` con degradado diagonal, `LineaRutaTimeline` (conector visual Origen → Destino estilo Uber/Cabify), `MapaRutaVisualCard` (mapa vectorial en `Canvas` con ruta GPS), `ChipVehiculoBelmonte` y `TarjetaMenuAccionModerna`. |
| `QR_*.png` (4 archivos) | `C:\dev\belmonte\qr_patentes_prueba\` | Imágenes PNG de 400x400 px con los códigos QR reales de los 4 vehículos de prueba del script SQL: `KR-TW-21` (Toyota Corolla), `LK-JH-44` (Hyundai Staria Van), `PX-RT-89` (Mercedes Vito Van) y `JT-KL-52` (Chevrolet NPR Camión). |

---

### 2. Recorrido por las 5 capas de la lectura de un Código QR (Fase 4)

1. **Capa `qr` (`QrAnalyzer.kt` + `EscanearQrScreen.kt`):**
   - `VistaCamaraQrEnVivo` vincula `Preview` e `ImageAnalysis` al `ProcessCameraProvider` y al ciclo de vida (`LocalLifecycleOwner`).
   - Cuando la cámara enfoca el código QR de la patente (por ejemplo `"KR-TW-21"` para el Viaje #103), `QrAnalyzer.analyze(imageProxy)` extrae `primerQr = "KR-TW-21"` y ejecuta el callback `onQrLeido("KR-TW-21")`.
2. **Capa `qr` → `viewmodel` (`BelmonteViewModel.kt`):**
   - `EscanearQrScreen` llama a `viewModel.verificarQrVehiculo(idViaje = 103, textoQr = "KR-TW-21")`.
3. **Capa `repository` (`VehiculoRepository.kt`):**
   - `verificarPatenteQrParaViaje(103, "KR-TW-21")` consulta el viaje `#103` y el vehículo en Room, normaliza el texto y compara si la patente escaneada coincide con el `idVehiculo` asignado a ese viaje.
4. **Capa `dao` (`VehiculoDao.kt` + `ViajeDao.kt`):**
   - Ejecuta `SELECT * FROM viajes WHERE id_viaje = :idViaje` y `SELECT * FROM vehiculos WHERE patente = :patente`.
5. **Retorno reactivo hacia `EscanearQrScreen.kt`:**
   - `_resultadoQr.value` se actualiza con `Pair(true, "✅ VEHÍCULO VERIFICADO...")` (o `false` si la patente pertenece a otro vehículo) y Compose dibuja la tarjeta verde de confirmación o roja de alerta.

---

### 3. Cinco preguntas técnicas probables del profesor (Fase 4 — CameraX, Permisos y ML Kit)

1. **Pregunta:** *¿Por qué es obligatorio llamar a `imageProxy.close()` dentro del bloque `.addOnCompleteListener` en `QrAnalyzer.kt`?*  
   **Respuesta:** Porque configuramos `ImageAnalysis` con `STRATEGY_KEEP_ONLY_LATEST`. Si no cerramos el `imageProxy` cuando ML Kit termina de procesar el fotograma, CameraX asume que seguimos ocupando el buffer de memoria y deja de enviar nuevos fotogramas, congelando la detección de QR.

2. **Pregunta:** *¿Cómo integran la vista de cámara de CameraX (`PreviewView`), que es una vista clásica de Android, dentro de Jetpack Compose?*  
   **Respuesta:** Usando el composable `AndroidView(factory = { context -> PreviewView(context) })` en `EscanearQrScreen.kt`. `AndroidView` es el puente de interoperabilidad oficial de Jetpack Compose para embeber vistas nativas del SDK de Android.

3. **Pregunta:** *¿Cómo piden el permiso de cámara al usuario en Jetpack Compose?*  
   **Respuesta:** Primero declaramos `<uses-permission android:name="android.permission.CAMERA" />` en `AndroidManifest.xml`, y luego en `EscanearQrScreen.kt` usamos `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())` para solicitar `Manifest.permission.CAMERA` en tiempo de ejecución.

4. **Pregunta:** *¿Por qué filtraron ML Kit con `setBarcodeFormats(Barcode.FORMAT_QR_CODE)` en vez de escanear todos los formatos de códigos de barra?*  
   **Respuesta:** Porque al limitar el detector únicamente a `FORMAT_QR_CODE`, ML Kit no gasta CPU buscando códigos EAN-13, Code-128 o PDF417, logrando una lectura mucho más rápida y sin falsos positivos.

5. **Pregunta:** *¿Cómo dibujaron el marco del visor de cámara y el mapa de ruta sin usar imágenes externas pesadas?*  
   **Respuesta:** Usando el componente `Canvas` de Jetpack Compose (`drawLine`, `drawPath`, `drawCircle`) y degradados `Brush.linearGradient` / `Brush.verticalGradient`. Así todo se renderiza vectorialmente a cualquier resolución de pantalla y es 100% código Kotlin modificable en vivo.

---

### 4. Dos ejercicios de "Modifica esto en vivo" para practicar (Fase 4 — QR y UI)

#### Ejercicio práctico 1: Cambiar el color del marco o la línea láser del escáner QR en vivo
* **Lo que puede pedir el profesor:** *"Cambien el color de las esquinas del visor de la cámara QR a verde o aumenten el tiempo de espera entre lecturas del mismo código QR".*
* **Pasos exactos (toma 30 segundos):**
  1. Para cambiar el tiempo entre lecturas: abrir `qr/QrAnalyzer.kt` (línea 55) y cambiar `2500L` (2,5 segundos) a `5000L` (5 segundos).
  2. Para cambiar el color del marco del visor: abrir `qr/EscanearQrScreen.kt` y en el bloque `Canvas` cambiar `val colorMarco = Color(0xFF38BDF8)` por `val colorMarco = ColorEstadoEnCurso`.

#### Ejercicio práctico 2: Aceptar códigos QR con prefijo corporativo (ej. `"BELMONTE:KR-TW-21"`)
* **Lo que puede pedir el profesor:** *"Modifiquen la validación del QR para que si el código QR viene con el formato `BELMONTE:KR-TW-21` o `PATENTE:KR-TW-21`, igual extraiga la patente y la valide".*
* **Pasos exactos:**
  1. Abrir `repository/VehiculoRepository.kt` y buscar la función `verificarPatenteQrParaViaje`.
  2. Al limpiar el string `textoQr`, encadenar `.removePrefix("BELMONTE:").removePrefix("PATENTE:").trim().uppercase()`.
