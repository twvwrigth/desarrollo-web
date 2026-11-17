# Reporte de Avance del Proyecto

**Equipo:** José Emmanuel Martínez Galicia (@tachi), Erick Iray Gutiérrez del Ángel, José Luis Lucas Landa
**Fecha:** 16/11/2026
**Número de reporte:** 3
**Repositorio GitHub:** https://github.com/twvwrigth/desarrollo-web

---

# 1. Actividades realizadas

Describir las actividades desarrolladas desde el último reporte.

Desde el Reporte 2 (13/11/2026, backend básico E2), en este periodo se
trabaja en el backend completo E3:

- [HECHO, probado 16/11/2026, 11/11 casos OK] José Emmanuel: `ResenaControlador.java`
  completo: trae POST/GET sin cambios, más
  `GET /api/resenas/resumen?barberoId=` con promedio y total,
  `PUT /api/resenas/{id}` para editar la propia reseña y
  `DELETE /api/resenas/{id}` 
- [HECHO, probado 16/11/2026 contra MySQL real en WSL, con persistencia tras
  reinicio] José Luis: `UsuarioControlador.java`, `HorarioControlador.java`,
  `IUsuario.java` y `HorarioBarbero.java` completos: `GET /api/usuarios?rol=`
  (solo activos, rol inválido → 400), `PUT /api/usuarios/{id}` (edita perfil,
  re-encripta BCrypt, email de otro → 409, no cambia rol ni activo),
  `DELETE /api/usuarios/{id}` (baja lógica `activo=false`), `PUT /api/horarios/{id}`
  (parcial, traslape → 409, `diaSemana` pasa a `Integer` para el PUT parcial) y
  `DELETE /api/horarios/{id}`.
- [HECHO, verificado 16/11/2026, 10/10 pruebas OK con `mvn test`; falta probar con cURL
  contra MySQL y subir] Erick: `ServicioControlador.java`, `CorteControlador.java`,
  `CitaControlador.java` y `CotizacionControlador.java` completos: PUT/DELETE de servicios
  y cortes (solo admin, baja lógica), GET /api/citas con filtros (clienteId, barberoId,
  fecha, estado), GET /api/citas/disponibilidad (huecos libres del barbero),
  PUT /api/citas/{id} para reprogramar, PUT /api/citas/{id}/estado con transiciones,
  DELETE /api/citas/{id} (cancela), GET /api/cotizaciones?clienteId=,
  PUT /api/cotizaciones/{id}/estado y DELETE /api/cotizaciones/{id} (solo PENDIENTE).

---

# 2. Evidencias de trabajo

| Actividad | Integrante responsable | Enlace a commit |
| --- | --- | --- |
| E3 reseñas: PUT/DELETE + resumen + frontend E3 | José Emmanuel Martínez Galicia | https://github.com/twvwrigth/desarrollo-web/commit/34232722690a239742594f119450c0feebe849d5 |
| E3 usuarios y horarios: PUT/DELETE, filtro por rol y baja lógica + pruebas cURL contra MySQL real y persistencia tras reinicio | José Luis Lucas Landa | https://github.com/twvwrigth/desarrollo-web/commit/85af7560b582a9735dfc48d46fa2f5ea976459f6 |
| E3 servicios, cortes, citas y cotizaciones: PUT/DELETE, filtros, disponibilidad y cambios de estado + pruebas cURL | Erick Iray Gutiérrez del Ángel | https://github.com/twvwrigth/desarrollo-web/commit/77bddb9caf8cf79ab3c75b5f9ee24a41a6fb3c3c |

---

# 3. Problemas encontrados

Describir los problemas técnicos encontrados y cómo fueron solucionados.

- Error de compilación al agregar el resumen: `cannot find symbol: variable
  iusuario` en `ResenaControlador.java`. 
- El log de `pruebas-E3.sh` no se guardó (`Permission denied`): el script se
  corrió desde la carpeta System32 de Windows, que no deja escribir desde WSL.
  Solución: correrlo desde la carpeta del repo (`cd` al proyecto antes de
  `bash pruebas-E3.sh`). Las pruebas sí corrieron todas y se leyeron de la
  terminal.
- Error en los curls de E3: el script ponía `BASE=http://localhost:8081/api` y las rutas
  ya traen el `/api`, así que todas las URLs salían con `/api/api/` y daban 404.
  Solución: `BASE=http://localhost:8081` en el script y en la guía (el mismo error
  estaba en el script de E2 y también se corrigió).

---

# 4. Avance funcional

Indicar qué funcionalidades ya se encuentran operativas y cuáles están pendientes.

**Operativas:**

- Todo lo del Reporte 2 sigue operativo (usuarios, horarios, servicios, cortes,
  cotizaciones, citas POST/GET y reseñas POST/GET, probado el 13/11/2026).
