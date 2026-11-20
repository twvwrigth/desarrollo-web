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
- **José Luis Lucas Landa (E4 — login, registro, usuarios y horarios):** [PENDIENTE — al 19/11/2026 23:22 no se verificó su frontend E4 en el repo]

## 2. Evidencias de trabajo

| Actividad | Integrante responsable | Enlace a commit |
|---|---|---|
| E4 frontend: tablas, formularios y Fetch + reseñas (index, resenas, agendar, app.js, styles.css — 5 archivos, 434 inserciones) | José Emmanuel Martínez Galicia | https://github.com/twvwrigth/desarrollo-web/commit/731d67410f7728c8890f0a103e817709b17a44d4 |
|  |  |  |
|  |  |  |
|  |  |  |

## 3. Problemas encontrados

1. **Agendar cita devolvía 400 "Bad Request" sin mensaje.**
2. **El botón Editar reseña no hacía nada.** 
3. **El botón Reprogramar cita no hacía nada en el preview de VS Code.** 


## 4. Avance funcional

- **Frontend E4 (José Emmanuel):** probado en navegador contra el backend real en el puerto 8081. `resenas.html` carga la tabla y el resumen; la reseña de la cita #1 se recreó por el formulario (5/5, "sisia", 19/11/2026 22:46). `agendar.html` crea citas (cita #2: cliente 2, barbero 3, servicio 1 Corte clásico, corte 1 Fade, 2026-11-20 12:00-12:30, notas "das", PENDIENTE) y reprograma con el formulario inline. Las funciones Fetch comunes (`apiFetch`) y los mensajes de error/confirmación funcionan.

- **Pendiente de subir:** el frontend E4 de Erick (servicios, cortes, citas, cotizaciones) y de José Luis (login, registro, usuarios, horarios).

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
- **Integrante 3 — José Luis Lucas Landa:** [PENDIENTE — anotar su frontend E4 y su commit cuando lo suba]
- **Integrante 4:** No aplica.

## 7. Próximas actividades

1. Subir el frontend E4 de Erick (servicios, cortes, citas, cotizaciones) y de José Luis (login, registro, usuarios, horarios) y anotar sus commits.
2. Probar la integración completa frontend-backend con los tres módulos y anotar los resultados reales.
5. Coordinar la entrega final: release de GitHub, video de 15-20 min con los tres integrantes, documento PDF final.

## Observaciones adicionales

- Entrega final: 23 de noviembre de 2026; revisiones 24 y 25 de noviembre; segundo parcial 26 de noviembre de 2026.
