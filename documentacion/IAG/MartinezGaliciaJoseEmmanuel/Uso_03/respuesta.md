# Respuesta de IA — Uso_03

La IA entregó:

- `index.html`: tabla de servicios (GET /api/servicios) y tabla de cortes (GET /api/servicios/{id}/cortes).
- `resenas.html`: formulario para crear reseña, tabla de reseñas, tarjeta de resumen por barbero y formulario oculto de edición.
- `js/app.js`: `apiFetch()` común (base, Content-Type JSON, X-Usuario-Id), sesión y mensajes de error/confirmación.
- `css/styles.css`: estilos de tablas, mensajes y sesión.
- Después: corrección del formato de fecha (ISO con 'T'), corrección del botón Editar (RESENAS_CACHE) y formulario inline de reprogramar en lugar de `prompt()`.

Verificación: navegador real el 19/11/2026 (cita #2 creada, reseña "sisia" recreada, reprogramar 10/10). La división de `app.js` en `common.js` + `resenas.js` (d2494c5) es parte de este uso.
