# Respuesta de IA — Uso_04

La IA entregó:

- `pruebas-curl-permisos.sh` con 24 casos, función `check` y
  `RESULTADO_FINAL fail=0/1`. Cubre listar usuarios sin sesión 401, como
  CLIENTE 403, como BARBERO 403 y como ADMIN 200; `?rol=BARBERO` sin sesión
  200 (sigue abierto); ver perfil sin sesión 401, propio 200, ajeno 403 y
  como ADMIN 200; editar perfil propio 200 y ajeno 403; DELETE como CLIENTE
  403 y como ADMIN 204; `/estado` como CLIENTE 403 y reactivar como ADMIN
  200; horarios: listar sin sesión 200, crear sin sesión 401, como CLIENTE
  403, de otro barbero 403 y como dueño 201, editar como CLIENTE 403, de
  otro barbero 403 y como dueño 200, eliminar como ADMIN 204.
- Los correos llevan marca de tiempo y el día del horario varía, así que el
  script se puede repetir.

