# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 13 de noviembre de 2026
**Número de reporte:** 1

---

# 1. Breve descripción de uso

pruebas con cURL de entidades y repositorios de servicios, cortes y cotizaciones; POST y GET de citas
Alcance limitado a mi módulo: `Servicio.java`, `Corte.java`, `Cita.java`,`EstadoCita.java`, `IServicio.java`, `ICorte.java`, `ICita.java`, `ServicioControlador.java`, `CorteControlador.java`, `CitaControlador.java` y las pruebas `ErickE2RepositoryTest.java`. No se tocó nada de otros módulos.

---

# 2. Evidencias de uso

| Evidencia | Formato |
|------------|---------|
| Herramienta | Mano (Muse, agente personal) |
| Modelo/temperatura | Muse Spark 1.3 |
| Prompt original | "Ayudame analizando los siguientes archivos de referencia y explicame el funcionamiento de una API y como es que puedo hacer una en codigo." El pedido completo, con los archivos que compartí, está en `prompt.md`. |
| Respuesta de IA | Guía y script de cURL. El resumen de lo entregado está en `respuesta.md`. |
| Código anterior | Commit `8a3ed17` ("Merge branch 'main' of https://github.com/twvwrigth/desarrollo-web", jluis-ll, 2026-11-12) |
| Código posterior | Commit `f95e0f5` ("Correción ICotizaciónDetalle", 2026-11-14) |
| Diferencias entre versiones | `git diff --numstat 8a3ed17 f95e0f5` (salida completa en `cambios.diff`): |
| | `2	2	demo/.gitattributes` |
| | `33	33	demo/.gitignore` |
| | `3	3	demo/.mvn/wrapper/maven-wrapper.properties` |
| | `88	88	demo/pom.xml` |
| | `140	0	demo/src/main/java/com/example/demo/Cita.java` |
| | `148	0	demo/src/main/java/com/example/demo/CitaControlador.java` |
| | `39	39	demo/src/main/java/com/example/demo/Configuracion.java` |
| | `96	0	demo/src/main/java/com/example/demo/Corte.java` |
| | `94	0	demo/src/main/java/com/example/demo/CorteControlador.java` |
| | `9	0	demo/src/main/java/com/example/demo/EstadoCita.java` |
| | `78	78	demo/src/main/java/com/example/demo/HorarioBarbero.java` |
| | `77	77	demo/src/main/java/com/example/demo/HorarioControlador.java` |
| | `12	0	demo/src/main/java/com/example/demo/ICita.java` |
| | `14	0	demo/src/main/java/com/example/demo/ICorte.java` |
| | `12	12	demo/src/main/java/com/example/demo/IHorarioBarbero.java` |
| | `15	0	demo/src/main/java/com/example/demo/IServicio.java` |
| | `12	13	demo/src/main/java/com/example/demo/IUsuario.java` |
| | `8	8	demo/src/main/java/com/example/demo/Rol.java` |
| | `87	0	demo/src/main/java/com/example/demo/Servicio.java` |
| | `85	0	demo/src/main/java/com/example/demo/ServicioControlador.java` |
| | `117	117	demo/src/main/java/com/example/demo/Usuario.java` |
| | `100	100	demo/src/main/java/com/example/demo/UsuarioControlador.java` |
| Prueba de funcionamiento | `mvn test` BUILD SUCCESS, 2 pruebas con H2, 2026-11-13 22:36 (en el entorno de Mano, sobre el código entregado). Script pruebas-curl-erick.sh contra el backend real, [fecha]: 24/24 casos OK. Login admin 200; cliente y barbero creados 201; servicio "Corte clásico E2" 150.00 201, duplicado 409, sin admin 403; corte "Fade bajo E2" 201; cita PENDIENTE 2026-11-20 10:00–10:30 201, traslape 10:15–10:45 409; GETs 200. |
| Reflexión del estudiante | El uso de IA para explicar el funcionamiento de las API´s fue de mucha ayuda, así como realizar pruebas curl me ayudó a conocer mejor su estructura y el cómo se deben de usar de forma correcta |

> El prompt y la respuesta están en `prompt.md` y `respuesta.md`.
