# Guía de Defensa en Vivo — Proyecto Traslados Belmonte SpA (DSY1105)

**Integrantes:** Andrés Osorio · Joaquín Henríquez  
**Ubicación del proyecto Android:** `C:\dev\belmonte\BelmonteApp`  
**Ubicación de microservicios Backend:** `C:\dev\belmonte\belmonte-backend`

---

## FASE 1: Proyecto Android + Dependencias + Paquetes + Tema Material 3

### 1. ¿Qué archivos se crearon en esta Fase 1 y para qué sirve cada uno?

#### Configuración de Gradle y Proyecto (`C:\dev\belmonte\BelmonteApp`)
| Archivo | ¿Qué hace? | ¿Por qué está ahí? |
|---|---|---|
| `settings.gradle.kts` | Define el nombre del proyecto (`BelmonteApp`), incluye el módulo `:app` y configura los repositorios de descarga (`google()`, `mavenCentral()`). | Sin este archivo Gradle no sabe qué módulos compilar ni de dónde descargar librerías de AndroidX. |
| `gradle/libs.versions.toml` | **Version Catalog** (catálogo centralizado de versiones). Declara en `[versions]`, `[libraries]` y `[plugins]` las versiones exactas de Room, KSP, Navigation Compose, Lifecycle, Compose BOM y CameraX/ML Kit. | Es el estándar actual de Android Studio con Kotlin DSL: permite cambiar una versión en un solo lugar sin buscarla en varios archivos `build.gradle.kts`. |
| `build.gradle.kts` (raíz) | Declara los plugins globales (`android-application`, `kotlin-android`, `kotlin-compose`, `ksp`) con `apply false`. | Carga los plugins una sola vez para que los submódulos (`:app`) los activen. |
| `app/build.gradle.kts` | Configura el `namespace = "cl.belmonte.app"`, `minSdk = 26`, `compileSdk`, activa `compose = true` y declara las dependencias con `implementation(...)` y `ksp(...)`. | Es el archivo que le dice al compilador qué librerías usa nuestra app móvil y activa el procesador de anotaciones KSP para Room. |
| `app/src/main/AndroidManifest.xml` | Registra la clase `BelmonteApplication`, la actividad principal `MainActivity` y el permiso de cámara (`android.permission.CAMERA`). | Es el archivo de identidad de la app ante el sistema operativo Android. |

