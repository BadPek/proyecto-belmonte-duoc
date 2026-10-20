package cl.belmonte.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.belmonte.app.dao.CobroDao
import cl.belmonte.app.dao.UsuarioDao
import cl.belmonte.app.dao.VehiculoDao
import cl.belmonte.app.dao.ViajeDao
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de datos local SQLite gestionada por Room para Traslados Belmonte SpA.
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Reúne las 13 entidades @Entity del modelo relacional v3, expone los 4 DAOs (UsuarioDao,
 * VehiculoDao, ViajeDao y CobroDao) y mantiene una única instancia en memoria (patrón Singleton).
 *
 * ¿POR QUÉ USAMOS fallbackToDestructiveMigration()?
 * Durante el desarrollo y la defensa en vivo, si el profesor nos pide agregar una columna nueva
 * a una tabla @Entity y subimos version = 2, Room recrea la tabla automáticamente y vuelve a
 * cargar los datos de prueba sin exigir escribir un script de migración manual.
 */
@Database(
    entities = [
        EmpresaEntity::class,
        UsuarioEntity::class,
        ConductorEntity::class,
        TipoVehiculoEntity::class,
        VehiculoEntity::class,
        EstadoViajeEntity::class,
        ViajeEntity::class,
        HistorialEstadoEntity::class,
        HistorialReasignacionEntity::class,
        IncidenciaEntity::class,
        CobroEntity::class,
        CobroCupoEntity::class,
        AsignacionCupoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun vehiculoDao(): VehiculoDao
    abstract fun viajeDao(): ViajeDao
    abstract fun cobroDao(): CobroDao

    companion object {
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        /**
         * Obtiene la instancia única de AppDatabase. Si no existe, la construye y precarga
         * los datos de prueba del script SQL.
         */
        fun obtenerBaseDatos(context: Context): AppDatabase {
            return INSTANCIA ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "belmonte_local.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCIA?.let { baseDatos ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    DatosInicialesBelmonte.poblarSiEstaVacia(baseDatos)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCIA = instance
                instance
            }
        }
    }
}
