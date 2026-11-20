# Reporte de Avance del Proyecto

**Equipo:** 5
**Fecha:** 20/11/2026
**Número de reporte:** 4
**Repositorio GitHub:** https://github.com/twvwrigth/desarrollo-web

## 1. Actividades realizadas

- **José Emmanuel Martínez Galicia (E4 — reseñas + integración general):**
- [HECHO, probado en navegador 19/11/2026] Frontend E4: `index.html` con tabla de servicios (GET /api/servicios) y tabla de cortes. `resenas.html` con formulario para crear reseña, tabla de reseñas (Cita | Calificación | Comentario | Fecha | Acciones), tarjeta de resumen por barbero y formulario, y mensajes de error/confirmación con el formato {"error","mensaje"} del backend; `css/styles.css` con estilos de tablas, mensajes y sesión.
- [HECHO, 19/11/2026] Corrección del formato de fecha al agendar: `agendarCita`
- [HECHO, 19/11/2026] Corrección del botón Editar reseña.
- [HECHO, 19/11/2026] Reprogramar cita con formulario inline en `agendar.html`
- **Erick Iray Gutiérrez del Ángel (E4 — servicios, cortes, citas y cotizaciones):** [PENDIENTE — al 19/11/2026 23:22 no se verificó su frontend E4 en el repo]
- **José Luis Lucas Landa (E4 — login, registro, usuarios y horarios):**
- [HECHO, código entregado 20/11/2026, pendiente de subir al repo y probar contra MySQL real en WSL] Backend E4: `PUT /api/usuarios/{id}/estado` (200/400/404) y `GET /api/usuarios?activos=false` en `UsuarioControlador.java`; test `JoseLuisE4IntegrationTest` 1/1 OK en VM con H2. Frontend E4: `login.html` (POST /api/auth/login, sesión en sessionStorage), `registro.html` (POST /api/usuarios, 201/400/403/409), `usuarios.html` (tabla con filtro por rol, editar PUT, desactivar DELETE 204, reactivar PUT /estado) y `horarios.html` (selector de barberos, crear horario, tabla, editar PUT, eliminar DELETE 204) + `js/usuarios-horarios.js` en archivo aparte para no chocar con `app.js`.

## 2. Evidencias de trabajo

| Actividad | Integrante responsable | Enlace a commit |
| --- | --- | --- |
| E4 frontend: tablas, formularios y Fetch + reseñas (index, resenas, agendar, app.js, styles.css — 5 archivos, 434 inserciones) | José Emmanuel Martínez Galicia | https://github.com/twvwrigth/desarrollo-web/commit/731d67410f7728c8890f0a103e817709b17a44d4 |
| E4 backend usuarios: PUT /api/usuarios/{id}/estado y GET /api/usuarios?activos=false (UsuarioControlador.java, pom.xml con H2 solo para tests, JoseLuisE4IntegrationTest.java) | José Luis Lucas Landa | [PENDIENTE — por subir] |
| E4 frontend usuarios y horarios: login.html, registro.html, usuarios.html, horarios.html y js/usuarios-horarios.js (5 archivos nuevos, no se tocó app.js ni index/agendar/resenas) | José Luis Lucas Landa | https://github.com/twvwrigth/desarrollo-web/commit/a7607167470468f93d06c5b77b834fb8298186fb |
|  |  |  |

## 3. Problemas encontrados

1. **Agendar cita devolvía 400 "Bad Request" sin mensaje.**
2. **El botón Editar reseña no hacía nada.**
3. **El botón Reprogramar cita no hacía nada en el preview de VS Code.**
4. **Activar/desactivar un usuario era imposible con el backend E3 (área de José Luis).** El `PUT /api/usuarios/{id}` no cambia `activo` (además `activo` es `boolean` primitivo: si no viene en el JSON queda en `false` y cualquier edición de perfil desactivaría al usuario), el `GET /api/usuarios` solo lista activos y el `GET /api/usuarios/{id}` da 404 para inactivos. Al desactivar a alguien, desaparecía del listado y no había forma de reactivarlo. Solución (E4, módulo propio): endpoint nuevo `PUT /api/usuarios/{id}/estado` con `{"activo": true/false}` (200/400/404, sí acepta usuarios dados de baja) y `GET /api/usuarios?activos=false` para listar inactivos; el PUT de perfil quedó intacto.

## 4. Avance funcional

- **Frontend E4 (José Emmanuel):** probado en navegador contra el backend real en el puerto 8081. `resenas.html` carga la tabla y el resumen; la reseña de la cita #1 se recreó por el formulario (5/5, "sisia", 19/11/2026 22:46). `agendar.html` crea citas (cita #2: cliente 2, barbero 3, servicio 1 Corte clásico, corte 1 Fade, 2026-11-20 12:00-12:30, notas "das", PENDIENTE) y reprograma con el formulario inline. Las funciones Fetch comunes (`apiFetch`) y los mensajes de error/confirmación funcionan.
- **Backend y frontend E4 (José Luis):** backend verificado`,` el usuario ya no sale en el listado normal, con `?activos=false` sí sale con `"activo":false`, reactivar por PUT /estado 200 y vuelve al listado, `/estado` sin `activo` 400, id inexistente 404, login de dado de baja 401 y horarios (crear, traslape 409, listar, eliminar 204). Frontend verificado solo por sintaxis (`node --check` OK) y cruce de IDs del JS contra el HTML; pendiente probarlo en navegador contra MySQL real en WSL (backend en 8081, frontend con `python3 -m http.server 5500`) y subir el commit.
- **Pendiente de subir:** el frontend E4 de Erick (servicios, cortes, citas, cotizaciones).

## 5. Uso de IA generativa

- Herramienta utilizada:
- Actividad para la que se utilizó:
- Código o contenido generado:
- Modificaciones realizadas por el equipo:
- Forma en que se verificó el resultado:

> Si no se utilizó IA, indicarlo expresamente.

## 6. Participación individual

- **Integrante 1 — José Emmanuel Martínez Galicia:** E4 frontend de reseñas e integración general (index, resenas, agendar, app.js, styles.css); corrección de fecha ISO, Editar con caché y formulario de reprogramar; pruebas en navegador 19/11/2026. Commit 731d674.
- **Integrante 2 — Erick Iray Gutiérrez del Ángel:** [PENDIENTE — anotar su frontend E4 y su commit cuando lo suba]
- **Integrante 3 — José Luis Lucas Landa:** E4 backend de usuarios (PUT /estado, ?activos=false, test 1/1 OK en VM con H2) y E4 frontend de login, registro, usuarios y horarios (4 páginas + js/usuarios-horarios.js, node --check OK); pendiente integrar, probar en WSL contra MySQL real y subir el commit. Commit [PENDIENTE — por subir].
- **Integrante 4:** No aplica.

## 7. Próximas actividades

1. Subir el frontend E4 de Erick (servicios, cortes, citas, cotizaciones) y el backend + frontend E4 de José Luis (integrar en su PC, probar contra MySQL real en WSL y subir el commit); anotar sus commits.
2. Probar la integración completa frontend-backend con los tres módulos y anotar los resultados reales.
3. Agregar los enlaces de Login/Registro/Usuarios/Horarios al nav de index.html, agendar.html y resenas.html (ver NAV-PATCH, parte de la integración general de Emmanuel).
4. Coordinar la entrega final: release de GitHub, video de 15-20 min con los tres integrantes, documento PDF final.

## Observaciones adicionales

- Entrega final: 23 de noviembre de 2026; revisiones 24 y 25 de noviembre; segundo parcial 26 de noviembre de 2026.