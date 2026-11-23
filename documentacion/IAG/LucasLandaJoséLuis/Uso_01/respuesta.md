# Respuesta de IA — Uso_01

La IA entregó:

- `pruebas-curl-e2.sh` con los casos 0-6: login admin, registro de cliente,
  crear barbero con admin, crear horario, listar horarios, consultar usuario
  y login cliente. Imprime cada respuesta; los códigos se ven con `curl -i`
  (201, 200, 401, 403, 404, 409).
- El id del admin sale del login (ajuste del 2026-11-22 al armar el set por
  avance; antes se asumía 1).

Verificación: los casos del script se verificaron en el smoke test del E2
en VM (registro 201 sin contraseña en la respuesta, duplicado 409, barbero
con admin 201, horario 201, GET 200, traslape 409, login cliente 200).
