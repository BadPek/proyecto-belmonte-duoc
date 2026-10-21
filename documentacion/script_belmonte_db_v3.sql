-- ==============================================================================
-- PROYECTO TRASLADOS BELMONTE SpA  —  MODELO RELACIONAL v3 (corregido)
-- Asignatura: DSY1105 Desarrollo de Aplicaciones Móviles - Duoc UC
-- Integrantes: Andrés Osorio - Joaquín Henríquez
-- Motor objetivo: MySQL 8.0.16+ (probado también en MariaDB 10.11)
--
-- MODELO DE NEGOCIO
--   Belmonte pone la flota (autos, vans y camiones) y los choferes. Cada empresa cliente
--   compra un PAQUETE MENSUAL con cupos de viajes por tipo de vehículo
--   (ej: $5.000.000 = 30 viajes en auto, 20 en van y 10 en camión). El admin de la empresa
--   reparte esos cupos entre sus trabajadores. Cada viaje consume 1 cupo del tipo pedido.
--   Cada tipo de vehículo incluye un tope de km por viaje; los km sobre el tope se
--   cobran a fin de mes (cobro EXTRAS). Los cupos que no se usan vencen al terminar el mes.
--
-- Dueño de cada tabla (arquitectura de microservicios, misma BD compartida):
--   ms-usuarios : roles, usuarios, empresas, conductores
--   ms-vehiculos: tipos_vehiculo, vehiculos
--   ms-viajes   : viajes, estados_viaje, historial_estados_viaje, historial_reasignaciones, incidencias
--   ms-cobros   : cobros, cobro_cupos, asignaciones_cupo
--
-- Cambios respecto a la v2:
--   + EMPRESAS y rol ROLE_ADMIN_EMPRESA (ve viajes, cobros y reparte cupos de SU empresa).
--   + CONDUCTORES: datos propios del chofer (licencia, foto, disponibilidad), 1:1 con USUARIOS.
--   + TIPOS_VEHICULO: auto, van y camión, con km incluidos por viaje y valor del km extra.
--   + ESTADOS_VIAJE (catálogo) e HISTORIAL_ESTADOS_VIAJE (quién cambió cada estado y cuándo).
--   + COBROS (cobro mensual a cada empresa), COBRO_CUPOS (detalle del paquete) y
--     ASIGNACIONES_CUPO (cupos que el admin de empresa entrega a cada trabajador).
--   + VIAJES recupera todos los datos del anexo (pasajero, fecha, hora, origen/destino
--     georreferenciados, vuelo y estado) y agrega empresa, tipo de vehículo, distancia y km extra.
--   + INCIDENCIAS guarda foto (cámara) y coordenadas GPS del teléfono.
--
-- Todos los datos de prueba son FICTICIOS (Ley 19.628 / Ley 21.719 y anexo del caso).
-- Clave de prueba para todos los usuarios: Password123*  (hash BCrypt real, verificado)
-- ==============================================================================

DROP DATABASE IF EXISTS belmonte_db;
CREATE DATABASE belmonte_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE belmonte_db;

