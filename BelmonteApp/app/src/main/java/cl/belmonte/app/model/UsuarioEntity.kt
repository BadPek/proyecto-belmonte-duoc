package cl.belmonte.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para la tabla "usuarios" (todas las personas que inician sesión en la app).
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Guarda los datos de login y perfil de los 4 roles del sistema:
 * - ROLE_ADMIN (Recepcionista / Administrador central de Belmonte, idEmpresa = null)
 * - ROLE_ADMIN_EMPRESA (Administrador de empresa cliente, idEmpresa obligatorio)
 * - ROLE_SOLICITANTE (Trabajador que pide viajes con sus cupos, idEmpresa obligatorio)
 * - ROLE_CONDUCTOR (Chofer de Belmonte, idEmpresa = null)
 *
 * NOTA SOBRE LA CONTRASEÑA (RN-01):
 * En la base de datos local Room guardamos la clave de prueba "Password123*" en texto plano
 * para permitir el login offline en esta etapa (Prueba 2). En el backend (ms-usuarios con
 * Spring Security) la columna password_hash almacena y verifica el hash con BCrypt.
 */
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_usuario")
    val idUsuario: Long = 0,

    @ColumnInfo(name = "id_rol")
    val idRol: Int,

    @ColumnInfo(name = "rol")
    val rol: String, // ROLE_ADMIN, ROLE_ADMIN_EMPRESA, ROLE_SOLICITANTE, ROLE_CONDUCTOR

    @ColumnInfo(name = "id_empresa")
    val idEmpresa: Long?, // NULL para personal de Belmonte; obligatorio para clientes (RN-05)

    @ColumnInfo(name = "rut")
    val rut: String,

    @ColumnInfo(name = "nombres")
    val nombres: String,

    @ColumnInfo(name = "apellidos")
    val apellidos: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "password_hash")
    val passwordHash: String, // En local: "Password123*". En el backend se usa BCrypt.

    @ColumnInfo(name = "telefono")
    val telefono: String,

    @ColumnInfo(name = "activo")
    val activo: Boolean = true
) {
    /** Propiedad auxiliar para mostrar el nombre completo fácilmente en las pantallas Compose. */
    val nombreCompleto: String
        get() = "$nombres $apellidos"
}