- Reseñas E3, probado el 16/11/2026 ~13:19 CST con `pruebas-E3.sh` (11/11 casos OK):
  - `GET /api/resenas/resumen?barberoId=3` → 200, promedio 5.0, total 1.
  - Sin barberoId → 400; barbero 999 → 404.
  - `PUT /api/resenas/1` (cliente 2, 5 → 4) → 200; después el resumen dice
    promedio 4.0, total 1. Calificación 6 → 400; otro usuario → 403;
    reseña 999 → 404.
  - Persistencia: tras parar y volver a arrancar el backend, el resumen sigue
    diciendo promedio 4.0, total 1.
  - `DELETE /api/resenas/1` (cliente 2) → 204; otro usuario → 403;
    reseña 999 → 404; borrarla otra vez → 404. Después del DELETE la lista
    queda vacía y el resumen dice total 0.
  - Nota: el log del script no se guardó en archivo por un `Permission denied`
    al escribir en la carpeta System32 de Windows; los resultados se tomaron
    de la salida de la terminal, que mostró OK en los 11 casos.
- E3 de José Luis, probado el 16/11/2026 contra MySQL real en WSL (puerto 8081):
  persistencia comprobada con POST de usuario, parada y reinicio del backend y
  GET con 200 y los mismos datos. Prueba de integración `JoseLuisE3IntegrationTest`
  1/1 OK y smoke cURL todo verde (`fail=0`): filtro `?rol=BARBERO` incluye barbero
  y excluye cliente, rol inválido → 400, PUT usuario → 200, email duplicado → 409,
  PUT usuario 404, PUT horario parcial → 200, traslape al editar → 409, PUT horario
  404, DELETE horario → 204 y repetido → 404, DELETE usuario → 204, GET tras baja
  → 404. Nota: el test y los docs cURL no se subieron en `85af756`, quedaron solo
  en copia local.
- E3 de Erick, verificado el 16/11/2026 con `mvn test` (10/10 pruebas OK, H2, sin tocar
  MySQL): PUT/DELETE de servicio y corte con baja lógica, filtros de citas,
  disponibilidad con huecos libres, reprogramar cita, transiciones de estado
  (PENDIENTE → CONFIRMADA/CANCELADA, CONFIRMADA → COMPLETADA/CANCELADA) y DELETE de
  cita que cancela; filtro y cambio de estado de cotizaciones y DELETE solo si está
  PENDIENTE. Falta la prueba con cURL contra MySQL y subir el commit.

**Pendientes:**

- Probar el frontend E3 en el navegador contra el backend corriendo en WSL
  (`python3 -m http.server 5500` desde `frontend/`, no abrir como `file://`).
- Reprogramar y cancelar cita desde el frontend: depende de que Erick suba su
  PUT/DELETE de citas. Hasta entonces esos botones avisan 404, que no es error
  de la prueba.
- Cuando Erick suba `PUT /api/citas/{id}/estado`, ya no hará falta el
  `UPDATE citas SET estado='COMPLETADA'` directo en MySQL para probar reseñas nuevas.
- Casos de error de reseñas del E2 por documentar: duplicada (409), calificación 6
  (400), cita inexistente (404).

---

# 5. Uso de IA generativa

Documentar el uso de herramientas de inteligencia artificial durante el desarrollo del proyecto.

- **Herramienta utilizada:**

- **Actividad para la que se utilizó:**

- **Código o contenido generado:**

- **Modificaciones realizadas por el equipo:**

- **Forma en que se verificó el resultado:**

> Si no se utilizó IA, indicarlo expresamente.

---s

# 6. Participación individual

Describir brevemente la contribución de cada integrante.

- **Integrante 1:** José Emmanuel Martínez Galicia (@tachi) — PUT/DELETE de reseñas,
  GET resumen, pruebas cURL E3 y frontend E3 (resumen, editar/eliminar reseña,
  mis citas con filtro, reprogramar/cancelar). Commit [PENDIENTE].
- **Integrante 2:** Erick Iray Gutiérrez del Ángel — PUT/DELETE de servicios y cortes,
  filtros y disponibilidad de citas, reprogramar y cambio de estado de citas,
  filtro y cambio de estado de cotizaciones, pruebas cURL E3 y 8 pruebas MockMvc
  (10/10 con `mvn test`). Commit [PENDIENTE].
- **Integrante 3:** José Luis Lucas Landa — PUT/DELETE de usuarios y horarios,
  filtro por rol, baja lógica y pruebas contra MySQL real en WSL con persistencia
  tras reinicio.
  Commit: https://github.com/twvwrigth/desarrollo-web/commit/85af7560b582a9735dfc48d46fa2f5ea976459f6

---

# 7. Próximas actividades

Indicar las tareas que se realizarán antes del siguiente reporte.

- Probar el E3 de reseñas en WSL y subir el commit de José Emmanuel.
- Probar los endpoints de Erick cuando los suba (reprogramar/cancelar cita y cambio
  de estado) y el frontend completo en el navegador.
- Preparar la entrega del 23/11/2026: release en GitHub, video de 15-20 minutos con
  los tres integrantes, PDF final del documento y defensa individual de cada parte.
- Revisiones 24 y 25/11/2026 y segundo parcial 26/11/2026.

---

## Observaciones adicionales

Agregar cualquier comentario, acuerdo, decisión técnica o evidencia complementaria relevante para el proyecto.

- El `application.properties` que está hoy en el repo trae la IP de Erick. Cada
  integrante debe usar su propia IP local y no subir ese cambio.
