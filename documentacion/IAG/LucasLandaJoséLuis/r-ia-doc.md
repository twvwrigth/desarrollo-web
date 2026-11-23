# r-ia-doc - Jose Luis Lucas Landa - Dudas sobre permisos por rol (2026-11-22)

> Consultas de dudas (no generación de código) sobre qué mover para los
> permisos por rol del E4. Formato del curso (proyecto-final__1.pdf,
> "Detalle de entrega para consulta", `r-ia-doc.md`): Fecha, Herramienta,
> Prompt original, Respuesta IA. Herramienta en todas las filas:
> Muse Spark, Meta.

| Fecha | Prompt original | Respuesta IA |
|---|---|---|
| 22/11/2026 17:34 | ¿Por qué, logueado como usuario normal, puedo modificar y eliminar usuarios? ¿Qué restricciones se pueden usar en mis páginas, se oculta por tipo de usuario? | El backend solo pedía ADMIN al crear BARBERO/ADMIN; PUT, DELETE, `/estado` y todos los horarios no tenían ningún chequeo de rol o sesión, y el frontend guardaba el rol en sesión pero no lo usaba para restringir nada. Se explicó en dos capas: ocultar en el frontend es solo apariencia, el 403 en el backend es la seguridad real; y se recomendó usuarios solo ADMIN, horarios solo ADMIN o barbero dueño, registro según sesión y nav oculta por rol. |
| 22/11/2026 17:43 | Si le movemos al backend va a ser mucho? | No: 2 archivos, unas 70 líneas siguiendo el patrón del chequeo de admin que ya tenía el POST, sin modelos, sin cambios de BD, sin endpoints nuevos ni dependencias; el test crece con los casos 403. |
| 22/11/2026 18:04 | ¿Qué le moviste al pom? ¿Era necesario? | Solo se agregó H2 en scope `test`, nada más. Necesario solo para correr el test JUnit (usa H2 en memoria); para que la app corra con MySQL no hace falta. Es el mismo pom que ya venía en la entrega del backend E4. |
| 22/11/2026 18:05 | ¿Cuántas líneas agregaste en el backend? | 112 agregadas en total: 60 en `UsuarioControlador.java` y 52 en `HorarioControlador.java` (94 de código y 18 de comentarios). La mayoría son los 4 métodos que se repiten en los dos archivos (revisar sesión, revisar admin, 401 y 403) y el chequeo de 2-3 líneas en cada endpoint. |
| 22/11/2026 18:10 |
