# Respuesta de la IA — Uso_01 (solo cotizaciones)

Mano generó la parte de cotizaciones de mi módulo E2, siguiendo el estilo del
demo de José Luis (un solo paquete `com.example.demo`, entidad + `CrudRepository`
+ controlador, sin DTOs ni capa de servicios):

- 2 entidades: `Cotizacion` (id, cliente, fecha, total, estado, detalle) y
  `CotizacionDetalle` (id, servicio, cantidad, precioUnitario). Relación de un
  solo sentido: `Cotizacion.detalle` es `@OneToMany` con `@JoinColumn(name =
  "cotizacion_id")`; `CotizacionDetalle` no tiene campo `cotizacion` (sin
  anotaciones Jackson).
- 2 repositorios: `ICotizacion` (con `findByClienteId`) e `ICotizacionDetalle`.
- 1 controlador, `CotizacionControlador`, con 3 endpoints: POST
  /api/cotizaciones (201/400/404), GET /api/cotizaciones/{id} (con su detalle)
  y GET /api/clientes/{id}/cotizaciones.
- Reglas: el cliente debe existir y ser CLIENTE; el detalle trae al menos 1
  renglón; cada servicio debe existir y estar activo, cantidad > 0; si el
  precio unitario viene nulo se toma el precio del servicio; el total se
  calcula en el servidor (el total que manda el cliente se ignora); la
  cotización nace PENDIENTE.
- Pruebas: `ErickE2RepositoryTest.java` (H2: recarga la cotización, detalle de
  1 renglón, total 300.00) y `CotizacionJsonTest.java` (el JSON trae "detalle"
  y 150.00, y no trae "cotizacion").
- Guía y script de cURL para cotizaciones (`PRUEBAS-CURL-Erick-E2.md`,
  `pruebas-curl-erick.sh`): crear con total 310.00 (1×150 + 2×80) y los dos GETs.
- Verificación: `mvn test` BUILD SUCCESS, 2 pruebas, 2026-11-13 22:36.
- GET /api/cotizaciones?clienteId=: con parámetro devuelve solo las del cliente
  (`findByClienteId`); sin parámetro, todas (200).
- PUT /api/cotizaciones/{id}/estado: solo PENDIENTE → ACEPTADA o RECHAZADA
  (200). Estado inválido o ausente → 400; si ya no está PENDIENTE → 409;
  mandar PENDIENTE como destino → 409. Sin autenticación, según los códigos
  del reparto.
- DELETE /api/cotizaciones/{id}: borrado físico (la cotización no tiene columna
  `activo` en el diagrama ER, así que no hay baja lógica posible), solo si está
  PENDIENTE (204); si está ACEPTADA o RECHAZADA → 409; el detalle se borra con
  ella por el `cascade` del `@OneToMany`.
- Prueba `cotizacionesFiltroEstadoYDelete` en `ErickE3ControllerTest.java`
  (MockMvc, H2), una de las 8 pruebas nuevas del E3.
- Guía y script de cURL para cotizaciones (`PRUEBAS-CURL-Erick-E3.md`,
  `pruebas-curl-erick-e3.sh`): filtro y estado (6 casos) y eliminar (4 casos),
  más la creación con total 310.00 (1×150 + 2×80).
- Verificación: `mvn test` BUILD SUCCESS, 10 pruebas (2 de E2 + 8 nuevas de
  E3), 2026-11-16.

