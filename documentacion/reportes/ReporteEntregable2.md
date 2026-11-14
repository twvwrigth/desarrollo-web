# Reporte de Avance del Proyecto

**Equipo:** José Emmanuel Martínez Galicia (@tachi), Erick Iray Gutiérrez del Ángel, José Luis Lucas Landa
**Fecha:** 13/11/2026
**Número de reporte:** 2
**Repositorio GitHub:** https://github.com/twvwrigth/desarrollo-web

---

# 1. Actividades realizadas

Describir las actividades desarrolladas desde el último reporte.

Desde el Reporte 1 (11/11/2026), que correspondió únicamente a la fase de diseño sin funcionalidad ejecutable, en este periodo se implementó el backend E2 y se integró y probó en WSL:

- Se creó el proyecto Spring Boot con el backend de usuarios y horarios, login con BCrypt y los endpoints POST/GET correspondientes (José Luis).
- Se implementaron las entidades, los repositorios y los endpoints POST/GET de servicios, cortes, cotizaciones y citas (Erick).
- Se implementaron POST y GET de reseñas y el esqueleto HTML/CSS del frontend con las páginas de inicio, agendar y reseñas (José Emmanuel).
- Se integró el trabajo de los tres en el repositorio y se probó el flujo completo de punta a punta con cURL contra el backend corriendo en WSL (puerto 8081) y MySQL en Windows (base `barberia_db`).
- Se corrigieron los errores que impidieron el arranque y la integración: el método `findByCotizacionId` de `ICotizacionDetalle` y la corrección de `CotizacionControlador`.

---

# 2. Evidencias de trabajo

| Actividad | Integrante responsable | Enlace a commit |
| --- | --- | --- |
| Creación del proyecto con Spring Initializer, backend de usuarios y horarios con login POST y GET, BCrypt | José Luis Lucas Landa | https://github.com/twvwrigth/desarrollo-web/commit/fe82a7042f81e8f079a3946b6463eb85c2059b20 |
| Entidades y repositorios de servicios, cortes y cotizaciones, POST y GET de citas | Erick Iray Gutiérrez del Ángel | https://github.com/twvwrigth/desarrollo-web/commit/2a6af524bb36a82353e408bf273b2eed4929e8bd |
| Corrección de CotizacionControlador.java | Erick Iray Gutiérrez del Ángel | https://github.com/twvwrigth/desarrollo-web/commit/27b38dc0567d802b24f5dd83ca6ce639294f577e |
| Correción ICotizaciónDetalle | Erick Iray Gutiérrez del Ángel | https://github.com/twvwrigth/desarrollo-web/commit/f95e0f5cdacb582449d972a9b6a1cbb9055014fb |
| E2 resenas: entidad, repositorio y controlador POST/GET + esqueleto frontend | José Emmanuel Martínez Galicia | https://github.com/twvwrigth/desarrollo-web/commit/4109e6bba805000fb9d12be2d60c8c72876d7885 |

---

# 3. Problemas encontrados

Describir los problemas técnicos encontrados y cómo fueron solucionados.

- **El backend no arrancaba por&#32;`ICotizacionDetalle`:** Spring Data fallaba al crear el repositorio con el error `No property 'cotizacionId' found for type 'CotizacionDetalle'`. `CotizacionDetalle` no tiene campo `cotizacion` (solo servicio, cantidad y precioUnitario); la llave vive en `Cotizacion` con `@OneToMany`. El método `findByCotizacionId` no era usado por ningún controlador, así que se borró esa línea en el commit `f95e0f5`.
- **MySQL inalcanzable y acceso denegado:** el `application.properties` del repo traía la IP del host de quien lo subió (primero 172.29.192.1, luego 172.20.96.1), inalcanzable desde el WSL de Emmanuel. Se cambió localmente a su IP de host, 172.22.128.1, y se otorgaron privilegios al usuario `listi` sobre `barberia_db` (error 1044). Ese cambio es solo local y no se sube al repo.
- **El commit&#32;`f95e0f5`&#32;trajo cambios de finales de línea en 15 archivos** (\~587 inserciones / 592 eliminaciones). No afecta la compilación, pero ensucia el diff; se deja constancia para que el equipo lo tome en cuenta en los siguientes commits.

