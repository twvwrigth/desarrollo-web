# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 16 de noviembre de 2026
**Número de reporte:** 2

---

# 1. Breve descripción de uso

Ampliar el controlador de reseñas con el backend completo del E3: GET /api/resenas/resumen por barbero, PUT /api/resenas/{id} y DELETE /api/resenas/{id}, manteniendo el POST/GET del E2. Reglas del E3: solo el cliente de la cita edita o elimina su reseña; la eliminación de reseña es física.

Alcance limitado a mi módulo: `ResenaControlador.java` y ajustes menores del frontend de reseñas.

---

# 2. Evidencias de uso

| Evidencia | Formato |
|------------|---------|
| Herramienta | Muse (Meta) |
| Modelo/temperatura | Muse Spark 1.3 |
| Prompt original | "Agrega al controlador de reseñas el resumen por barbero, el PUT y el DELETE del E3, con las reglas de que solo el cliente edita y elimina su reseña". |
| Respuesta de IA | Resumen con promedio redondeado a 1 decimal y total; PUT que actualiza solo calificación y comentario; DELETE físico con 204; helper `clienteDeLaResena()` para la regla de propiedad; `@Autowired IUsuario` para validar el barbero en el resumen. El resumen de lo entregado está en `respuesta.md`. |
| Código anterior | Commit `4109e6bba805000fb9d12be2d60c8c72876d7885` |
| Código posterior | Commit `34232722690a239742594f119450c0feebe849d5` ("E3 resenas: PUT/DELETE + resumen + front") |
| Diferencias entre versiones | `git diff --numstat 4109e6b 3423272` (salida completa en `cambios.diff`): |
| | `107 15 demo/src/main/java/com/example/demo/ResenaControlador.java` |
| | `41  6  frontend/agendar.html` |
| | `7   3  frontend/css/styles.css` |
| | `1   4  frontend/index.html` |
| | `31  1  frontend/resenas.html` |
| Prueba de funcionamiento | Script `pruebas-E3.sh` contra el backend real, 16/11/2026: **11/11 casos OK**. Resumen 200 (promedio 5.0, total 1), 400 sin barberoId, 404 barbero 999; PUT 200 (5 → 4, resumen 4.0), 400 calificación 6, 403 otro usuario, 404 reseña 999; persistencia tras reinicio del backend (resumen sigue 4.0 / total 1); DELETE 204 propio, 403 otro usuario, 404 inexistente y 404 al repetir. |
| Código de uso | Modificación |
| Reflexión del estudiante | Considero que es una ayuda muy grande al momendo de resolver dudas. ya que cuando estaba deseñando el put y el delete, me surgieron dudas porque me salian varios errores que no entendia, despues de pedir ayuda, me señalo de manera clara mis errores y me ayudo con las pruebas.  |

> El prompt y la respuesta están como subsección en `prompt.md` y `respuesta.md`.

---

## Observaciones adicionales

- Error de compilación durante la integración: `iusuario` no se encontraba porque el controlador E2 no tenía el campo `@Autowired IUsuario`; se declaró una sola vez a nivel de clase y el backend compiló.
- El log del script no se guardó (`Permission denied`) porque se corrió desde la carpeta System32 de Windows; los resultados se leyeron de la salida de la terminal. La próxima vez se corre desde la carpeta del repo.
- Las pruebas del E3 no llevan un r-ia-dev aparte: este reporte cubre el uso de IA en el desarrollo del E3; el uso de IA para preparar las pruebas se documenta en el Uso_04.
