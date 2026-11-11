# Definición de endpoints REST — Sistema Gestor de Citas para Barbería

## Convenciones

- **Base:** `/api` · **Formato:** JSON (`Content-Type: application/json`).
- **Filtros** (GET) por *query string*; **datos de alta y edición** (POST/PUT) en el cuerpo JSON.
- **Fechas:** `fecha` como `YYYY-MM-DD`; `fechaHoraInicio` como `YYYY-MM-DDTHH:mm:ss`.
- **Hito:** **E2** = backend inicial (POST y GET, pruebas con cURL). **E3** = API completa (PUT, DELETE, filtros, validaciones y códigos HTTP).
- **Autenticación (decisión a confirmar):** sin Spring Security. El login devuelve los datos del usuario, el frontend los guarda en `sessionStorage` y envía el header `X-Usuario-Id`; el backend usa ese id para validar el rol en los endpoints de administración. Es una simplificación y debe documentarse como limitación.

## Códigos HTTP

| Código | Uso |
|---|---|
| 200 | Consulta o actualización correcta |
| 201 | Recurso creado |
| 204 | Eliminación o baja correcta, sin cuerpo |
| 400 | Datos inválidos o faltantes |
| 401 | Credenciales incorrectas |
| 403 | El rol no tiene permiso |
| 404 | Recurso no encontrado |
| 409 | Conflicto (email repetido, cita traslapada, reseña duplicada) |

**Formato de error:**
```json
{ "error": "Conflicto", "mensaje": "El barbero ya tiene una cita en ese horario" }
```

---

## 1. Usuarios y acceso

| Método | Ruta | Descripción | Respuestas | Hito |
|---|---|---|---|---|
| POST | `/api/auth/login` | Inicia sesión | 200, 401 | E2 |
| POST | `/api/usuarios` | Registra cliente (o barbero, si lo hace el admin) | 201, 400, 409 | E2 |
| GET | `/api/usuarios/{id}` | Consulta un usuario | 200, 404 | E2 |
| GET | `/api/usuarios?rol=BARBERO` | Lista usuarios, filtrable por rol | 200 | E3 |
| PUT | `/api/usuarios/{id}` | Edita perfil | 200, 400, 404, 409 | E3 |
| DELETE | `/api/usuarios/{id}` | Baja lógica (`activo = false`) | 204, 404 | E3 |

```json
// POST /api/auth/login
{ "email": "ana@correo.com", "password": "secreto123" }
// 200
{ "id": 4, "nombre": "Ana", "rol": "CLIENTE" }

// POST /api/usuarios
{ "nombre": "Ana", "email": "ana@correo.com", "password": "secreto123", "telefono": "2281234567" }
```

## 2. Servicios

| Método | Ruta | Descripción | Respuestas | Hito |
|---|---|---|---|---|
| GET | `/api/servicios` | Lista servicios (`?activo=true`) | 200 | E2 |
| GET | `/api/servicios/{id}` | Consulta un servicio | 200, 404 | E2 |
| POST | `/api/servicios` | Crea servicio (admin) | 201, 400, 403, 409 | E2 |
| PUT | `/api/servicios/{id}` | Edita servicio (admin) | 200, 400, 403, 404 | E3 |
| DELETE | `/api/servicios/{id}` | Baja lógica (admin) | 204, 403, 404 | E3 |

```json
// POST /api/servicios
{ "nombre": "Corte clásico", "descripcion": "Tijera y máquina", "duracionMin": 30, "precio": 150.00 }
```

## 3. Catálogo de cortes

| Método | Ruta | Descripción | Respuestas | Hito |
|---|---|---|---|---|
| GET | `/api/cortes` | Lista cortes (`?servicioId=` y `?nombre=`) | 200 | E2 |
| GET | `/api/cortes/{id}` | Consulta un corte | 200, 404 | E2 |
| POST | `/api/cortes` | Crea corte (admin) | 201, 400, 403 | E2 |
| PUT | `/api/cortes/{id}` | Edita corte (admin) | 200, 400, 403, 404 | E3 |
| DELETE | `/api/cortes/{id}` | Baja lógica (admin) | 204, 403, 404 | E3 |

```json
// POST /api/cortes
{ "nombre": "Fade", "descripcion": "Degradado en laterales", "imagenUrl": "img/cortes/fade.jpg",
  "overlayUrl": "img/overlays/fade.png", "servicioId": 1 }
```

Las imágenes viven en `frontend/img/`; el backend solo guarda la ruta. En el MVP no hay endpoint de subida de archivos.

## 4. Horarios de barberos