---

# 4. Avance funcional

Indicar qué funcionalidades ya se encuentran operativas y cuáles están pendientes.

**Operativas (probadas con cURL contra el backend real, 13 nov 2026, \~22:42-22:47):**

- Usuarios: POST /api/usuarios, GET /api/usuarios/{id}, POST /api/auth/login.
- Horarios: POST y GET /api/barberos/{id}/horarios.
- Servicios: POST y GET /api/servicios, GET /api/servicios/{id}.
- Cortes: POST y GET /api/cortes, GET /api/cortes/{id}, GET /api/servicios/{id}/cortes.
- Cotizaciones: POST y GET /api/cotizaciones/{id}, GET /api/clientes/{id}/cotizaciones.
- Citas: POST /api/citas (nace en PENDIENTE), GET /api/citas/{id}, GET por cliente y por barbero.
- Reseñas: POST /api/resenas y GET /api/resenas?barberoId=, GET /api/resenas.

Prueba de flujo completo (base limpia): login admin id 1, cliente id 2, barbero id 3, horario viernes 09:00-18:00, servicio id 1 (Corte clásico, $150, 30 min), corte id 1 (Fade), cotización id 1 (total 150.00), cita id 1 PENDIENTE 2026-11-20 11:00-11:30. En MySQL se puso la cita en COMPLETADA y luego POST /api/resenas (citaId 1, calificación 5, cliente 2) devolvió la reseña id 1, y GET /api/resenas?barberoId=3 la devolvió con 200.

**Pendientes:**

- Casos de error de reseñas por correr y documentar: duplicada (409), calificación 6 (400), cita inexistente (404), otro cliente (403) y cita PENDIENTE (409).
- Prueba del frontend en el navegador contra el backend (las páginas y el JavaScript ya están subidos, solo se probaron con cURL).
- Funcionalidades E3: PUT/DELETE de usuarios, horarios, servicios, cortes, citas y reseñas; cambio de estado de citas y cotizaciones por endpoint (hoy solo se hace por SQL); GET /api/resenas/resumen.

---

# 5. Uso de IA generativa

Documentar el uso de herramientas de inteligencia artificial durante el desarrollo del proyecto.

- \*\*Herramienta utilizada:\*\*

- \*\*Actividad para la que se utilizó:\*\*

- \*\*Código o contenido generado:\*\*

- \*\*Modificaciones realizadas por el equipo:\*\*

- \*\*Forma en que se verificó el resultado:\*\*

> Si no se utilizó IA, indicarlo expresamente.

---

# 6. Participación individual

Describir brevemente la contribución de cada integrante.

- **Integrante 1:** José Emmanuel Martínez Galicia (@tachi) — POST y GET de reseñas (entidad, repositorio y controlador), pruebas con cURL del flujo completo y esqueleto HTML/CSS del frontend. Commit `4109e6b`.
- **Integrante 2:** Erick Iray Gutiérrez del Ángel — Entidades, repositorios y POST/GET de servicios, cortes, cotizaciones y citas, y correcciones de `CotizacionControlador` e `ICotizacionDetalle`. Commits `2a6af52`, `27b38dc` y `f95e0f5`.
- **Integrante 3:** José Luis Lucas Landa — Proyecto Spring Boot, backend de usuarios y horarios con login y BCrypt. Commit `fe82a70`.

---

# 7. Próximas actividades

Indicar las tareas que se realizarán antes del siguiente reporte.
- Probar el frontend en el navegador contra el backend corriendo en WSL.
- Dejar el `application.properties` fuera del repo (`.gitignore` + `application.properties.example`) y preparar el README reproducible para clonar y correr en otro WSL.

---

## Observaciones adicionales

Agregar cualquier comentario, acuerdo, decisión técnica o evidencia complementaria relevante para el proyecto.

- El `application.properties` que está hoy en el repo trae la IP de Erick (172.20.96.1). Cada integrante debe usar su propia IP local y no subir ese cambio.
- Antes de la entrega final hay que cambiar o quitar la contraseña demo del administrador (`admin@barberia.com` / `admin123`).