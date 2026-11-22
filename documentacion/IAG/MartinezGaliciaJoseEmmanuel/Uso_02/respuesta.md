# Respuesta de IA — Uso_02

La IA entregó el `ResenaControlador.java` con put y delete:

- GET /api/resenas/resumen?barberoId= (promedio redondeado a 1 decimal, total; 400 sin barberoId, 404 si no es barbero).
- PUT /api/resenas/{id} (solo calificación y comentario; 400/403/404).
- DELETE /api/resenas/{id} (físico, 204; 403/404).
- Helper `clienteDeLaResena()` y `@Autowired IUsuario` para validar el barbero.

Verificación: `pruebas-E3.sh` el 16/11/2026, 11/11 casos OK contra el backend real, incluida la persistencia tras reinicio. El error de compilación `iusuario` se corrigió declarando el campo una sola vez a nivel de clase.
