
DROP DATABASE IF EXISTS barberia_db;
CREATE DATABASE barberia_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE barberia_db;


CREATE TABLE usuarios (
  id             INT AUTO_INCREMENT PRIMARY KEY,
  nombre         VARCHAR(100) NOT NULL,
  email          VARCHAR(120) NOT NULL,
  password_hash  VARCHAR(255) NOT NULL,
  telefono       VARCHAR(20),
  rol            ENUM('CLIENTE','BARBERO','ADMIN') NOT NULL DEFAULT 'CLIENTE',
  activo         BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_usuarios_email UNIQUE (email)
) ENGINE=InnoDB;


CREATE TABLE servicios (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  nombre       VARCHAR(100) NOT NULL,
  descripcion  VARCHAR(255),
  duracion_min INT NOT NULL,
  precio       DECIMAL(10,2) NOT NULL,
  activo       BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT uk_servicios_nombre UNIQUE (nombre),
  CONSTRAINT ck_servicios_duracion CHECK (duracion_min > 0),
  CONSTRAINT ck_servicios_precio CHECK (precio >= 0)
) ENGINE=InnoDB;


CREATE TABLE cortes (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  nombre      VARCHAR(100) NOT NULL,
  descripcion VARCHAR(255),
  imagen_url  VARCHAR(255),
  overlay_url VARCHAR(255),
  servicio_id INT NOT NULL,
  activo      BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_cortes_servicio FOREIGN KEY (servicio_id)
    REFERENCES servicios (id)
) ENGINE=InnoDB;


CREATE TABLE horarios_barbero (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  barbero_id  INT NOT NULL,
  dia_semana  TINYINT NOT NULL,           -- 1=lunes ... 7=domingo
  hora_inicio TIME NOT NULL,
  hora_fin    TIME NOT NULL,
  CONSTRAINT fk_horarios_barbero FOREIGN KEY (barbero_id)
    REFERENCES usuarios (id) ON DELETE CASCADE,
  CONSTRAINT uk_horarios_bloque UNIQUE (barbero_id, dia_semana, hora_inicio),
  CONSTRAINT ck_horarios_dia CHECK (dia_semana BETWEEN 1 AND 7),
  CONSTRAINT ck_horarios_horas CHECK (hora_fin > hora_inicio)
) ENGINE=InnoDB;


CREATE TABLE citas (
  id                INT AUTO_INCREMENT PRIMARY KEY,
  cliente_id        INT NOT NULL,
  barbero_id        INT NOT NULL,
  servicio_id       INT NOT NULL,
  corte_id          INT NULL,
  fecha_hora_inicio DATETIME NOT NULL,
  fecha_hora_fin    DATETIME NOT NULL,
  estado            ENUM('PENDIENTE','CONFIRMADA','COMPLETADA','CANCELADA')
                    NOT NULL DEFAULT 'PENDIENTE',
  notas             VARCHAR(255),
  fecha_creacion    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_citas_cliente  FOREIGN KEY (cliente_id)  REFERENCES usuarios (id),
  CONSTRAINT fk_citas_barbero  FOREIGN KEY (barbero_id)  REFERENCES usuarios (id),
  CONSTRAINT fk_citas_servicio FOREIGN KEY (servicio_id) REFERENCES servicios (id),
  CONSTRAINT fk_citas_corte    FOREIGN KEY (corte_id)    REFERENCES cortes (id)
    ON DELETE SET NULL,
  CONSTRAINT ck_citas_fechas CHECK (fecha_hora_fin > fecha_hora_inicio)
) ENGINE=InnoDB;

CREATE INDEX idx_citas_barbero_inicio ON citas (barbero_id, fecha_hora_inicio);
CREATE INDEX idx_citas_cliente        ON citas (cliente_id);


CREATE TABLE cotizaciones (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  cliente_id INT NOT NULL,
  fecha      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  total      DECIMAL(10,2) NOT NULL DEFAULT 0,
  estado     ENUM('PENDIENTE','ACEPTADA','RECHAZADA') NOT NULL DEFAULT 'PENDIENTE',
  CONSTRAINT fk_cotizaciones_cliente FOREIGN KEY (cliente_id)
    REFERENCES usuarios (id)
) ENGINE=InnoDB;

CREATE TABLE cotizacion_detalle (
  id              INT AUTO_INCREMENT PRIMARY KEY,
  cotizacion_id   INT NOT NULL,
  servicio_id     INT NOT NULL,
  cantidad        INT NOT NULL DEFAULT 1,
  precio_unitario DECIMAL(10,2) NOT NULL,
  CONSTRAINT fk_detalle_cotizacion FOREIGN KEY (cotizacion_id)
    REFERENCES cotizaciones (id) ON DELETE CASCADE,
  CONSTRAINT fk_detalle_servicio FOREIGN KEY (servicio_id)
    REFERENCES servicios (id),
  CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB;

CREATE TABLE resenas (
  id             INT AUTO_INCREMENT PRIMARY KEY,
  cita_id        INT NOT NULL,
  calificacion   TINYINT NOT NULL,
  comentario     TEXT,
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_resenas_cita FOREIGN KEY (cita_id) REFERENCES citas (id),
  CONSTRAINT uk_resenas_cita UNIQUE (cita_id),
  CONSTRAINT ck_resenas_calificacion CHECK (calificacion BETWEEN 1 AND 5)
) ENGINE=InnoDB;


INSERT INTO servicios (nombre, descripcion, duracion_min, precio) VALUES
  ('Corte clásico',   'Corte con tijera y máquina',          30, 150.00),
  ('Corte Eskolar',   'Corte más arreglo de barba',          50, 230.00);


