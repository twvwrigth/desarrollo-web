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
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
public class ServicioControlador {

    @Autowired
    IServicio iservicio;

    @Autowired
    IUsuario iusuario;

    private boolean esAdmin(Integer adminId) {
        if (adminId == null) {
            return false;
        }
        Optional<Usuario> admin = iusuario.findById(adminId);
        return admin.isPresent() && admin.get().isActivo() && admin.get().getRol() == Rol.ADMIN;
    }

    // E2: POST /api/servicios -> 201 / 400 / 403 / 409
    @PostMapping("/api/servicios")
    public ResponseEntity<?> crear(@RequestBody Servicio servicio,
                                   @RequestHeader(value = "X-Usuario-Id", required = false) Integer adminId) {
        if (!esAdmin(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Sin permiso", "mensaje", "Solo un administrador puede crear servicios"));
        }
        if (servicio.getNombre() == null || servicio.getNombre().isBlank()
                || servicio.getPrecio() == null
                || servicio.getDuracionMin() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "nombre, precio y duracionMin son obligatorios"));
        }
        if (servicio.getDuracionMin() <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "duracionMin debe ser mayor que 0"));
        }
        if (servicio.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "precio no puede ser negativo"));
        }
        String nombre = servicio.getNombre().trim();
        servicio.setNombre(nombre);
        if (iservicio.existsByNombre(nombre)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Conflicto", "mensaje", "Ya existe un servicio con ese nombre"));
        }
        servicio.setActivo(true);
        Servicio guardado = iservicio.save(servicio);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }
    @GetMapping("/api/servicios")
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(iservicio.findByActivoTrue());
    }

    @GetMapping("/api/servicios/{id}")
    public ResponseEntity<?> obtener(@PathVariable Integer id) {
        Optional<Servicio> servicio = iservicio.findById(id);
        if (servicio.isEmpty() || !servicio.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Servicio no encontrado"));
        }
        return ResponseEntity.ok(servicio.get());
    }

    //Se actualizan solo los campos que vienen en el JSON 
    @PutMapping("/api/servicios/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id,
                                        @RequestBody Servicio cambios,
                                        @RequestHeader(value = "X-Usuario-Id", required = false) Integer adminId) {
        if (!esAdmin(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Sin permiso", "mensaje", "Solo un administrador puede editar servicios"));
        }
        Optional<Servicio> actual = iservicio.findById(id);
        if (actual.isEmpty() || !actual.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Servicio no encontrado"));
        }
        Servicio servicio = actual.get();
        if (cambios.getNombre() != null) {
            String nombre = cambios.getNombre().trim();
            if (nombre.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Datos invalidos", "mensaje", "nombre no puede estar vacío"));
            }
            // El nombre es único: solo choca si OTRO servicio ya lo tiene.
            if (!nombre.equals(servicio.getNombre()) && iservicio.existsByNombre(nombre)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Conflicto", "mensaje", "Ya existe un servicio con ese nombre"));
            }
            servicio.setNombre(nombre);
        }
        if (cambios.getDescripcion() != null) {
            servicio.setDescripcion(cambios.getDescripcion());
        }
        if (cambios.getDuracionMin() != null) {
            if (cambios.getDuracionMin() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Datos invalidos", "mensaje", "duracionMin debe ser mayor que 0"));
            }
            servicio.setDuracionMin(cambios.getDuracionMin());
        }
        if (cambios.getPrecio() != null) {
            if (cambios.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Datos invalidos", "mensaje", "precio no puede ser negativo"));
            }
            servicio.setPrecio(cambios.getPrecio());
        }
        return ResponseEntity.ok(iservicio.save(servicio));
    }
    @DeleteMapping("/api/servicios/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id,
                                      @RequestHeader(value = "X-Usuario-Id", required = false) Integer adminId) {
        if (!esAdmin(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Sin permiso", "mensaje", "Solo un administrador puede eliminar servicios"));
        }
        Optional<Servicio> actual = iservicio.findById(id);
        if (actual.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Servicio no encontrado"));
        }
        Servicio servicio = actual.get();
        servicio.setActivo(false);
        iservicio.save(servicio);
        return ResponseEntity.noContent().build();
    }
}
