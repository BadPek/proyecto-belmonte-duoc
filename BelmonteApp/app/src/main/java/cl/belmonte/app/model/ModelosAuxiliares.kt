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

/**
 * Modelo del Catálogo de Planes de Suscripción Mensual para Empresas Cliente.
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Representa los planes predefinidos de suscripción mensual (Básico, Grupos, Carga) y las tarifas
 * unitarias para el Plan Personalizado. Cuando la empresa contrata uno de estos planes, sus cupos
 * (cuposAuto, cuposVan, cuposCamion) y su precio se copian al cobro PAQUETE del mes en Room,
 * manteniendo intactas las asignaciones a los trabajadores.
 */
data class PlanSuscripcionCatalogo(
    val idPlan: Int,
    val codigo: String,
    val nombre: String,
    val descripcionIncluye: String,
    val cuposAuto: Int,
    val cuposVan: Int,
    val cuposCamion: Int,
    val precioMensualClp: Int,
    val etiquetaDestacada: String? = null
)

object CatalogoPlanesBelmonte {
    // Tarifas por viaje unitario cuando la empresa arma un Plan Personalizado a medida
    const val PRECIO_CUPO_AUTO_CLP = 28000
    const val PRECIO_CUPO_VAN_CLP = 45000
    const val PRECIO_CUPO_CAMION_CLP = 68000

    val planesFijos = listOf(
        PlanSuscripcionCatalogo(
            idPlan = 1,
            codigo = "PLAN_BASICO",
            nombre = "Plan Básico",
            descripcionIncluye = "10 viajes en Auto + 1 viaje en Van al mes",
            cuposAuto = 10,
            cuposVan = 1,
            cuposCamion = 0,
            precioMensualClp = 310000,
            etiquetaDestacada = "EJECUTIVO"
        ),
        PlanSuscripcionCatalogo(
            idPlan = 2,
            codigo = "PLAN_GRUPOS",
            nombre = "Plan Grupos",
            descripcionIncluye = "10 viajes en Auto + 4 viajes en Van al mes",
            cuposAuto = 10,
            cuposVan = 4,
            cuposCamion = 0,
            precioMensualClp = 440000,
            etiquetaDestacada = "MÁS POPULAR"
        ),
        PlanSuscripcionCatalogo(
            idPlan = 3,
            codigo = "PLAN_CARGA",
            nombre = "Plan Carga & Flota",
            descripcionIncluye = "10 viajes en Auto + 6 en Van + 5 en Camión al mes",
            cuposAuto = 10,
            cuposVan = 6,
            cuposCamion = 5,
            precioMensualClp = 850000,
            etiquetaDestacada = "FULL FLOTA"
        )
    )

    fun calcularPrecioPersonalizado(autos: Int, vans: Int, camiones: Int): Int {
        return (autos.coerceAtLeast(0) * PRECIO_CUPO_AUTO_CLP) +
            (vans.coerceAtLeast(0) * PRECIO_CUPO_VAN_CLP) +
            (camiones.coerceAtLeast(0) * PRECIO_CUPO_CAMION_CLP)
    }
}
