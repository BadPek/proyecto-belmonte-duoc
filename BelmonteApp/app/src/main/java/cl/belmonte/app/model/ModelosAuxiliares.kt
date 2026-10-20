package cl.belmonte.app.model

/**
 * Data classes auxiliares (no son tablas @Entity, sino modelos de vista/consulta)
 * para mostrar información combinada en las pantallas de la aplicación.
 *
 * ¿POR QUÉ SE HACE ASÍ?
 * Permite entregar al ViewModel y a las pantallas Compose un objeto simple y listo para mostrar
 * (por ejemplo, cupos asignados vs usados vs disponibles, o los datos del chofer con su nombre).
 */

/**
 * Resumen de cupos de un trabajador para un tipo de vehículo (Consulta C del script SQL).
 */
data class ResumenCupoTrabajador(
    val tipoVehiculo: TipoVehiculoEntity,
    val asignados: Int,
    val usados: Int,
    val disponibles: Int
)

/**
 * Resumen de cupos de una empresa cliente por tipo de vehículo (Consulta B del script SQL).
 */
data class ResumenCupoEmpresa(
    val idCobroCupo: Long,
    val tipoVehiculo: TipoVehiculoEntity,
    val contratados: Int,
    val repartidos: Int,
    val disponiblesPorRepartir: Int,
    val usadosPorEmpresa: Int
)

/**
 * Une la fila de UsuarioEntity con ConductorEntity para mostrar nombre, teléfono y licencia del chofer.
 */
data class ConductorConUsuario(
    val usuario: UsuarioEntity,
    val conductor: ConductorEntity
)

/**
 * Ubicación georreferenciada de prueba para facilitar el formulario de Nueva Solicitud (CU-02)
 * con coordenadas reales de Santiago y Rancagua (las mismas del script SQL).
 */
data class LugarFrecuente(
    val nombreCorto: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double
)

/**
 * Catálogo de lugares georreferenciados de prueba (Anexo 3.2 y script_belmonte_db_v3.sql).
 */
object LugaresDePrueba {
    val lista = listOf(
        LugarFrecuente(
            nombreCorto = "Oficina Minera Andes Sur (Las Condes)",
            direccion = "Av. Apoquindo 4501, Las Condes",
            latitud = -33.4116000,
            longitud = -70.5843000
        ),
        LugarFrecuente(
            nombreCorto = "Aeropuerto AMB (Pudahuel)",
            direccion = "Aeropuerto AMB, Pudahuel",
            latitud = -33.3930000,
            longitud = -70.7944000
        ),
        LugarFrecuente(
            nombreCorto = "Isidora Goyenechea 3000 (Las Condes)",
            direccion = "Isidora Goyenechea 3000, Las Condes",
            latitud = -33.4149000,
            longitud = -70.5986000
        ),
        LugarFrecuente(
            nombreCorto = "Oficina Consultora Pacífico (Providencia)",
            direccion = "Av. Providencia 2133, Providencia",
            latitud = -33.4230000,
            longitud = -70.6100000
        ),
        LugarFrecuente(
            nombreCorto = "Centro de Santiago (Alameda 1302)",
            direccion = "Av. Libertador B. O'Higgins 1302, Santiago",
            latitud = -33.4440000,
            longitud = -70.6530000
        ),
        LugarFrecuente(
            nombreCorto = "Bodega Maipú (Av. Pajaritos 4500)",
            direccion = "Av. Pajaritos 4500, Maipú",
            latitud = -33.4850000,
            longitud = -70.7550000
        ),
        LugarFrecuente(
            nombreCorto = "Faena Rancagua (más de 60 km - genera Km Extra)",
            direccion = "Av. Libertador B. O'Higgins 500, Rancagua",
            latitud = -34.1700000,
            longitud = -70.7400000
        )
    )
}