#### Código fuente en `app/src/main/java/cl/belmonte/app/` (Estructura obligatoria del profesor)
| Paquete / Archivo | ¿Qué hace? | ¿Por qué se diseñó así? |
|---|---|---|
| `BelmonteApplication.kt` | Clase que hereda de `Application`. Se ejecuta una sola vez al abrir la app. | Reemplaza a librerías como Hilt/Koin (que no se ven en clases): en la Fase 2 aquí crearemos la instancia única de `AppDatabase` y los repositorios. |
| `MainActivity.kt` | Única `ComponentActivity` del proyecto. Llama a `setContent { BelmonteAppTheme { ... } }`. | En Jetpack Compose se usa una sola Activity y todas las pantallas son funciones `@Composable`. |
| `ui/theme/Color.kt` | Define los colores corporativos de Belmonte SpA (`AzulBelmonte = 0xFF2563EB`, `AzulMarinoBelmonte = 0xFF002B5B`, `RojoAccionBelmonte = 0xFFE84C3D`, `FondoOscuro = 0xFF0F172A`) y los colores de los 5 estados del viaje. | Respeta el diseño visual de `maquetas_belmonte.html` (oscuro, azul `#2563EB`, tarjetas redondeadas) y permite modificar cualquier color en segundos durante la presentación. |
| `ui/theme/Type.kt` | Configura la tipografía Material 3 (`headlineLarge`, `titleLarge`, `titleMedium`, `bodyLarge`, `bodyMedium`, `labelLarge`). | Estandariza los tamaños de fuente en toda la app. |
| `ui/theme/Theme.kt` | Crea el `darkColorScheme` y expone el composable `BelmonteAppTheme`. | Aplica automáticamente los colores oscuros y el azul `#2563EB` a todos los `Button`, `Card`, `Scaffold` y `TopAppBar`. |
| `view/components/BelmonteCard.kt` | Define dos componentes reutilizables: `BelmonteCard` (tarjeta redondeada con borde suave) y `BadgeEstadoViaje` (etiqueta de color según estado `PENDIENTE`, `ASIGNADO`, `EN_CURSO`, `FINALIZADO`, `CANCELADO`). | Evita repetir código de diseño en las pantallas de Solicitante, Admin Central, Conductor y Admin de Empresa. |
| `view/InicioFase1Screen.kt` | Pantalla de prueba de la Fase 1 que muestra una tarjeta de viaje de muestra (#101), los paquetes configurados y un botón interactivo que rota el badge por los 5 estados del viaje. | Permite comprobar en el emulador que Compose, el estado (`remember { mutableIntStateOf }`) y el tema Material 3 funcionan antes de pasar a Room. |
| `navigation/Rutas.kt` | Objeto `Rutas` con las constantes de todas las pantallas del proyecto (`login`, `nueva_solicitud`, `mis_viajes`, `solicitudes_pendientes`, `viajes_conductor`, `escanear_qr/{idViaje}`, etc.). | Centraliza los nombres de ruta que usará el `NavHost` en la Fase 3. |
| `model/`, `dao/`, `database/`, `repository/`, `viewmodel/`, `qr/` | Paquetes creados desde la Fase 1 con su estructura lista para las Fases 2, 3 y 4. | Cumple desde el primer commit con la estructura obligatoria exigida por el docente. |

---

### 2. Recorrido completo de una funcionalidad (Arquitectura MVVM + Room)

En nuestra aplicación, cualquier acción del usuario sigue siempre este flujo en **5 capas** (sin saltarse ninguna):

```
[1. VIEW (Compose)]  ──(evento click / observa StateFlow)──>  [2. VIEWMODEL]
                                                                    │
                                                       (llama función suspend / Flow)
                                                                    ▼
[5. DATABASE (Roomite)] <──(ejecuta SQL generado por KSP)── [4. DAO] <── [3. REPOSITORY]
```

1. **View (`view/`):** Es una función `@Composable` (por ejemplo, `NuevaSolicitudScreen`). Solo dibuja la interfaz en pantalla observando el estado con `collectAsState()` y cuando el usuario presiona un botón (ej. *"Confirmar Solicitud"*), llama a una función del `ViewModel`. **La vista nunca contiene SQL ni cálculos de negocio.**
2. **ViewModel (`viewmodel/`):** Recibe la acción de la vista, abre una corrutina con `viewModelScope.launch { ... }`, valida los campos de la pantalla y llama al `Repository`. Expone los datos hacia la vista mediante `StateFlow` (`_uiState.asStateFlow()`). **El ViewModel solo habla con el Repository, nunca directo con el DAO.**
3. **Repository (`repository/`):** Contiene la lógica de negocio local (por ejemplo: verificar que al trabajador le queden cupos del tipo de vehículo elegido, calcular la distancia con la fórmula de Haversine y los km extra, insertar el viaje y registrar la fila en `historial_estados_viaje`) invocando uno o varios métodos del `DAO`.
4. **DAO (`dao/`):** Es una `interface` anotada con `@Dao`. Define las operaciones SQL sobre las tablas (`@Insert`, `@Update`, `@Delete`, `@Query("SELECT ...")`). Si devuelve `Flow<List<ViajeEntity>>`, Room avisa automáticamente cada vez que cambia la tabla.
5. **Database (`database/AppDatabase.kt`):** Clase abstracta que hereda de `RoomDatabase` y lista todas las entidades (`@Database(entities = [...])`). Al crearse por primera vez (`RoomDatabase.Callback.onCreate`), precarga los datos de prueba del script SQL (empresas, usuarios, vehículos, cupos y viajes 101–107).

---

### 3. ¿Qué hace cada dependencia en `libs.versions.toml` y `app/build.gradle.kts`?

| Dependencia | ¿Para qué sirve en BelmonteApp? |
|---|---|
| `androidx.compose:compose-bom` | **Bill of Materials de Compose**: coordina que `ui`, `material3`, `ui-graphics` y `ui-tooling` usen versiones compatibles entre sí sin ponerle versión a cada una. |
| `androidx.compose.material3:material3` | Provee los componentes visuales de **Material Design 3**: `Scaffold`, `TopAppBar`, `Card`, `Button`, `OutlinedTextField`, `Badge`, etc. |
| `androidx.compose.material:material-icons-extended` | Íconos vectoriales de Material (autos, camiones, código QR, mapa, alerta de incidencia, teléfono). |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | Permite obtener e inyectar los `ViewModel` directamente dentro de funciones `@Composable` usando `viewModel(factory = ...)` y mantenerlos vivos aunque gire la pantalla. |
| `androidx.navigation:navigation-compose` (`2.9.8` / `2.10.x`) | Permite navegar entre pantallas `@Composable` mediante `NavHost` y `NavController` pasando argumentos como `{idViaje}`. |
| `androidx.room:room-runtime` (`2.8.5`) | Librería principal de **Room** para crear y consultar la base de datos SQLite local en el teléfono. En Room `2.8.5` ya incluye soporte nativo para `suspend` y `Flow` de Kotlin, por lo que **no se usa `room-ktx`**. |
| `androidx.room:room-compiler` (`2.8.5` con `ksp`) | Procesador de anotaciones que lee nuestras clases `@Entity`, `@Dao` y `@Database` en tiempo de compilación y escribe automáticamente el código SQLite real (`AppDatabase_Impl`). |
| `com.google.devtools.ksp` (`2.3.11`) | **Kotlin Symbol Processing**: herramienta que ejecuta `room-compiler` mucho más rápido que el antiguo `kapt` porque entiende código Kotlin directamente. |
| `androidx.camera:camera-*` (CameraX) | Controla la cámara del teléfono respetando el ciclo de vida de la pantalla para mostrar la vista previa al escanear el QR del vehículo. |
| `com.google.mlkit:barcode-scanning` | Librería de **ML Kit** que analiza cada fotograma de CameraX y detecta el código QR con la patente del vehículo (`KR-TW-21`, `LK-JH-44`, `PX-RT-89`, `JT-KL-52`) sin necesitar internet. |

---

### 4. Cinco preguntas técnicas probables del profesor (con respuesta clara)

1. **Pregunta:** *¿Por qué en `app/build.gradle.kts` usaron `ksp(libs.androidx.room.compiler)` y no agregaron `room-ktx`?*  
   **Respuesta:** Porque `room-compiler` no es una librería que se ejecute dentro del celular en tiempo de ejecución (`implementation`), sino un generador de código que corre al compilar el proyecto usando **KSP (Kotlin Symbol Processing)** para crear las clases `_Impl` de los DAOs y de `AppDatabase`. Y no agregamos `room-ktx` porque a partir de Room 2.7 y 2.8 toda la integración con corrutinas (`suspend`) y `Flow` ya viene incorporada directamente dentro de `room-runtime`.

2. **Pregunta:** *¿Cómo crean e inyectan los repositorios y ViewModels si no están usando Hilt ni Koin?*  
   **Respuesta:** Lo hacemos de forma manual como se ve en clases: creamos la clase `BelmonteApplication` (que hereda de `Application` y está registrada en el `AndroidManifest.xml`). Ahí instanciamos una sola vez `AppDatabase` y cada `Repository`. Luego, cada `ViewModel` tiene su propio `ViewModelProvider.Factory` que recibe el repositorio desde `BelmonteApplication`.

3. **Pregunta:** *¿Dónde se definen los colores de la aplicación y cómo se aplican a los componentes de las pantallas?*  
   **Respuesta:** En el paquete `ui.theme`: primero declaramos las constantes hexadecimales en `Color.kt` (como `AzulBelmonte = Color(0xFF2563EB)`), luego las asignamos al esquema `darkColorScheme` en `Theme.kt` dentro de `BelmonteAppTheme`, y finalmente envolvemos la aplicación en `MainActivity.kt` con `BelmonteAppTheme`. Así cualquier componente Material 3 toma automáticamente esos colores mediante `MaterialTheme.colorScheme`.

4. **Pregunta:** *En la pantalla `InicioFase1Screen`, ¿por qué usan `var indiceEstado by remember { mutableIntStateOf(0) }` en vez de una variable normal `var indiceEstado = 0`?*  
   **Respuesta:** Porque en Jetpack Compose, si usamos una variable normal, al cambiar su valor Compose no se entera de que debe volver a dibujar la pantalla (no hay **recomposición**) y además el valor se reiniciaría a `0` en cada redibujado. `mutableIntStateOf` crea un estado observable por Compose y `remember` conserva ese valor entre recomposiciones.

5. **Pregunta:** *¿Por qué el proyecto tiene una sola Activity (`MainActivity`) si la aplicación tiene más de 12 pantallas distintas?*  
   **Respuesta:** Porque con Jetpack Compose aplicamos la arquitectura **Single-Activity**. `MainActivity` es solo el contenedor raíz donde vive el tema y el `NavHost` de Navigation Compose, y cada pantalla del sistema (Login, Solicitar Viaje, Viajes del Conductor, Escanear QR, etc.) es una función `@Composable` liviana que se intercambia sin el costo de crear nuevas Activities de Android.

---

### 5. Dos ejercicios de "Modifica esto en vivo" para practicar (Fase 1)

#### Ejercicio práctico 1: Cambiar el color de un estado o el color principal corporativo
* **Lo que puede pedir el profesor:** *"Cambien el color del estado `EN_CURSO` a celeste o cambien el color de los botones principales y muéstrenlo en el emulador".*
* **Dónde ir:** Abrir `app/src/main/java/cl/belmonte/app/ui/theme/Color.kt`.
* **Qué modificar:**
  Cambiar la línea:
  ```kotlin
  val ColorEstadoEnCurso = Color(0xFF22C55E) // Verde original
  ```
  por:
  ```kotlin
  val ColorEstadoEnCurso = Color(0xFF06B6D4) // Cian / Celeste
  ```
* **Resultado al ejecutar:** En la tarjeta de viaje, cuando el badge pasa a *"En Curso"*, su borde, fondo translúcido y texto cambian inmediatamente al nuevo color.

#### Ejercicio práctico 2: Agregar un botón nuevo o un dato adicional en la tarjeta de viaje de prueba
* **Lo que puede pedir el profesor:** *"Agreguen en la pantalla `InicioFase1Screen` un texto que muestre los kilómetros incluidos del vehículo y un botón que salude o cambie directamente al estado `FINALIZADO`".*
* **Dónde ir:** Abrir `app/src/main/java/cl/belmonte/app/view/InicioFase1Screen.kt`.
* **Qué modificar:** Debajo del `Text` de *"Destino: Aeropuerto AMB..."* (alrededor de la línea 95), agregar:
  ```kotlin
  Text(
      text = "Tipo: Auto Ejecutivo (Tope: 40 km incluidos)",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.tertiary
  )
  Spacer(modifier = Modifier.height(8.dp))
  Button(
      onClick = { indiceEstado = 3 }, // Posición 3 en la lista = "FINALIZADO"
      modifier = Modifier.fillMaxWidth()
  ) {
      Text("PASAR DIRECTO A FINALIZADO")
  }
  ```
* **Resultado al ejecutar:** Aparece el nuevo texto y al pulsar el botón el badge cambia directamente a *"Finalizado"*.
