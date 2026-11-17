package com.example.demo;

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
//Los cortes solo los toca un ADMIN.
@CrossOrigin(origins = "*")
@RestController
public class CorteControlador {

    @Autowired
    ICorte icorte;

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
    @PostMapping("/api/cortes")
    public ResponseEntity<?> crear(@RequestBody Corte corte,
                                   @RequestHeader(value = "X-Usuario-Id", required = false) Integer adminId) {
        if (!esAdmin(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Sin permiso", "mensaje", "Solo un administrador puede crear cortes"));
        }
        if (corte.getNombre() == null || corte.getNombre().isBlank()
                || corte.getServicio() == null || corte.getServicio().getId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "nombre y servicio son obligatorios"));
        }
        Optional<Servicio> servicio = iservicio.findById(corte.getServicio().getId());
        if (servicio.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Servicio no encontrado"));
        }
        if (!servicio.get().isActivo()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "El servicio no esta activo"));
        }
        corte.setServicio(servicio.get());
        corte.setActivo(true);
        Corte guardado = icorte.save(corte);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    //GET /api/cortes
    @GetMapping("/api/cortes")
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(icorte.findByActivoTrue());
    }
    @GetMapping("/api/cortes/{id}")
    public ResponseEntity<?> obtener(@PathVariable Integer id) {
        Optional<Corte> corte = icorte.findById(id);
        if (corte.isEmpty() || !corte.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Corte no encontrado"));
        }
        return ResponseEntity.ok(corte.get());
    }
    @GetMapping("/api/servicios/{id}/cortes")
    public ResponseEntity<?> listarPorServicio(@PathVariable Integer id) {
        Optional<Servicio> servicio = iservicio.findById(id);
        if (servicio.isEmpty() || !servicio.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Servicio no encontrado"));
        }
        return ResponseEntity.ok(icorte.findByServicioIdAndActivoTrue(id));
    }

    @PutMapping("/api/cortes/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id,
                                        @RequestBody Corte cambios,
                                        @RequestHeader(value = "X-Usuario-Id", required = false) Integer adminId) {
        if (!esAdmin(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Sin permiso", "mensaje", "Solo un administrador puede editar cortes"));
        }
        Optional<Corte> actual = icorte.findById(id);
        if (actual.isEmpty() || !actual.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Corte no encontrado"));
        }
        Corte corte = actual.get();
        if (cambios.getNombre() != null) {
            String nombre = cambios.getNombre().trim();
            if (nombre.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Datos invalidos", "mensaje", "nombre no puede estar vacío"));
            }
            corte.setNombre(nombre);
        }
        if (cambios.getDescripcion() != null) {
            corte.setDescripcion(cambios.getDescripcion());
        }
        if (cambios.getImagenUrl() != null) {
            corte.setImagenUrl(cambios.getImagenUrl());
        }
        if (cambios.getOverlayUrl() != null) {
            corte.setOverlayUrl(cambios.getOverlayUrl());
        }
        if (cambios.getServicio() != null && cambios.getServicio().getId() != null) {
            Optional<Servicio> servicio = iservicio.findById(cambios.getServicio().getId());
            if (servicio.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No encontrado", "mensaje", "Servicio no encontrado"));
            }
            if (!servicio.get().isActivo()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Datos invalidos", "mensaje", "El servicio no esta activo"));
            }
            corte.setServicio(servicio.get());
        }
        return ResponseEntity.ok(icorte.save(corte));
    }
    @DeleteMapping("/api/cortes/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id,
                                      @RequestHeader(value = "X-Usuario-Id", required = false) Integer adminId) {
        if (!esAdmin(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Sin permiso", "mensaje", "Solo un administrador puede eliminar cortes"));
        }
        Optional<Corte> actual = icorte.findById(id);
        if (actual.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Corte no encontrado"));
        }
        Corte corte = actual.get();
        corte.setActivo(false);
        icorte.save(corte);
        return ResponseEntity.noContent().build();
    }
}
