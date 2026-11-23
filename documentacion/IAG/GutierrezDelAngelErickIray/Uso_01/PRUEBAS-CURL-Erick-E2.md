# Pruebas con cURL — Parte E2 de Erick (servicios, cortes, citas)

Backend en `http://localhost:8081/api`, con MySQL corriendo y la base
`barberia_db` creada. Si es la primera vez, el admin de demo se crea solo
al arrancar (`admin@barberia.com` / `admin123` — cambiarlo antes de la entrega).

> Las fechas de los ejemplos son de ejemplo: usa fechas futuras reales
> cuando pruebes, y cambia los ids por los que te devuelva cada respuesta.

## 0. Preparar ids (se reutilizan en todo el archivo)

```bash
# Login del admin de demo -> anota el "id" (normalmente 1)
curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@barberia.com","password":"admin123"}'

# Registrar un cliente -> anota el "id"
curl -s -X POST http://localhost:8081/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Erick Cliente","email":"cliente@barberia.com","password":"cliente123","telefono":"2281000000"}'

# Crear un barbero (solo ADMIN, usa el id del admin en el header) -> anota el "id"
curl -s -X POST http://localhost:8081/api/usuarios \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: 1" \
  -d '{"nombre":"Barbero Uno","email":"barbero@barberia.com","password":"barbero123","telefono":"2281000001","rol":"BARBERO"}'
```

## 1. Servicios

```bash
# POST /api/servicios -> 201 (solo ADMIN)
curl -s -X POST http://localhost:8081/api/servicios \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: 1" \
  -d '{"nombre":"Corte clásico","descripcion":"Corte tradicional con tijera y máquina","duracionMin":30,"precio":150.00}'

# POST repetido con el mismo nombre -> 409 (nombre único)
curl -s -X POST http://localhost:8081/api/servicios \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: 1" \
  -d '{"nombre":"Corte clásico","descripcion":"Otro","duracionMin":30,"precio":150.00}'

# POST sin header de admin -> 403
curl -s -X POST http://localhost:8081/api/servicios \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Barba","descripcion":"Arreglo de barba","duracionMin":20,"precio":80.00}'

# POST con duracionMin 0 -> 400
curl -s -X POST http://localhost:8081/api/servicios \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: 1" \
  -d '{"nombre":"Tinte","descripcion":"Tinte","duracionMin":0,"precio":200.00}'

# GET /api/servicios -> 200 (catálogo, solo activos)
curl -s http://localhost:8081/api/servicios

# GET /api/servicios/{id} -> 200
curl -s http://localhost:8081/api/servicios/1

# GET /api/servicios/{id} inexistente -> 404
curl -s http://localhost:8081/api/servicios/999
```

## 2. Cortes

```bash
# POST /api/cortes -> 201 (solo ADMIN; el servicio debe existir)
curl -s -X POST http://localhost:8081/api/cortes \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: 1" \
  -d '{"nombre":"Fade bajo","descripcion":"Degradado bajo con navaja","servicio":{"id":1},"imagenUrl":"https://ejemplo.com/fade.jpg","overlayUrl":"https://ejemplo.com/fade-overlay.png"}'

# POST con servicio inexistente -> 404
curl -s -X POST http://localhost:8081/api/cortes \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: 1" \
  -d '{"nombre":"Moicano","descripcion":"Moicano","servicio":{"id":999}}'

# GET /api/cortes -> 200 (solo activos)
curl -s http://localhost:8081/api/cortes

# GET /api/cortes/{id} -> 200
curl -s http://localhost:8081/api/cortes/1

# GET /api/servicios/{id}/cortes -> 200 (cortes de ese servicio)
curl -s http://localhost:8081/api/servicios/1/cortes
```

## 3. Citas

```bash
# POST /api/citas -> 201 (nace PENDIENTE)
curl -s -X POST http://localhost:8081/api/citas \
  -H "Content-Type: application/json" \
  -d '{"cliente":{"id":2},"barbero":{"id":3},"servicio":{"id":1},"corte":{"id":1},"fechaHoraInicio":"2026-11-20T10:00:00","fechaHoraFin":"2026-11-20T10:30:00","notas":"Con navaja, por favor"}'

# POST en el mismo horario del mismo barbero -> 409 (traslape)
curl -s -X POST http://localhost:8081/api/citas \
  -H "Content-Type: application/json" \
  -d '{"cliente":{"id":2},"barbero":{"id":3},"servicio":{"id":1},"corte":{"id":1},"fechaHoraInicio":"2026-11-20T10:15:00","fechaHoraFin":"2026-11-20T10:45:00"}'

# POST con fin antes que inicio -> 400
curl -s -X POST http://localhost:8081/api/citas \
  -H "Content-Type: application/json" \
  -d '{"cliente":{"id":2},"barbero":{"id":3},"servicio":{"id":1},"corte":{"id":1},"fechaHoraInicio":"2026-11-20T11:00:00","fechaHoraFin":"2026-11-20T10:30:00"}'

# POST con un corte de otro servicio -> 400
# (primero crea un segundo servicio y un corte de ese servicio, luego
#  pide una cita con servicio 1 y el corte del servicio 2)

# POST con cliente inexistente -> 404
curl -s -X POST http://localhost:8081/api/citas \
  -H "Content-Type: application/json" \
  -d '{"cliente":{"id":999},"barbero":{"id":3},"servicio":{"id":1},"corte":{"id":1},"fechaHoraInicio":"2026-11-21T10:00:00","fechaHoraFin":"2026-11-21T10:30:00"}'

# GET /api/citas/{id} -> 200
curl -s http://localhost:8081/api/citas/1

# GET /api/clientes/{id}/citas -> 200
curl -s http://localhost:8081/api/clientes/2/citas

# GET /api/barberos/{id}/citas -> 200
curl -s http://localhost:8081/api/barberos/3/citas
```
