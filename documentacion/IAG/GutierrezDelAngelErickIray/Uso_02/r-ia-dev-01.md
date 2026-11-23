# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 16 de noviembre de 2026
**Número de reporte:** 1

---

# 1. Breve descripción de uso

pruebas con cURL de entidades y repositorios de servicios, cortes y cotizaciones; POST y GET de citas
Alcance limitado a mi módulo:`Cotizacion.java`, `CotizacionDetalle.java`, `ICotizacion.java`, `ICotizacionDetalle.java`, `CotizacionControlador.java` y las pruebas `ErickE2RepositoryTest.java` y `CotizacionJsonTest.java`. No se tocó nada de otros módulos.

---

# 2. Evidencias de uso

| Evidencia | Formato |
|------------|---------|
| Herramienta | Mano (Muse, agente personal) |
| Modelo/temperatura | Muse Spark 1.3 |
| Prompt original | "Ayudame a realizar las pruebas curl de las cotizaciones." El pedido completo, con los archivos que compartí, está en `prompt.md`. |
| Respuesta de IA | Guía y script de cURL. El resumen de lo entregado está en `respuesta.md`. |
| Código anterior | Commit `8a3ed17` ("Merge branch 'main' of https://github.com/twvwrigth/desarrollo-web", jluis-ll, 2026-11-12) |
| Código posterior | Commit `f95e0f5` ("Correción ICotizaciónDetalle", 2026-11-14) |
| Diferencias entre versiones | `git diff --numstat 8a3ed17 f95e0f5` (salida completa en `cambios.diff`): |
| | `2	2	demo/.gitattributes` |
| | `33	33	demo/.gitignore` |
| | `3	3	demo/.mvn/wrapper/maven-wrapper.properties` |
| | `88	88	demo/pom.xml` |
| | `103	0	demo/src/main/java/com/example/demo/Cotizacion.java` |
| | `119	0	demo/src/main/java/com/example/demo/CotizacionControlador.java` |
| | `61	0	demo/src/main/java/com/example/demo/CotizacionDetalle.java` |
| | `8	0	demo/src/main/java/com/example/demo/EstadoCotizacion.java` |
| | `10	0	demo/src/main/java/com/example/demo/ICotizacion.java` |
| | `6	0	demo/src/main/java/com/example/demo/ICotizacionDetalle.java` |
| Prueba de funcionamiento | Script pruebas curl contra el backend cotizaciones, 11/11 casos OK. Cotización creada 201 con total 310.00; filtro por cliente 200; estado inválido 400; PENDIENTE como destino 409; ACEPTADA 200; RECHAZADA después de aceptada 409; DELETE de ACEPTADA 409; segunda cotización creada 201, DELETE 204 y GET posterior 404. |
| Reflexión del estudiante | La IA me ha ayudado a poder realizar pruebas curl de forma eficiente, sin errores de sintaxis o errores en la lógica |

> El prompt y la respuesta están en `prompt.md` y `respuesta.md`.
