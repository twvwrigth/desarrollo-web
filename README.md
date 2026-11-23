# Sistema gestor de citas para barbería

Sistema web para gestionar citas de una barbería: usuarios con roles
(CLIENTE, BARBERO, ADMIN), catálogo de servicios y cortes, horarios y
disponibilidad de barberos, citas, cotizaciones y reseñas.

Proyecto de la materia de Desarrollo Web, Universidad Veracruzana.
Entrega final: 23 de noviembre de 2026.

## Equipo

| Integrante | Módulo |
|---|---|
| José Emmanuel Martínez Galicia | Reseñas e integración general (index, navegación, estilos globales, funciones Fetch comunes) |
| Erick Iray Gutiérrez del Ángel | Servicios, cortes, citas y cotizaciones |
| José Luis Lucas Landa | Usuarios, horarios, login y registro |

Docente: José Rafael Rojano Caceres.

Repositorio: https://github.com/twvwrigth/desarrollo-web

## Tecnologías

- Backend: Java 17, Spring Boot 4.1.2, Spring Data JPA (Hibernate), MySQL 8.
- Frontend: HTML, CSS y JavaScript con Fetch (sin framework).
- La contraseña se guarda con BCrypt (`spring-security-crypto`). No hay
  Spring Security completo ni JWT: el frontend guarda la sesión en
  `sessionStorage` y manda el header `X-Usuario-Id` en las operaciones que
  lo requieren. Es una limitación conocida del proyecto, documentada para
  la defensa.

## Estructura del repo

```
README.md
demo/                          Backend Spring Boot (el pom.xml está aquí, no en la raíz)
  src/main/java/com/example/demo/
    DemoApplication.java       Arranque
    Configuracion.java         Crea el usuario demo admin al arrancar
    Usuario.java, Rol.java, HorarioBarbero.java
    Servicio.java, Corte.java, Cita.java, EstadoCita.java
    Cotizacion.java, CotizacionDetalle.java, EstadoCotizacion.java
    Resena.java
    IUsuario.java, IHorarioBarbero.java, IServicio.java, ICorte.java,
    ICita.java, ICotizacion.java, ICotizacionDetalle.java, IResena.java
    UsuarioControlador.java, HorarioControlador.java,
    ServicioControlador.java, CorteControlador.java,
    CitaControlador.java, CotizacionControlador.java,
    ResenaControlador.java
  src/main/resources/application.properties
frontend/                      Frontend (HTML/CSS/JS)
  index.html                   Tabla de servicios y tabla de cortes
  agendar.html                 Agendar cita y mis citas
  resenas.html                 Reseñas por barbero + resumen
  servicios.html               Servicios y cortes (admin)
  cotizaciones.html            Cotizaciones
  login.html, registro.html    Sesión y registro
  usuarios.html, horarios.html Usuarios (admin) y horarios
  css/styles.css
  js/app.js                    Funciones comunes (apiFetch, sesión, mensajes)
  js/common.js, js/resenas.js  Funciones comunes y módulo de reseñas
  js/citas.js                  Cambio de estado de cita
  js/servicios.js, js/cotizaciones.js, js/usuarios.js, js/horarios.js
documentacion/
  diseno/                      Requerimientos, endpoints y diseño
  reportes/                    Reportes de avance (Entregable 1-4)
  IAG/                         Reportes de uso de IA por integrante (r-ia-dev)
```

## Cómo correrlo

### Requisitos

- JDK 17 y MySQL 8 corriendo.
- Base de datos `barberia_db` creada y usuario con privilegios sobre ella
  (en el equipo se usa el usuario `listi`):

```sql
CREATE DATABASE IF NOT EXISTS barberia_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES ON barberia_db.* TO 'listi'@'%';
FLUSH PRIVILEGES;
```

### 1. Backend (puerto 8081)

En `demo/src/main/resources/application.properties` pon la IP del host de
tu propia máquina en la URL de MySQL. La que trae el repo es la de un
integrante; cada quien usa la suya. No subas el archivo con tu IP al repo.

Desde la carpeta `demo/` (ahí está el `pom.xml`, no en la raíz):

```bash
cd demo
./mvnw spring-boot:run
```

El backend queda en http://localhost:8081. Las tablas se crean solas
(`spring.jpa.hibernate.ddl-auto=update`).

### 2. Frontend (puerto 5500)

Desde la carpeta `frontend/`, con el backend corriendo:

```bash
python3 -m http.server 5500
```

