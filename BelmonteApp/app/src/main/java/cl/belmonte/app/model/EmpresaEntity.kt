package cl.belmonte.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para la tabla "empresas" (clientes corporativos que contratan traslados).
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Representa a cada empresa cliente de Belmonte SpA (ej: Minera Andes Sur SpA y Consultora Pacífico Austral).
 *
 * ¿POR QUÉ SE HACE ASÍ?
 * Mantiene los mismos nombres de columnas del modelo relacional v3 (script_belmonte_db_v3.sql)
 * usando @ColumnInfo para que SQLite local refleje fielmente la base de datos del proyecto.
 */
@Entity(tableName = "empresas")
data class EmpresaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_empresa")
    val idEmpresa: Long = 0,

    @ColumnInfo(name = "rut_empresa")
    val rutEmpresa: String,

    @ColumnInfo(name = "razon_social")
    val razonSocial: String,

    @ColumnInfo(name = "giro")
    val giro: String?,

    @ColumnInfo(name = "direccion")
    val direccion: String,

    @ColumnInfo(name = "telefono")
    val telefono: String,

    @ColumnInfo(name = "email_contacto")
    val emailContacto: String,

    @ColumnInfo(name = "activa")
    val activa: Boolean = true
)
