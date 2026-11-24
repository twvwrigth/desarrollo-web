package com.example.demo;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
public class CotizacionControlador {

    @Autowired
    ICotizacion icotizacion;

    @Autowired
    IUsuario iusuario;

    @Autowired
    IServicio iservicio;

    @PostMapping("/api/cotizaciones")
    public ResponseEntity<?> crear(@RequestBody Cotizacion cotizacion) {
        if (cotizacion.getCliente() == null || cotizacion.getCliente().getId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "cliente es obligatorio"));
        }
        Optional<Usuario> cliente = iusuario.findById(cotizacion.getCliente().getId());
        if (cliente.isEmpty() || !cliente.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Cliente no encontrado"));
        }
        if (cliente.get().getRol() != Rol.CLIENTE) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "El cliente debe tener rol CLIENTE"));
        }
        // Barbero opcional, elegido por el cliente: si viene, debe existir,
        // estar activo y tener rol BARBERO; si no viene, queda en null y se
        // puede asignar después con PUT /api/cotizaciones/{id}/barbero.
        if (cotizacion.getBarbero() != null && cotizacion.getBarbero().getId() != null) {
            Optional<Usuario> barbero = iusuario.findById(cotizacion.getBarbero().getId());
            if (barbero.isEmpty() || !barbero.get().isActivo()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No encontrado", "mensaje", "Barbero no encontrado"));
            }
            if (barbero.get().getRol() != Rol.BARBERO) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Datos invalidos", "mensaje", "El barbero debe tener rol BARBERO"));
            }
            cotizacion.setBarbero(barbero.get());
        } else {
            cotizacion.setBarbero(null);
        }
        if (cotizacion.getDetalle() == null || cotizacion.getDetalle().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "La cotizacion necesita al menos un servicio en el detalle"));
        }

        BigDecimal total = BigDecimal.ZERO;
        for (CotizacionDetalle renglon : cotizacion.getDetalle()) {
            if (renglon.getServicio() == null || renglon.getServicio().getId() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Datos invalidos", "mensaje", "Cada renglon necesita un servicio"));
            }
            Optional<Servicio> servicio = iservicio.findById(renglon.getServicio().getId());
            if (servicio.isEmpty() || !servicio.get().isActivo()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No encontrado", "mensaje", "Servicio no encontrado o inactivo"));
            }
            if (renglon.getCantidad() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Datos invalidos", "mensaje", "La cantidad debe ser mayor que 0"));
            }
            if (renglon.getPrecioUnitario() == null) {
                renglon.setPrecioUnitario(servicio.get().getPrecio());
            }
            if (renglon.getPrecioUnitario().compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Datos invalidos", "mensaje", "precioUnitario no puede ser negativo"));
            }
            
            renglon.setServicio(servicio.get());
            total = total.add(renglon.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(renglon.getCantidad())));
        }

        cotizacion.setCliente(cliente.get());
        cotizacion.setTotal(total);
        cotizacion.setEstado(EstadoCotizacion.PENDIENTE);
        Cotizacion guardada = icotizacion.save(cotizacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @GetMapping("/api/cotizaciones/{id}")
    public ResponseEntity<?> obtener(@PathVariable Integer id) {
        Optional<Cotizacion> cotizacion = icotizacion.findById(id);
        if (cotizacion.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Cotizacion no encontrada"));
        }
        return ResponseEntity.ok(cotizacion.get());
    }

    @GetMapping("/api/clientes/{id}/cotizaciones")
    public ResponseEntity<?> listarPorCliente(@PathVariable Integer id) {
        Optional<Usuario> cliente = iusuario.findById(id);
        if (cliente.isEmpty() || !cliente.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Cliente no encontrado"));
        }
        return ResponseEntity.ok(icotizacion.findByClienteId(id));
    }

    // Sin clienteId devuelve todas las cotizaciones; con clienteId solo las de ese cliente.
    @GetMapping("/api/cotizaciones")
    public ResponseEntity<?> listar(@RequestParam(required = false) Integer clienteId) {
        if (clienteId != null) {
            return ResponseEntity.ok(icotizacion.findByClienteId(clienteId));
        }
        return ResponseEntity.ok(icotizacion.findAll());
    }
    @PutMapping("/api/cotizaciones/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id,
                                          @RequestBody(required = false) Map<String, String> cuerpo,
                                          @RequestHeader(value = "X-Usuario-Id", required = false) Integer usuarioId) {
        // Solo el ADMIN o un BARBERO pueden aceptar o rechazar cotizaciones:
        // el cliente la pide, la barbería la acepta o la rechaza.
        if (!esAdminOBarbero(usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Sin permiso",
                            "mensaje", "Solo el administrador o el barbero pueden aceptar o rechazar cotizaciones"));
        }
        Optional<Cotizacion> actual = icotizacion.findById(id);
        if (actual.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Cotizacion no encontrada"));
        }
        Cotizacion cotizacion = actual.get();
        String nuevoTexto = cuerpo == null ? null : cuerpo.get("estado");
        if (nuevoTexto == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "estado es obligatorio: ACEPTADA o RECHAZADA"));
        }
        EstadoCotizacion nuevo;
        try {
            nuevo = EstadoCotizacion.valueOf(nuevoTexto.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "estado no válido: ACEPTADA o RECHAZADA"));
        }
        if (cotizacion.getEstado() != EstadoCotizacion.PENDIENTE) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Conflicto",
                            "mensaje", "Solo una cotización PENDIENTE puede cambiar de estado"));
        }
        if (nuevo == EstadoCotizacion.PENDIENTE) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Conflicto",
                            "mensaje", "No se puede volver una cotización a PENDIENTE"));
        }
        cotizacion.setEstado(nuevo);
        return ResponseEntity.ok(icotizacion.save(cotizacion));
    }
    // Asigna o cambia el barbero de una cotización PENDIENTE.
    // Body: {"barberoId": 3}. Solo el cliente dueño de la cotización o el ADMIN.
    @PutMapping("/api/cotizaciones/{id}/barbero")
    public ResponseEntity<?> cambiarBarbero(@PathVariable Integer id,
                                           @RequestBody(required = false) Map<String, Integer> cuerpo,
                                           @RequestHeader(value = "X-Usuario-Id", required = false) Integer usuarioId) {
        Optional<Cotizacion> actual = icotizacion.findById(id);
        if (actual.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Cotizacion no encontrada"));
        }
        Cotizacion cotizacion = actual.get();
        Integer clienteId = cotizacion.getCliente() == null ? null : cotizacion.getCliente().getId();
        if (usuarioId == null || (!usuarioId.equals(clienteId) && !esAdmin(usuarioId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Sin permiso",
                            "mensaje", "Solo el cliente de la cotización o el administrador pueden asignar el barbero"));
        }
        Integer barberoId = cuerpo == null ? null : cuerpo.get("barberoId");
        if (barberoId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "barberoId es obligatorio"));
        }
        Optional<Usuario> barbero = iusuario.findById(barberoId);
        if (barbero.isEmpty() || !barbero.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Barbero no encontrado"));
        }
        if (barbero.get().getRol() != Rol.BARBERO) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "El barbero debe tener rol BARBERO"));
        }
        if (cotizacion.getEstado() != EstadoCotizacion.PENDIENTE) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Conflicto",
                            "mensaje", "Solo una cotización PENDIENTE puede cambiar de barbero"));
        }
        cotizacion.setBarbero(barbero.get());
        return ResponseEntity.ok(icotizacion.save(cotizacion));
    }

    // true si el usuario existe, está activo y su rol es ADMIN o BARBERO.
    private boolean esAdminOBarbero(Integer usuarioId) {
        if (usuarioId == null) {
            return false;
        }
        Optional<Usuario> usuario = iusuario.findById(usuarioId);
        return usuario.isPresent() && usuario.get().isActivo()
                && (usuario.get().getRol() == Rol.ADMIN || usuario.get().getRol() == Rol.BARBERO);
    }

    // true si el usuario existe, está activo y su rol es ADMIN.
    private boolean esAdmin(Integer usuarioId) {
        if (usuarioId == null) {
            return false;
        }
        Optional<Usuario> usuario = iusuario.findById(usuarioId);
        return usuario.isPresent() && usuario.get().isActivo() && usuario.get().getRol() == Rol.ADMIN;
    }

    @DeleteMapping("/api/cotizaciones/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        Optional<Cotizacion> actual = icotizacion.findById(id);
        if (actual.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Cotizacion no encontrada"));
        }
        if (actual.get().getEstado() != EstadoCotizacion.PENDIENTE) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Conflicto",
                            "mensaje", "Solo se puede eliminar una cotización PENDIENTE"));
        }
        icotizacion.delete(actual.get());
        return ResponseEntity.noContent().build();
    }
}
