package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

//El cliente manda su id y el detalle con los servicios y cantidades
@CrossOrigin(origins = "*")
@RestController
public class CotizacionControlador {

    @Autowired
    ICotizacion icotizacion;

    @Autowired
    IUsuario iusuario;

    @Autowired
    IServicio iservicio;

    // E2: POST /api/cotizaciones -> 201 / 400 / 404
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
            renglon.setCotizacion(cotizacion);
            total = total.add(renglon.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(renglon.getCantidad())));
        }

        cotizacion.setCliente(cliente.get());
        cotizacion.setTotal(total);
        cotizacion.setEstado(EstadoCotizacion.PENDIENTE);
        Cotizacion guardada = icotizacion.save(cotizacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    // E2: GET /api/cotizaciones/{id} -> 200 / 404
    @GetMapping("/api/cotizaciones/{id}")
    public ResponseEntity<?> obtener(@PathVariable Integer id) {
        Optional<Cotizacion> cotizacion = icotizacion.findById(id);
        if (cotizacion.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Cotizacion no encontrada"));
        }
        return ResponseEntity.ok(cotizacion.get());
    }

    // E2: GET /api/clientes/{id}/cotizaciones -> 200 / 404
    @GetMapping("/api/clientes/{id}/cotizaciones")
    public ResponseEntity<?> listarPorCliente(@PathVariable Integer id) {
        Optional<Usuario> cliente = iusuario.findById(id);
        if (cliente.isEmpty() || !cliente.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Cliente no encontrado"));
        }
        return ResponseEntity.ok(icotizacion.findByClienteId(id));
    }
}
