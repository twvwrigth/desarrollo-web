# Respuesta de IA — Uso_02

La IA entregó:

- `pruebas-curl-e3.sh` con función `check` con el código esperado y
  `RESULTADO_FINAL fail=0/1`. Cubre filtro `?rol=BARBERO` (incluye barbero,
  excluye cliente), rol inválido 400, PUT usuario 200, PUT con email
  duplicado 409, PUT usuario 404, PUT horario parcial, traslape 409, PUT
  horario 404, DELETE horario 204 y repetido 404, DELETE usuario (baja
  lógica) 204, GET tras baja 404 y filtro CLIENTE sin la baja.
- El id del admin sale del login (ajuste del 2026-11-22 al armar el set por
  avance; antes se asumía 1).

Verificación: script corrido en VM contra el backend E3 con todo OK
(`RESULTADO_FINAL fail=0`).