| Método | Ruta | Descripción | Respuestas | Hito |
|---|---|---|---|---|
| GET | `/api/barberos/{id}/horarios` | Horario semanal de un barbero | 200, 404 | E2 |
| POST | `/api/barberos/{id}/horarios` | Agrega un bloque de horario | 201, 400, 404, 409 | E2 |
| PUT | `/api/horarios/{id}` | Edita un bloque | 200, 400, 404 | E3 |
| DELETE | `/api/horarios/{id}` | Elimina un bloque | 204, 404 | E3 |

```json
// POST /api/barberos/2/horarios
{ "diaSemana": 1, "horaInicio": "10:00", "horaFin": "18:00" }
```

## 5. Citas

| Método | Ruta | Descripción | Respuestas | Hito |
|---|---|---|---|---|
| GET | `/api/citas/disponibilidad?barberoId=&servicioId=&fecha=` | Horas libres del barbero ese día | 200, 400, 404 | E2 |
| POST | `/api/citas` | Agenda una cita | 201, 400, 404, 409 | E2 |
| GET | `/api/citas/{id}` | Consulta una cita | 200, 404 | E2 |
| GET | `/api/citas?clienteId=&barberoId=&fecha=&estado=` | Lista citas con filtros | 200 | E3 |
| PUT | `/api/citas/{id}` | Reprograma (fecha, barbero, notas) | 200, 400, 404, 409 | E3 |
| PUT | `/api/citas/{id}/estado` | Cambia el estado | 200, 400, 403, 404, 409 | E3 |
| DELETE | `/api/citas/{id}` | Cancela la cita (`estado = CANCELADA`) | 204, 404, 409 | E3 |

```json
// POST /api/citas
{ "clienteId": 4, "barberoId": 2, "servicioId": 1, "corteId": 3,
  "fechaHoraInicio": "2026-11-20T11:00:00", "notas": "Sin máquina en las patillas" }
// 201
{ "id": 15, "estado": "PENDIENTE", "fechaHoraInicio": "2026-11-20T11:00:00",
  "fechaHoraFin": "2026-11-20T11:30:00" }

// PUT /api/citas/15/estado
{ "estado": "CONFIRMADA" }

// GET /api/citas/disponibilidad?barberoId=2&servicioId=1&fecha=2026-11-20
{ "fecha": "2026-11-20", "horasLibres": ["10:00", "10:30", "11:30", "12:00"] }
```

## 6. Cotizaciones

| Método | Ruta | Descripción | Respuestas | Hito |
|---|---|---|---|---|
| POST | `/api/cotizaciones` | Genera cotización y calcula el total | 201, 400, 404 | E2 |
| GET | `/api/cotizaciones/{id}` | Consulta con su detalle | 200, 404 | E2 |
| GET | `/api/cotizaciones?clienteId=` | Lista cotizaciones del cliente | 200 | E3 |
| PUT | `/api/cotizaciones/{id}/estado` | Acepta o rechaza | 200, 400, 404, 409 | E3 |
| DELETE | `/api/cotizaciones/{id}` | Elimina (solo si está PENDIENTE) | 204, 404, 409 | E3 |

```json
// POST /api/cotizaciones
{ "clienteId": 4, "items": [ { "servicioId": 1, "cantidad": 1 }, { "servicioId": 3, "cantidad": 1 } ] }
// 201
{ "id": 7, "estado": "PENDIENTE", "total": 250.00,
  "items": [ { "servicioId": 1, "cantidad": 1, "precioUnitario": 150.00 },
             { "servicioId": 3, "cantidad": 1, "precioUnitario": 100.00 } ] }
```

## 7. Reseñas

| Método | Ruta | Descripción | Respuestas | Hito |
|---|---|---|---|---|
| POST | `/api/resenas` | Crea reseña de una cita COMPLETADA | 201, 400, 404, 409 | E2 |
| GET | `/api/resenas?barberoId=` | Lista reseñas de un barbero | 200 | E2 |
| GET | `/api/resenas/resumen?barberoId=` | Promedio y total de reseñas | 200, 404 | E3 |
| PUT | `/api/resenas/{id}` | Edita su reseña | 200, 400, 404 | E3 |
| DELETE | `/api/resenas/{id}` | Elimina su reseña | 204, 404 | E3 |

```json
// POST /api/resenas
{ "citaId": 15, "calificacion": 5, "comentario": "Excelente corte y buen trato" }

// GET /api/resenas/resumen?barberoId=2
{ "barberoId": 2, "promedio": 4.6, "total": 12 }
```

## 8. Probador virtual (extra)

No requiere endpoints propios. El frontend usa `GET /api/cortes` para obtener `overlayUrl`, procesa el video de la cámara en el navegador y, si el cliente decide agendar, usa `POST /api/citas` con el `corteId` elegido.