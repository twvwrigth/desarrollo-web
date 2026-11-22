# Pruebas con cURL — Reseñas E2 (José Emmanuel)
# Backend corriendo en WSL: ./mvnw spring-boot:run  (puerto 8081)
# Base: barberia_db. Los inserts van directo en MySQL porque el endpoint
# POST /api/citas de Erick todavía no existe en el repo.

## 0. Datos de prueba (en MySQL, una sola vez)
# Ajusta los ids a los que ya tengas: cliente 4 y barbero 2 son los del
# ejemplo de endpoints.md. La cita 15 debe quedar COMPLETADA.
#
# INSERT INTO servicios (nombre, descripcion, duracion_min, precio)
#   VALUES ('Corte clásico', 'Corte con tijera y máquina', 30, 150.00);
# INSERT INTO citas (cliente_id, barbero_id, servicio_id, fecha_hora_inicio,
#   fecha_hora_fin, estado) VALUES (4, 2, 1, '2026-11-20 11:00:00',
#   '2026-11-20 11:30:00', 'COMPLETADA');
# -- anota el id real de esa cita y úsalo como CITaid abajo (aquí se usa 15).

## 1. Camino feliz — 201
curl -X POST http://localhost:8081/api/resenas \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: 4" \
  -d '{"citaId": 15, "calificacion": 5, "comentario": "Excelente corte y buen trato"}'

## 2. Calificación inválida — 400
curl -X POST http://localhost:8081/api/resenas \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: 4" \
  -d '{"citaId": 15, "calificacion": 6, "comentario": "No debería pasar"}'

## 3. Cita inexistente — 404
curl -X POST http://localhost:8081/api/resenas \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: 4" \
  -d '{"citaId": 999, "calificacion": 5, "comentario": "No existe"}'

## 4. Reseña duplicada (repetir el caso 1) — 409
curl -X POST http://localhost:8081/api/resenas \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: 4" \
  -d '{"citaId": 15, "calificacion": 4, "comentario": "Segunda reseña de la misma cita"}'

## 5. Cita no completada — 409
# (crea otra cita en estado PENDIENTE y prueba con su id)
curl -X POST http://localhost:8081/api/resenas \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: 4" \
  -d '{"citaId": 16, "calificacion": 5, "comentario": "Cita pendiente"}'

## 6. Reseña de otro cliente — 403
curl -X POST http://localhost:8081/api/resenas \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: 99" \
  -d '{"citaId": 15, "calificacion": 5, "comentario": "No soy el cliente"}'

## 7. Listar reseñas de un barbero — 200
curl "http://localhost:8081/api/resenas?barberoId=2"

## 8. Listar todas — 200
curl http://localhost:8081/api/resenas
