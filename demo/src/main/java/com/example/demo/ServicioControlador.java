package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
public class ServicioControlador {

    @Autowired
    IServicio iservicio;

    @Autowired
    IUsuario iusuario;
    //Verifica que el id del header sea de un usuario activo con rol ADMIN
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

    // E2: GET /api/servicios -> 200 (solo activos, es el catálogo del frontend)
    @GetMapping("/api/servicios")
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(iservicio.findByActivoTrue());
    }

    // E2: GET /api/servicios/{id} -> 200 / 404
    @GetMapping("/api/servicios/{id}")
    public ResponseEntity<?> obtener(@PathVariable Integer id) {
        Optional<Servicio> servicio = iservicio.findById(id);
        if (servicio.isEmpty() || !servicio.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Servicio no encontrado"));
        }
        return ResponseEntity.ok(servicio.get());
    }
}
