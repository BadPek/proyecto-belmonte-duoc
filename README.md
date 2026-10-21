# Proyecto Transportes Belmonte (Duoc UC)

Repositorio completo del sistema corporativo de **Transportes Belmonte**, que incluye la aplicación móvil nativa Android (`BelmonteApp`), el backend REST en Spring Boot (`belmonte-backend`), la guía de defensa técnica y toda la documentación de base de datos y casos de uso.

---

## 📁 Estructura del Repositorio

- **[`BelmonteApp/`](./BelmonteApp)**: Proyecto Android nativo en **Kotlin + Jetpack Compose (Material 3)** con arquitectura **MVVM**, base de datos local **Room (SQLite)** con las 8 entidades del modelo relacional, escáner de cámara en vivo (**CameraX + ML Kit**) y mapa GPS interactivo (**OSMDroid**).
- **[`belmonte-backend/`](./belmonte-backend)**: Microservicio REST en **Java + Spring Boot + JPA/Hibernate** conectado a Oracle / H2 con los mismos endpoints consumidos por Retrofit.
- **[`GUIA_DEFENSA.md`](./GUIA_DEFENSA.md)**: Guía paso a paso para la defensa ante el profesor (arquitectura, explicación de cada archivo y ejercicios de modificación en vivo).
- **[`qr_patentes_prueba/`](./qr_patentes_prueba)**: Imágenes PNG de códigos QR listos para escanear con la cámara durante la demostración.
- **[`documentacion/`](./documentacion)**:
  - `script_belmonte_db_v3.sql` — Script DDL/DML completo en Oracle SQL / PL/SQL.
  - `Informe_Base_Datos_Belmonte_v3.docx` — Informe técnico de base de datos.
  - `Informe_Casos_de_Uso_Belmonte_Estudiantes.docx` — Informe de casos de uso y requerimientos.
  - `modelo_relacional_belmonte_v3_completo.png` — Diagrama entidad-relación completo.
  - `maquetas_belmonte.html` — Maquetas interactivas HTML.

---

## 🚀 Cómo abrir y ejecutar en otro PC

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/BadPek/proyecto-belmonte-duoc.git
   ```
2. En **Android Studio**, selecciona **File > Open** y abre la carpeta **`BelmonteApp`** (dentro de `proyecto-belmonte-duoc/BelmonteApp`).
3. Espera a que termine el *Gradle Sync* y presiona **Run ('app')** ▶ sobre un emulador o teléfono físico.

---

## 👥 Portales y Cuentas de Prueba Incluidas

Al iniciar la aplicación, la pantalla inicial permite elegir a qué portal ingresar:

| Portal | Rol Interno | Correo Demo | Contraseña |
| :--- | :--- | :--- | :--- |
| **1. Trabajador que Ocupará Servicios** | `ROLE_SOLICITANTE` | `dmunoz@constructoraandina.cl` / `fcastro@mineralosbronces.cl` | `Belmonte2026` |
| **2. Administrador de Empresa Cliente** | `ROLE_ADMIN_EMPRESA` | `therrera@constructoraandina.cl` / `ivera@mineralosbronces.cl` | `Belmonte2026` |
| **3. Administrador Central de Belmonte** | `ROLE_ADMIN` | `vfuentes@belmonte.cl` | `Belmonte2026` |
| **4. Conductor de Flota Belmonte** | `ROLE_CONDUCTOR` | `psoto@belmonte.cl` / `mrojas@belmonte.cl` | `Belmonte2026` |