-- ------------------------------------------------------------------------------
-- 1. EMPRESAS (clientes corporativos que contratan los traslados)
-- ------------------------------------------------------------------------------
CREATE TABLE empresas (
    id_empresa      BIGINT AUTO_INCREMENT PRIMARY KEY,
    rut_empresa     VARCHAR(12)  NOT NULL UNIQUE COMMENT 'Formato 76482913-1',
    razon_social    VARCHAR(120) NOT NULL,
    giro            VARCHAR(120) NULL,
    direccion       VARCHAR(255) NOT NULL,
    telefono        VARCHAR(20)  NOT NULL,
    email_contacto  VARCHAR(100) NOT NULL,
    activa          BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 2. ROLES (autorización con Spring Security + JWT)
-- ------------------------------------------------------------------------------
CREATE TABLE roles (
    id_rol       INT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(50)  NOT NULL UNIQUE COMMENT 'ROLE_ADMIN, ROLE_ADMIN_EMPRESA, ROLE_SOLICITANTE, ROLE_CONDUCTOR',
    descripcion  VARCHAR(150) NULL
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 3. USUARIOS (todas las personas que inician sesión)
--    id_empresa es NULL para el personal de Belmonte (admin central y conductores)
--    y obligatorio (regla validada en el backend) para ADMIN_EMPRESA y SOLICITANTE.
-- ------------------------------------------------------------------------------
CREATE TABLE usuarios (
    id_usuario     BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_rol         INT          NOT NULL,
    id_empresa     BIGINT       NULL,
    rut            VARCHAR(12)  NOT NULL UNIQUE,
    nombres        VARCHAR(60)  NOT NULL,
    apellidos      VARCHAR(60)  NOT NULL,
    email          VARCHAR(100) NOT NULL UNIQUE COMMENT 'Usuario de login',
    password_hash  VARCHAR(255) NOT NULL COMMENT 'Hash BCrypt',
    telefono       VARCHAR(20)  NOT NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuarios_rol     FOREIGN KEY (id_rol)     REFERENCES roles(id_rol),
    CONSTRAINT fk_usuarios_empresa FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 4. CONDUCTORES (especialización 1:1 de USUARIOS: solo los choferes tienen fila aquí)
--    La PK es a la vez FK a usuarios -> un usuario es conductor como máximo una vez.
-- ------------------------------------------------------------------------------
CREATE TABLE conductores (
    id_conductor          BIGINT       PRIMARY KEY COMMENT 'Mismo id que usuarios.id_usuario',
    clase_licencia        VARCHAR(5)   NOT NULL COMMENT 'Clase de licencia profesional',
    vencimiento_licencia  DATE         NOT NULL,
    foto_url              VARCHAR(255) NULL COMMENT 'Foto que ve el pasajero en "Conductor asignado"',
    disponible            BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_conductores_usuario FOREIGN KEY (id_conductor) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 5. TIPOS_VEHICULO (catálogo: auto, van, camión)
--    km_incluidos: km que cubre 1 cupo. Los km sobre ese tope se cobran a fin de mes
--    a valor_km_extra.
-- ------------------------------------------------------------------------------
CREATE TABLE tipos_vehiculo (
    id_tipo_vehiculo  INT          PRIMARY KEY,
    codigo            VARCHAR(20)  NOT NULL UNIQUE COMMENT 'AUTO, VAN, CAMION',
    nombre            VARCHAR(40)  NOT NULL,
    descripcion       VARCHAR(150) NULL COMMENT 'Para qué se usa',
    km_incluidos      INT          NOT NULL COMMENT 'Tope de km por viaje incluido en 1 cupo',
    valor_km_extra    INT          NOT NULL COMMENT 'CLP por cada km sobre el tope',
    activo            BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_tipos_km    CHECK (km_incluidos > 0),
    CONSTRAINT ck_tipos_valor CHECK (valor_km_extra >= 0)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 6. VEHICULOS (flota de Belmonte)
-- ------------------------------------------------------------------------------
CREATE TABLE vehiculos (
    id_vehiculo          BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_tipo_vehiculo     INT         NOT NULL,
    patente              VARCHAR(10) NOT NULL UNIQUE,
    marca                VARCHAR(40) NOT NULL,
    modelo               VARCHAR(40) NOT NULL,
    anio                 INT         NOT NULL,
    color                VARCHAR(30) NOT NULL,
    capacidad_pasajeros  INT         NOT NULL,
    activo               BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_vehiculos_tipo      FOREIGN KEY (id_tipo_vehiculo) REFERENCES tipos_vehiculo(id_tipo_vehiculo),
    CONSTRAINT ck_vehiculos_capacidad CHECK (capacidad_pasajeros BETWEEN 1 AND 20),
    CONSTRAINT ck_vehiculos_anio      CHECK (anio BETWEEN 2000 AND 2100)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 7. ESTADOS_VIAJE (catálogo: PENDIENTE -> ASIGNADO -> EN_CURSO -> FINALIZADO / CANCELADO)
-- ------------------------------------------------------------------------------
CREATE TABLE estados_viaje (
    id_estado    INT          PRIMARY KEY,
    codigo       VARCHAR(20)  NOT NULL UNIQUE,
    nombre       VARCHAR(40)  NOT NULL,
    descripcion  VARCHAR(150) NULL,
    orden        INT          NOT NULL COMMENT 'Posición en el flujo, para mostrar en la app'
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 8. VIAJES (entidad central: estructura exigida por el anexo 3.2)
--    distancia_km, km_extra y cargo_extra se calculan al pedir el viaje con la tarifa
--    vigente del tipo de vehículo; el cargo extra solo se cobra si el viaje termina.
-- ------------------------------------------------------------------------------
CREATE TABLE viajes (
    id_viaje            BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_solicitante      BIGINT        NOT NULL COMMENT 'Usuario que pide el traslado (usa uno de sus cupos)',
    id_empresa          BIGINT        NOT NULL COMMENT 'Empresa del solicitante al pedir el viaje; no cambia después',
    id_tipo_vehiculo    INT           NOT NULL COMMENT 'Tipo pedido: define qué cupo se consume',
    id_conductor        BIGINT        NULL     COMMENT 'NULL mientras está PENDIENTE',
    id_vehiculo         BIGINT        NULL     COMMENT 'Se asigna junto al conductor (debe ser del tipo pedido)',
    id_estado           INT           NOT NULL DEFAULT 1 COMMENT 'Estado actual (1 = PENDIENTE)',
    nombre_pasajero     VARCHAR(100)  NOT NULL COMMENT 'Pasajero, o quien recibe la carga en viajes de camión',
    telefono_pasajero   VARCHAR(20)   NULL,
    cantidad_pasajeros  INT           NOT NULL DEFAULT 1 COMMENT 'Puede ser 0 en viajes de camión',
    descripcion_carga   VARCHAR(255)  NULL     COMMENT 'Solo viajes de camión',
    fecha_servicio      DATE          NOT NULL COMMENT 'Su mes define de qué paquete sale el cupo',
    hora_servicio       TIME          NOT NULL,
    direccion_origen    VARCHAR(255)  NOT NULL,
    latitud_origen      DECIMAL(10,7) NOT NULL COMMENT 'Para Intent de Google Maps / Waze',
    longitud_origen     DECIMAL(10,7) NOT NULL,
    direccion_destino   VARCHAR(255)  NOT NULL,
    latitud_destino     DECIMAL(10,7) NOT NULL,
    longitud_destino    DECIMAL(10,7) NOT NULL,
    numero_vuelo        VARCHAR(10)   NULL     COMMENT 'Solo traslados aeropuerto',
    observaciones       VARCHAR(255)  NULL,
    distancia_km        DECIMAL(6,2)  NOT NULL COMMENT 'Línea recta origen-destino (haversine)',
    km_extra            DECIMAL(6,2)  NOT NULL DEFAULT 0 COMMENT 'Km sobre el tope del tipo de vehículo',
    cargo_extra         INT           NOT NULL DEFAULT 0 COMMENT 'CLP = km_extra x valor_km_extra; se cobra a fin de mes',
    creado_en           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_viajes_solicitante FOREIGN KEY (id_solicitante)   REFERENCES usuarios(id_usuario),
    CONSTRAINT fk_viajes_empresa     FOREIGN KEY (id_empresa)       REFERENCES empresas(id_empresa),
    CONSTRAINT fk_viajes_tipo        FOREIGN KEY (id_tipo_vehiculo) REFERENCES tipos_vehiculo(id_tipo_vehiculo),
    CONSTRAINT fk_viajes_conductor   FOREIGN KEY (id_conductor)     REFERENCES conductores(id_conductor),
    CONSTRAINT fk_viajes_vehiculo    FOREIGN KEY (id_vehiculo)      REFERENCES vehiculos(id_vehiculo),
    CONSTRAINT fk_viajes_estado      FOREIGN KEY (id_estado)        REFERENCES estados_viaje(id_estado),
    CONSTRAINT ck_viajes_pasajeros   CHECK (cantidad_pasajeros >= 0),
    CONSTRAINT ck_viajes_lat_origen  CHECK (latitud_origen  BETWEEN -90  AND 90),
    CONSTRAINT ck_viajes_lng_origen  CHECK (longitud_origen BETWEEN -180 AND 180),
    CONSTRAINT ck_viajes_lat_destino CHECK (latitud_destino  BETWEEN -90  AND 90),
    CONSTRAINT ck_viajes_lng_destino CHECK (longitud_destino BETWEEN -180 AND 180),
    CONSTRAINT ck_viajes_distancia   CHECK (distancia_km >= 0 AND km_extra >= 0 AND cargo_extra >= 0)
) ENGINE=InnoDB;

CREATE INDEX idx_viajes_conductor_fecha  ON viajes (id_conductor, fecha_servicio);
CREATE INDEX idx_viajes_estado           ON viajes (id_estado);
CREATE INDEX idx_viajes_empresa_fecha    ON viajes (id_empresa, fecha_servicio);
CREATE INDEX idx_viajes_solicitante_tipo ON viajes (id_solicitante, id_tipo_vehiculo, fecha_servicio);

-- ------------------------------------------------------------------------------
-- 9. HISTORIAL_ESTADOS_VIAJE (trazabilidad: cada cambio de estado = una fila)
--    La hora real de inicio es la fila EN_CURSO y la de término, la fila FINALIZADO.
-- ------------------------------------------------------------------------------
CREATE TABLE historial_estados_viaje (
    id_historial  BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_viaje      BIGINT       NOT NULL,
    id_estado     INT          NOT NULL COMMENT 'Estado al que pasó el viaje',
    id_usuario    BIGINT       NOT NULL COMMENT 'Quién hizo el cambio',
    fecha_cambio  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacion   VARCHAR(255) NULL,
    CONSTRAINT fk_hist_estado_viaje   FOREIGN KEY (id_viaje)   REFERENCES viajes(id_viaje),
    CONSTRAINT fk_hist_estado_estado  FOREIGN KEY (id_estado)  REFERENCES estados_viaje(id_estado),
    CONSTRAINT fk_hist_estado_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 10. HISTORIAL_REASIGNACIONES (cambio de conductor ordenado por el admin central)
-- ------------------------------------------------------------------------------
CREATE TABLE historial_reasignaciones (
    id_reasignacion        BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_viaje               BIGINT       NOT NULL,
    id_conductor_anterior  BIGINT       NOT NULL,
    id_conductor_nuevo     BIGINT       NOT NULL,
    id_admin               BIGINT       NOT NULL COMMENT 'Admin que ejecutó la reasignación',
    motivo                 VARCHAR(255) NOT NULL,
    fecha_reasignacion     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reasig_viaje    FOREIGN KEY (id_viaje)              REFERENCES viajes(id_viaje),
    CONSTRAINT fk_reasig_anterior FOREIGN KEY (id_conductor_anterior) REFERENCES conductores(id_conductor),
    CONSTRAINT fk_reasig_nuevo    FOREIGN KEY (id_conductor_nuevo)    REFERENCES conductores(id_conductor),
    CONSTRAINT fk_reasig_admin    FOREIGN KEY (id_admin)              REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 11. INCIDENCIAS (reportes del chofer en ruta; usa cámara y GPS del teléfono)
-- ------------------------------------------------------------------------------
CREATE TABLE incidencias (
    id_incidencia    BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_viaje         BIGINT        NOT NULL,
    id_conductor     BIGINT        NOT NULL COMMENT 'Chofer que reporta',
    tipo_incidencia  VARCHAR(30)   NOT NULL,
    descripcion      VARCHAR(500)  NOT NULL,
    foto_url         VARCHAR(255)  NULL COMMENT 'Foto tomada con la cámara del teléfono',
    latitud          DECIMAL(10,7) NULL COMMENT 'Ubicación GPS al momento del reporte',
    longitud         DECIMAL(10,7) NULL,
    fecha_registro   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_incidencias_viaje     FOREIGN KEY (id_viaje)     REFERENCES viajes(id_viaje),
    CONSTRAINT fk_incidencias_conductor FOREIGN KEY (id_conductor) REFERENCES conductores(id_conductor),
    CONSTRAINT ck_incidencias_tipo CHECK (tipo_incidencia IN
        ('COLISION', 'PANNE_MECANICA', 'CONGESTION_CRITICA', 'PASAJERO_NO_SE_PRESENTA', 'OTRO'))
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 12. COBROS (lo que Belmonte le cobra a cada empresa, por mes)
--     PAQUETE: compra del paquete mensual de cupos (se paga al inicio del mes).
--     EXTRAS : suma de los km extra de los viajes finalizados del mes (se emite al cierre).
-- ------------------------------------------------------------------------------
CREATE TABLE cobros (
    id_cobro       BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_empresa     BIGINT      NOT NULL,
    periodo        CHAR(7)     NOT NULL COMMENT 'Mes del cobro, formato AAAA-MM',
    tipo_cobro     VARCHAR(10) NOT NULL COMMENT 'PAQUETE o EXTRAS',
    monto          INT         NOT NULL COMMENT 'CLP, sin decimales',
    estado_pago    VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE',
    fecha_emision  DATE        NOT NULL,
    fecha_pago     DATE        NULL,
    CONSTRAINT fk_cobros_empresa  FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa),
    CONSTRAINT ck_cobros_periodo  CHECK (periodo LIKE '____-__'),
    CONSTRAINT ck_cobros_tipo     CHECK (tipo_cobro IN ('PAQUETE', 'EXTRAS')),
    CONSTRAINT ck_cobros_monto    CHECK (monto >= 0),
    CONSTRAINT ck_cobros_estado   CHECK (estado_pago IN ('PENDIENTE', 'PAGADO', 'ANULADO'))
) ENGINE=InnoDB;

CREATE INDEX idx_cobros_empresa_periodo ON cobros (id_empresa, periodo);

-- ------------------------------------------------------------------------------
-- 13. COBRO_CUPOS (detalle de un paquete: cuántos viajes trae de cada tipo)
-- ------------------------------------------------------------------------------
CREATE TABLE cobro_cupos (
    id_cobro_cupo     BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_cobro          BIGINT NOT NULL COMMENT 'Cobro de tipo PAQUETE',
    id_tipo_vehiculo  INT    NOT NULL,
    cantidad_viajes   INT    NOT NULL,
    CONSTRAINT uq_cobro_cupos_tipo   UNIQUE (id_cobro, id_tipo_vehiculo),
    CONSTRAINT fk_cobro_cupos_cobro  FOREIGN KEY (id_cobro)         REFERENCES cobros(id_cobro),
    CONSTRAINT fk_cobro_cupos_tipo   FOREIGN KEY (id_tipo_vehiculo) REFERENCES tipos_vehiculo(id_tipo_vehiculo),
    CONSTRAINT ck_cobro_cupos_cant   CHECK (cantidad_viajes > 0)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------------------
-- 14. ASIGNACIONES_CUPO (el admin de la empresa reparte los cupos entre sus trabajadores)
-- ------------------------------------------------------------------------------
CREATE TABLE asignaciones_cupo (
    id_asignacion     BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_cobro_cupo     BIGINT   NOT NULL COMMENT 'Línea del paquete de la que salen los cupos',
    id_usuario        BIGINT   NOT NULL COMMENT 'Trabajador que recibe los cupos',
    cantidad_viajes   INT      NOT NULL,
    fecha_asignacion  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_asignaciones_usuario UNIQUE (id_cobro_cupo, id_usuario),
    CONSTRAINT fk_asignaciones_cupo    FOREIGN KEY (id_cobro_cupo) REFERENCES cobro_cupos(id_cobro_cupo),
    CONSTRAINT fk_asignaciones_usuario FOREIGN KEY (id_usuario)    REFERENCES usuarios(id_usuario),
    CONSTRAINT ck_asignaciones_cant    CHECK (cantidad_viajes >= 0)
) ENGINE=InnoDB;


-- ==============================================================================
-- DATOS DE PRUEBA (100% ficticios)
-- ==============================================================================

-- Roles
INSERT INTO roles (id_rol, nombre, descripcion) VALUES
(1, 'ROLE_ADMIN',         'Administrador/recepcionista de Belmonte: asigna viajes y registra los cobros'),
(2, 'ROLE_ADMIN_EMPRESA', 'Administrador de una empresa cliente: reparte cupos y ve viajes y cobros de su empresa'),
(3, 'ROLE_SOLICITANTE',   'Trabajador de una empresa cliente que pide traslados con sus cupos'),
(4, 'ROLE_CONDUCTOR',     'Chofer de Belmonte: ve sus viajes, inicia/finaliza y navega con Maps/Waze');

-- Empresas cliente
INSERT INTO empresas (id_empresa, rut_empresa, razon_social, giro, direccion, telefono, email_contacto) VALUES
(1, '76482913-1', 'Minera Andes Sur SpA',              'Servicios mineros',    'Av. Apoquindo 4501, Las Condes',    '+56200000001', 'contacto@andessur.test'),
(2, '77215604-9', 'Consultora Pacífico Austral Ltda.', 'Asesorías de gestión', 'Av. Providencia 2133, Providencia', '+56200000002', 'contacto@pacificoaustral.test');

-- Usuarios (hash BCrypt de 'Password123*')
INSERT INTO usuarios (id_usuario, id_rol, id_empresa, rut, nombres, apellidos, email, password_hash, telefono) VALUES
(1, 1, NULL, '12845371-7', 'Valeria',  'Fuentes Rojas',   'valeria.admin@belmonte.test',          '$2a$10$D8gCvqakjMDI0LOtsUyLzuFzpyT2VzFLK7d7s4xBFcU2lsBvZzSlC', '+56900000001'),
(2, 2, 1,    '15392847-9', 'Tomás',    'Herrera Lagos',   'tomas.herrera@andessur.test',          '$2a$10$D8gCvqakjMDI0LOtsUyLzuFzpyT2VzFLK7d7s4xBFcU2lsBvZzSlC', '+56900000002'),
(3, 3, 1,    '16473920-1', 'Daniela',  'Muñoz Pino',      'daniela.munoz@andessur.test',          '$2a$10$D8gCvqakjMDI0LOtsUyLzuFzpyT2VzFLK7d7s4xBFcU2lsBvZzSlC', '+56900000003'),
(4, 2, 2,    '17284561-4', 'Ignacio',  'Vera Salinas',    'ignacio.vera@pacificoaustral.test',    '$2a$10$D8gCvqakjMDI0LOtsUyLzuFzpyT2VzFLK7d7s4xBFcU2lsBvZzSlC', '+56900000004'),
(5, 3, 2,    '18395026-6', 'Fernanda', 'Castro Ríos',     'fernanda.castro@pacificoaustral.test', '$2a$10$D8gCvqakjMDI0LOtsUyLzuFzpyT2VzFLK7d7s4xBFcU2lsBvZzSlC', '+56900000005'),
(6, 4, NULL, '13582947-1', 'Miguel',   'Rojas Contreras', 'miguel.rojas@belmonte.test',           '$2a$10$D8gCvqakjMDI0LOtsUyLzuFzpyT2VzFLK7d7s4xBFcU2lsBvZzSlC', '+56900000006'),
(7, 4, NULL, '14627385-8', 'Pedro',    'Soto Morales',    'pedro.soto@belmonte.test',             '$2a$10$D8gCvqakjMDI0LOtsUyLzuFzpyT2VzFLK7d7s4xBFcU2lsBvZzSlC', '+56900000007'),
(8, 4, NULL, '19473826-9', 'Jorge',    'Alarcón Bravo',   'jorge.alarcon@belmonte.test',          '$2a$10$D8gCvqakjMDI0LOtsUyLzuFzpyT2VzFLK7d7s4xBFcU2lsBvZzSlC', '+56900000008'),
(9, 4, NULL, '15836204-K', 'Luis',     'Carrasco Vega',   'luis.carrasco@belmonte.test',          '$2a$10$D8gCvqakjMDI0LOtsUyLzuFzpyT2VzFLK7d7s4xBFcU2lsBvZzSlC', '+56900000009');

-- Conductores (datos propios del chofer)
INSERT INTO conductores (id_conductor, clase_licencia, vencimiento_licencia, foto_url, disponible) VALUES
(6, 'A2', '2028-03-15', 'conductores/6.jpg', FALSE),
(7, 'A2', '2027-11-30', 'conductores/7.jpg', FALSE),
(8, 'A3', '2029-06-01', 'conductores/8.jpg', TRUE),
(9, 'A4', '2028-08-20', 'conductores/9.jpg', TRUE);

-- Tipos de vehículo (tarifas de km extra ficticias)
INSERT INTO tipos_vehiculo (id_tipo_vehiculo, codigo, nombre, descripcion, km_incluidos, valor_km_extra) VALUES
(1, 'AUTO',   'Auto ejecutivo', 'Traslado de personas, hasta 4 pasajeros', 40, 1100),
(2, 'VAN',    'Van',            'Traslado de grupos, hasta 8 pasajeros',   40, 1600),
(3, 'CAMION', 'Camión',         'Traslado de materiales y carga',          60, 2500);

-- Vehículos
INSERT INTO vehiculos (id_vehiculo, id_tipo_vehiculo, patente, marca, modelo, anio, color, capacidad_pasajeros) VALUES
(1, 1, 'KR-TW-21', 'Toyota',        'Camry',       2023, 'Gris plata',   4),
(2, 1, 'LK-JH-44', 'Hyundai',       'Santa Fe',    2023, 'Gris grafito', 4),
(3, 2, 'PX-RT-89', 'Mercedes-Benz', 'Vito Tourer', 2024, 'Negro',        8),
(4, 3, 'JT-KL-52', 'Hyundai',       'HD65',        2022, 'Blanco',       2);

-- Estados del viaje
INSERT INTO estados_viaje (id_estado, codigo, nombre, descripcion, orden) VALUES
(1, 'PENDIENTE',  'Pendiente',  'Solicitud creada, sin conductor asignado',   1),
(2, 'ASIGNADO',   'Asignado',   'Conductor y vehículo asignados por el admin', 2),
(3, 'EN_CURSO',   'En curso',   'El conductor inició el viaje',                3),
(4, 'FINALIZADO', 'Finalizado', 'El conductor terminó el viaje',               4),
(5, 'CANCELADO',  'Cancelado',  'Solicitud anulada antes de iniciar el viaje', 5);

-- Viajes (cubren los 5 estados, los 3 tipos de vehículo y ambas empresas)
--   Viaje 107: camión, 76,18 km con tope de 60 km -> 16,18 km extra x $2.500 = $40.450
INSERT INTO viajes (id_viaje, id_solicitante, id_empresa, id_tipo_vehiculo, id_conductor, id_vehiculo, id_estado,
                    nombre_pasajero, telefono_pasajero, cantidad_pasajeros, descripcion_carga,
                    fecha_servicio, hora_servicio, direccion_origen, latitud_origen, longitud_origen,
                    direccion_destino, latitud_destino, longitud_destino, numero_vuelo, observaciones,
                    distancia_km, km_extra, cargo_extra, creado_en) VALUES
(101, 3, 1, 1, NULL, NULL, 1, 'Andrea Lagos Vidal',          '+56900000101', 1, NULL,
     '2026-10-15', '07:30:00', 'Isidora Goyenechea 3000, Las Condes',        -33.4149000, -70.5986000,
     'Aeropuerto AMB, Pudahuel',                     -33.3930000, -70.7944000, 'LA452', 'Equipaje grande',
     18.34, 0, 0, '2026-10-09 18:20:00'),
(102, 5, 2, 1, 6,    1,    2, 'Rodrigo Paredes Núñez',       '+56900000102', 1, NULL,
     '2026-10-15', '09:00:00', 'Av. Apoquindo 4501, Las Condes',             -33.4116000, -70.5843000,
     'Av. Vitacura 2939, Las Condes',                -33.4140000, -70.6020000, NULL, NULL,
     1.66, 0, 0, '2026-10-09 19:10:00'),
(103, 3, 1, 1, 7,    2,    3, 'Claudia Ortiz Medina',        '+56900000103', 2, NULL,
     '2026-10-10', '10:15:00', 'Aeropuerto AMB, Terminal Internacional',     -33.3930000, -70.7944000,
     'Av. Kennedy 9001, Las Condes',                 -33.3920000, -70.5460000, 'H2502', NULL,
     23.06, 0, 0, '2026-10-09 12:00:00'),
(104, 5, 2, 1, 6,    1,    4, 'Sebastián Fuentes Toro',      '+56900000104', 1, NULL,
     '2026-10-08', '15:00:00', 'Av. Libertador B. O''Higgins 1302, Santiago', -33.4440000, -70.6530000,
     'Av. Providencia 2133, Providencia',            -33.4230000, -70.6100000, NULL, NULL,
     4.62, 0, 0, '2026-10-07 11:30:00'),
(105, 3, 1, 2, 8,    3,    4, 'Equipo Proyecto Norte',       '+56900000105', 5, NULL,
     '2026-10-09', '18:00:00', 'Av. Apoquindo 4501, Las Condes',             -33.4116000, -70.5843000,
     'Aeropuerto AMB, Pudahuel',                     -33.3930000, -70.7944000, 'JA120', 'Grupo de 5 personas',
     19.61, 0, 0, '2026-10-08 10:00:00'),
(106, 5, 2, 1, NULL, NULL, 5, 'Rodrigo Paredes Núñez',       '+56900000102', 1, NULL,
     '2026-10-12', '08:00:00', 'Av. Providencia 2133, Providencia',          -33.4230000, -70.6100000,
     'Av. Libertador B. O''Higgins 3850, Estación Central', -33.4540000, -70.6880000, NULL, NULL,
     8.02, 0, 0, '2026-10-09 09:00:00'),
(107, 3, 1, 3, 9,    4,    4, 'Recibe: Marcos Díaz Fuentes', '+56900000107', 0, 'Repuestos y herramientas para faena (800 kg)',
     '2026-10-06', '08:00:00', 'Av. Pajaritos 4500, Maipú',                  -33.4850000, -70.7550000,
     'Av. Libertador B. O''Higgins 500, Rancagua',   -34.1700000, -70.7400000, NULL, 'Cargar en bodega 3',
     76.18, 16.18, 40450, '2026-10-04 10:00:00');

-- Historial de estados (coherente con el estado actual de cada viaje)
INSERT INTO historial_estados_viaje (id_viaje, id_estado, id_usuario, fecha_cambio, observacion) VALUES
(101, 1, 3, '2026-10-09 18:20:00', 'Solicitud creada desde la app'),
(102, 1, 5, '2026-10-09 19:10:00', 'Solicitud creada desde la app'),
(102, 2, 1, '2026-10-09 20:00:00', 'Asignado a Miguel Rojas / KR-TW-21'),
(103, 1, 3, '2026-10-09 12:00:00', 'Solicitud creada desde la app'),
(103, 2, 1, '2026-10-09 13:00:00', 'Asignado a Jorge Alarcón / KR-TW-21'),
(103, 3, 7, '2026-10-10 10:20:00', 'Viaje iniciado por el conductor de reemplazo'),
(104, 1, 5, '2026-10-07 11:30:00', 'Solicitud creada desde la app'),
(104, 2, 1, '2026-10-07 12:00:00', 'Asignado a Miguel Rojas / KR-TW-21'),
(104, 3, 6, '2026-10-08 15:05:00', NULL),
(104, 4, 6, '2026-10-08 15:42:00', NULL),
(105, 1, 3, '2026-10-08 10:00:00', 'Solicitud creada desde la app'),
(105, 2, 1, '2026-10-08 11:00:00', 'Asignado a Jorge Alarcón / PX-RT-89'),
(105, 3, 8, '2026-10-09 18:03:00', NULL),
(105, 4, 8, '2026-10-09 19:01:00', NULL),
(106, 1, 5, '2026-10-09 09:00:00', 'Solicitud creada desde la app'),
(106, 5, 5, '2026-10-09 16:30:00', 'Reunión suspendida por el cliente'),
(107, 1, 3, '2026-10-04 10:00:00', 'Solicitud creada desde la app'),
(107, 2, 1, '2026-10-04 15:00:00', 'Asignado a Luis Carrasco / JT-KL-52'),
(107, 3, 9, '2026-10-06 08:05:00', NULL),
(107, 4, 9, '2026-10-06 10:10:00', NULL);

-- Incidencias (con foto y GPS del teléfono)
INSERT INTO incidencias (id_incidencia, id_viaje, id_conductor, tipo_incidencia, descripcion, foto_url, latitud, longitud, fecha_registro) VALUES
(1, 103, 8, 'PANNE_MECANICA',     'Falla de batería camino al aeropuerto; el vehículo no enciende.', 'incidencias/1.jpg', -33.4010000, -70.7300000, '2026-10-10 08:30:00'),
(2, 105, 8, 'CONGESTION_CRITICA', 'Accidente en Costanera Norte; retraso estimado de 20 minutos.',   NULL,                -33.4105000, -70.6150000, '2026-10-09 18:25:00');

-- Reasignación generada por la incidencia 1 (mismo tipo de vehículo: auto)
INSERT INTO historial_reasignaciones (id_viaje, id_conductor_anterior, id_conductor_nuevo, id_admin, motivo, fecha_reasignacion) VALUES
(103, 8, 7, 1, 'Panne mecánica informada por el conductor (incidencia #1). Se cambia también el vehículo por otro auto: LK-JH-44.', '2026-10-10 08:40:00');

-- Cobros: paquetes de octubre (ficticios). El cobro EXTRAS de octubre se emite al cierre del mes.
INSERT INTO cobros (id_cobro, id_empresa, periodo, tipo_cobro, monto, estado_pago, fecha_emision, fecha_pago) VALUES
(1, 1, '2026-10', 'PAQUETE', 5000000, 'PAGADO', '2026-10-01', '2026-10-02'),
(2, 2, '2026-10', 'PAQUETE', 1800000, 'PAGADO', '2026-10-01', '2026-10-03');

-- Detalle de cada paquete: cupos por tipo de vehículo
INSERT INTO cobro_cupos (id_cobro_cupo, id_cobro, id_tipo_vehiculo, cantidad_viajes) VALUES
(1, 1, 1, 30),   -- Minera Andes Sur: 30 viajes en auto
(2, 1, 2, 20),   --                   20 viajes en van
(3, 1, 3, 10),   --                   10 viajes en camión
(4, 2, 1, 15),   -- Consultora Pacífico: 15 viajes en auto
(5, 2, 2,  5);   --                       5 viajes en van

-- Reparto de cupos que hizo cada admin de empresa
INSERT INTO asignaciones_cupo (id_cobro_cupo, id_usuario, cantidad_viajes, fecha_asignacion) VALUES
(1, 3, 10, '2026-10-02 09:00:00'),   -- Daniela: 10 en auto
(2, 3,  5, '2026-10-02 09:00:00'),   -- Daniela:  5 en van
(3, 3,  3, '2026-10-02 09:00:00'),   -- Daniela:  3 en camión
(1, 2,  5, '2026-10-02 09:05:00'),   -- Tomás:    5 en auto
(4, 5,  8, '2026-10-03 10:00:00'),   -- Fernanda: 8 en auto
(5, 5,  2, '2026-10-03 10:00:00'),   -- Fernanda: 2 en van
(4, 4,  3, '2026-10-03 10:05:00');   -- Ignacio:  3 en auto


-- ==============================================================================
-- CONSULTAS DE VERIFICACIÓN (lo que cada rol ve en la app)
-- En microservicios, cada parte la resuelve el servicio dueño de esas tablas.
-- ==============================================================================

-- A) ADMIN DE EMPRESA (Tomás, empresa 1): viajes de SU empresa (ms-viajes)
SELECT v.id_viaje, v.fecha_servicio, t.codigo AS tipo, v.nombre_pasajero, e.codigo AS estado
FROM viajes v
JOIN estados_viaje e  ON e.id_estado        = v.id_estado
JOIN tipos_vehiculo t ON t.id_tipo_vehiculo = v.id_tipo_vehiculo
WHERE v.id_empresa = 1
ORDER BY v.fecha_servicio, v.hora_servicio;

-- B) ADMIN DE EMPRESA: cupos de octubre por tipo, contratados vs repartidos (ms-cobros)
SELECT t.codigo AS tipo,
       cc.cantidad_viajes AS contratados,
       COALESCE((SELECT SUM(a.cantidad_viajes) FROM asignaciones_cupo a
                 WHERE a.id_cobro_cupo = cc.id_cobro_cupo), 0) AS repartidos
FROM cobros c
JOIN cobro_cupos cc   ON cc.id_cobro        = c.id_cobro
JOIN tipos_vehiculo t ON t.id_tipo_vehiculo = cc.id_tipo_vehiculo
WHERE c.id_empresa = 1 AND c.periodo = '2026-10'
  AND c.tipo_cobro = 'PAQUETE' AND c.estado_pago = 'PAGADO';

-- C) TRABAJADOR (Daniela, id 3): cupos disponibles en octubre = asignados - usados
--    (asignados: ms-cobros; usados = viajes no cancelados del mes: ms-viajes)
SELECT t.codigo AS tipo, asig.asignados,
       COALESCE(uso.usados, 0)                  AS usados,
       asig.asignados - COALESCE(uso.usados, 0) AS disponibles
FROM (SELECT cc.id_tipo_vehiculo, SUM(a.cantidad_viajes) AS asignados
      FROM asignaciones_cupo a
      JOIN cobro_cupos cc ON cc.id_cobro_cupo = a.id_cobro_cupo
      JOIN cobros c       ON c.id_cobro       = cc.id_cobro
      WHERE a.id_usuario = 3 AND c.periodo = '2026-10' AND c.estado_pago = 'PAGADO'
      GROUP BY cc.id_tipo_vehiculo) asig
JOIN tipos_vehiculo t ON t.id_tipo_vehiculo = asig.id_tipo_vehiculo
LEFT JOIN (SELECT id_tipo_vehiculo, COUNT(*) AS usados
           FROM viajes
           WHERE id_solicitante = 3 AND id_estado <> 5
             AND DATE_FORMAT(fecha_servicio, '%Y-%m') = '2026-10'
           GROUP BY id_tipo_vehiculo) uso ON uso.id_tipo_vehiculo = asig.id_tipo_vehiculo;

-- D) CONDUCTOR (Miguel, id 6): sus viajes con datos para navegar (ms-viajes)
SELECT v.id_viaje, v.fecha_servicio, v.hora_servicio, e.codigo AS estado, v.nombre_pasajero,
       v.direccion_origen, v.latitud_origen, v.longitud_origen,
       v.direccion_destino, v.latitud_destino, v.longitud_destino, v.numero_vuelo
FROM viajes v
JOIN estados_viaje e ON e.id_estado = v.id_estado
WHERE v.id_conductor = 6
ORDER BY v.fecha_servicio, v.hora_servicio;

-- E) ADMIN CENTRAL: solicitudes pendientes de asignar, con el tipo pedido (ms-viajes)
SELECT v.id_viaje, v.fecha_servicio, v.hora_servicio, t.codigo AS tipo, v.nombre_pasajero, v.id_empresa
FROM viajes v
JOIN tipos_vehiculo t ON t.id_tipo_vehiculo = v.id_tipo_vehiculo
WHERE v.id_estado = 1;

-- F) CIERRE DE MES: km extra de octubre por empresa -> monto del cobro EXTRAS (ms-viajes -> ms-cobros)
SELECT id_empresa, SUM(km_extra) AS km_extra, SUM(cargo_extra) AS monto_extras
FROM viajes
WHERE id_estado = 4 AND cargo_extra > 0
  AND DATE_FORMAT(fecha_servicio, '%Y-%m') = '2026-10'
GROUP BY id_empresa;

-- G) Línea de tiempo de un viaje (hora real de inicio y fin)
SELECT h.fecha_cambio, e.codigo AS estado, CONCAT(u.nombres, ' ', u.apellidos) AS realizado_por, h.observacion
FROM historial_estados_viaje h
JOIN estados_viaje e ON e.id_estado  = h.id_estado
JOIN usuarios u      ON u.id_usuario = h.id_usuario
WHERE h.id_viaje = 107
ORDER BY h.fecha_cambio;
