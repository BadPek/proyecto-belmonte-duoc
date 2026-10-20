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
