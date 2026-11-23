# Pruebas con cURL — Parte E2 de Erick (cotizaciones)

## 1. Cotizaciones

```bash
# POST /api/cotizaciones -> 201
# El total lo calcula el servidor: 1 x 150.00 + 2 x 80.00 = 310.00
# (el segundo renglón no trae precioUnitario, así que toma el precio
#  del servicio; el primero trae 150.00 explícito)
curl -s -X POST http://localhost:8081/api/cotizaciones \
  -H "Content-Type: application/json" \
  -d '{"cliente":{"id":2},"detalle":[{"servicio":{"id":1},"cantidad":1,"precioUnitario":150.00},{"servicio":{"id":2},"cantidad":2}]}'

# POST sin detalle -> 400
curl -s -X POST http://localhost:8081/api/cotizaciones \
  -H "Content-Type: application/json" \
  -d '{"cliente":{"id":2},"detalle":[]}'

# POST con cantidad 0 -> 400
curl -s -X POST http://localhost:8081/api/cotizaciones \
  -H "Content-Type: application/json" \
  -d '{"cliente":{"id":2},"detalle":[{"servicio":{"id":1},"cantidad":0}]}'

# POST con servicio inexistente -> 404
curl -s -X POST http://localhost:8081/api/cotizaciones \
  -H "Content-Type: application/json" \
  -d '{"cliente":{"id":2},"detalle":[{"servicio":{"id":999},"cantidad":1}]}'

# GET /api/cotizaciones/{id} -> 200 (trae el detalle)
curl -s http://localhost:8081/api/cotizaciones/1

# GET /api/clientes/{id}/cotizaciones -> 200
curl -s http://localhost:8081/api/clientes/2/cotizaciones
```