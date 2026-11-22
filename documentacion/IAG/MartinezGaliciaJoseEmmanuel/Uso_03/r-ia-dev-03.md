# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 19 de noviembre de 2026
**Número de reporte:** 3

---

# 1. Breve descripción de uso

Armar mi parte del frontend del E4 (reseñas + integración general): `index.html` con tabla de servicios y tabla de cortes, `resenas.html` con formulario para crear reseña, tabla de reseñas, tarjeta de resumen por barbero y formulario de edición, y `js/app.js` con la función común `apiFetch()`.

Alcance limitado a mi módulo y a la integración general: `frontend/index.html`, `frontend/resenas.html`, `frontend/agendar.html`, `frontend/js/app.js`, `frontend/css/styles.css`. `agendar.html` es módulo de Erick en E4; solo se mantuvo su estructura.

---

# 2. Evidencias de uso

| Evidencia | Formato |
|------------|---------|
| Herramienta | Muse (Meta) |
| Modelo/temperatura | Muse Spark 1.3 |
| Prompt original |  "Nesecito que le des mas estilo a mi parte del frontent con tablas y formularios ademas de Fetch común y mensajes de error y confirmación, en un zip". El texto exacto del pedido está en `prompt.md`. |
| Respuesta de IA | `index.html` (tabla de servicios con "Ver cortes" y tabla de cortes), `resenas.html` (crear, tabla, resumen, edición), `app.js` con `apiFetch()`, sesión y mensajes; `styles.css` con estilos de tablas y mensajes. El resumen de lo entregado está en `respuesta.md`. |
| Código anterior | Commit `34232722690a239742594f119450c0feebe849d5` (frontend E3) |
| Código posterior | Commit `731d67410f7728c8890f0a103e817709b17a44d4` ("E4 frontend: tablas, formularios y Fetch + resenas") |
| Diferencias entre versiones | `git diff --numstat 3423272 731d674` (salida completa en `cambios.diff`): |
| | `31  1  frontend/agendar.html` |
| | `28  5  frontend/css/styles.css` |
| | `32  3  frontend/index.html` |
| | `323 59 frontend/js/app.js` |
| | `20  6  frontend/resenas.html` |
| Prueba de funcionamiento | Navegador real (localhost:5500, backend en 8081), 19/11/2026: `resenas.html` carga la tabla y el resumen; cita #2 creada por el frontend (cliente 2, barbero 3, servicio 1 Corte clásico, corte 1 Fade, 2026-11-20 12:00-12:30, PENDIENTE); reseña recreada para la cita #1 (5/5, "sisia"); reprogramación con el formulario inline probada 10/10. . |
| Código de uso | Generación |
| Reflexión del estudiante | Este uso represento un gran avance en el proyecto ya que me ayudo a corregir muchos errores de diseño que incluí en mi parte del frontend. En este uso me llevo un aprendizaje que la ia generativa si se usa correctamente, puede ser una gran herramienta de ayuda, pero tambien hay que tener una distincion entre ayuda y distincion, si bien a lo largo de todos estos usos, me di cuenta que la ia puede hacer gran parte de todo el tabajo, es importante que la reflexion y el aprendizaje sea propio.   |

> El prompt y la respuesta están como subsección en `prompt.md` y `respuesta.md`.

---

## Observaciones adicionales

- Agendar devolvía 400 sin mensaje: `Cita.java` usa `LocalDateTime` sin `@JsonFormat` y Jackson solo acepta ISO con 'T'; el frontend mandaba la fecha con espacio. Se corrigió `agendarCita` para mandar inicio/fin en ISO con 'T'.
- El botón Editar reseña no hacía nada: el `onclick` pasaba el comentario con `JSON.stringify` y sus comillas dobles rompían el atributo HTML. Se corrigió pasando solo el id y buscando la reseña en un caché local (`RESENAS_CACHE`).
- El botón Reprogramar usaba `prompt()`, que el preview integrado de VS Code no muestra. Se cambió por un formulario inline con inputs `datetime-local`.
- La división de `app.js` en `common.js` + `resenas.js` (commit d2494c5, 20/11/2026) es parte de este mismo uso: las funciones comunes (`apiFetch`, sesión, mensajes) son la parte de integración general que me tocó en el E4.
