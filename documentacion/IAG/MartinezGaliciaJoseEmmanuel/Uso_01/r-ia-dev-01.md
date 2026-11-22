# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 13 de noviembre de 2026
**Número de reporte:** 1

---

# 1. Breve descripción de uso

Generar el esqueleto HTML/CSS del frontend, siguiendo `requerimientos.md`

Alcance limitado a mi módulo: `Resena.java`, `IResena.java`, `ResenaControlador.java` y el frontend (`index.html`, `agendar.html`, `resenas.html`, `css/styles.css`, `js/app.js`). No se tocó nada de otros módulos.

---

# 2. Evidencias de uso

| Evidencia | Formato |
|------------|---------|
| Herramienta | Muse (Meta) |
| Modelo/temperatura | Muse Spark 1.3 |
| Prompt original |  "Genera  el esqueleto del frontend con HTML/CSS para mi proyecto y dame casos de prueba con curl para probar contra mi backend".  |
| Respuesta de IA |  Frontend con Fetch a http://localhost:8081/api. El resumen de lo entregado está en `respuesta.md`. |
| Código anterior | No existía (archivos nuevos; base: commit f95e0f5) |
| Código posterior | Commit `4109e6bba805000fb9d12be2d60c8c72876d7885` ("E2 resenas: entidad, repositorio y controlador POST/GET + esqueleto frontend") |
| Diferencias entre versiones | `git diff --numstat f95e0f5 4109e6b` (salida completa en `cambios.diff`): |
| | `13  0  demo/src/main/java/com/example/demo/IResena.java` |
| | `84  0  demo/src/main/java/com/example/demo/Resena.java` |
| | `100 0  demo/src/main/java/com/example/demo/ResenaControlador.java` |
| | `61  0  frontend/agendar.html` |
| | `125 0  frontend/css/styles.css` |
| | `46  0  frontend/index.html` |
| | `164 0  frontend/js/app.js` |
| | `81  0  frontend/resenas.html` |
| Prueba de funcionamiento | cURL contra el backend real, 13/11/2026: flujo completo (admin 1, cliente 2, barbero 3, servicio 1 "Corte clásico", corte 1 "Fade", cotización 1 total 150.00, cita 1 PENDIENTE 2026-11-20 11:00-11:30; UPDATE a COMPLETADA en MySQL), POST /api/resenas 201 (reseña 1, calificación 5) y GET /api/resenas?barberoId=3 devuelve la reseña. |
| Código de uso | Generación |
| Reflexión del estudiante | El uso de IA Generativa a nivel de pruebas curl fue lo que mas me ayudo al momento de probar mi api, me resulto impresionante como este modelo de ia detecto que su sintaxis estaba mal al momento de entregarme los casos de prueba y sin que yo le dijera nada, me borro el mensaje que me habia entregado y me lo dio corregido.  |

> El prompt y la respuesta están en `prompt.md` y `respuesta.md`.

---

## Observaciones adicionales

- Al inicio se usó una `Cita` provisional para compilar antes de que Erick subiera la suya; se reemplazó por la `Cita` real de Erick (commit 2a6af52). El controlador se corrigió para usar el enum `EstadoCita.COMPLETADA` y `getCliente().getId()` (la `Cita` de Erick no tiene `getClienteId()`).
- La corrección del `ICotizacionDetalle` que impedía arrancar el backend es de Erick (commit f95e0f5), no de este uso.
