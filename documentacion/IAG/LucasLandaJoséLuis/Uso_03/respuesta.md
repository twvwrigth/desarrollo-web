# Respuesta de IA — Uso_03

La IA entregó:

- `pruebas-curl-e4.sh` con 20 casos, función `check` y `RESULTADO_FINAL
  fail=0/1`. Cubre login admin 200 y con contraseña mal 401, registro 201,
  registro con contraseña de 5 caracteres 400, email duplicado 409, crear
  BARBERO sin admin 403, PUT con contraseña de 5 caracteres 400, DELETE
  (baja lógica) 204, GET tras baja 404, login de inactivo 401,
  `?activos=false` con el inactivo y `"activo":false`, PUT de perfil sobre
  inactivo 404, `/estado` sin `activo` 400, `/estado` con id inexistente
  404, reactivar por `/estado` 200, GET del reactivado 200, y horarios:
  crear 201, traslape 409, fin menor o igual que inicio 400, listar 200,
  DELETE 204.
- Los correos llevan marca de tiempo y el día del horario varía, así que el
  script se puede repetir.