Entrar a http://localhost:5500. No abrir las páginas como `file://`,
porque el Fetch al backend no funciona así.

## Acceso demo

Al arrancar, el backend crea el usuario demo admin si no existe:

- Correo: `admin@barberia.com`
- Contraseña: `admin123`

Entrar por `login.html`. La sesión es por pestaña (`sessionStorage`): al
abrir otra pestaña hay que volver a entrar. La navegación y las páginas
muestran opciones según el rol, y las operaciones protegidas devuelven 401
sin sesión y 403 sin permiso.

> La contraseña demo debe cambiarse o eliminarse antes de la entrega final.

## Endpoints principales

Base: `http://localhost:8081/api`. Los errores usan el formato
`{"error":"...","mensaje":"..."}`.

| Recurso | Endpoints |
|---|---|
| Usuarios | POST /api/usuarios, GET /api/usuarios?rol=&activos=, GET /api/usuarios/{id}, PUT /api/usuarios/{id}, PUT /api/usuarios/{id}/estado, DELETE /api/usuarios/{id} (baja lógica), POST /api/auth/login |
| Horarios | POST /api/barberos/{id}/horarios, GET /api/barberos/{id}/horarios, PUT /api/horarios/{id}, DELETE /api/horarios/{id} |
| Servicios | POST /api/servicios, GET /api/servicios, GET /api/servicios/{id}, PUT /api/servicios/{id}, DELETE /api/servicios/{id} (admin, baja lógica) |
| Cortes | POST /api/cortes, GET /api/cortes, GET /api/cortes/{id}, GET /api/servicios/{id}/cortes, PUT /api/cortes/{id}, DELETE /api/cortes/{id} (admin, baja lógica) |
| Citas | POST /api/citas, GET /api/citas?clienteId=&barberoId=&fecha=&estado=, GET /api/citas/{id}, GET /api/clientes/{id}/citas, GET /api/barberos/{id}/citas, GET /api/citas/disponibilidad, PUT /api/citas/{id} (reprogramar), PUT /api/citas/{id}/estado, DELETE /api/citas/{id} (cancela) |
| Cotizaciones | POST /api/cotizaciones, GET /api/cotizaciones/{id}, GET /api/cotizaciones?clienteId=, GET /api/clientes/{id}/cotizaciones, PUT /api/cotizaciones/{id}/estado, DELETE /api/cotizaciones/{id} (solo si PENDIENTE) |
| Reseñas | POST /api/resenas, GET /api/resenas?barberoId=, GET /api/resenas/resumen?barberoId=, PUT /api/resenas/{id}, DELETE /api/resenas/{id} |

Reglas principales:

- Solo se puede reseñar una cita COMPLETADA, una reseña por cita y solo el
  cliente de esa cita. Las bajas de usuarios, servicios y cortes son
  lógicas (`activo=false`); la reseña y el horario se eliminan de forma
  física; la cotización solo se elimina si está PENDIENTE; el DELETE de
  cita la marca CANCELADA.
- La cita nace PENDIENTE y cambia PENDIENTE → CONFIRMADA → COMPLETADA
  (o CANCELADA); el cambio de estado requiere el header `X-Usuario-Id`.
- Crear servicios y cortes requiere ser ADMIN (header `X-Usuario-Id`).
- Las fechas se mandan en ISO con 'T' (`2026-11-20T11:00:00`); el backend
  usa `LocalDateTime` sin formato personalizado.
- El POST de cita valida traslape del barbero y devuelve 409 si choca.

## Pruebas

- Pruebas con cURL de reseñas (E2: POST/GET; E3: resumen, PUT, DELETE y
  persistencia tras reinicio): 11/11 casos OK contra el backend real.
  Scripts y casos en `documentacion/IAG/MartinezGaliciaJoseEmmanuel/Uso_02/`
  y en `docs/` del repo cuando se suban.
- Pruebas de frontend en navegador (localhost:5500 contra el backend en
  8081): agendar, reprogramar, cambiar estado, crear/editar/eliminar
  reseña y resumen por barbero.

## Releases

Releases en GitHub (V1 Diseño inicial, V2 API básico, V3 Backend completo,
V4 Desarrollo frontend — sistema integrado).

## Documentación

- Reportes de avance: `documentacion/reportes/ReporteEntregable1.md` a
  `ReporteEntregable4.md`.
- Diseño y endpoints: `documentacion/diseno/`.
- Uso de IA generativa por integrante: `documentacion/IAG/` (un reporte
  `r-ia-dev` por uso, con prompt, respuesta, commits y reflexión).
