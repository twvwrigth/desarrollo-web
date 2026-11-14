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

import java.util.Map;
import java.util.Optional;

//crear un corte es solo para ADMIN. El corte siempre pertenece a un servicio
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

    // E2: POST /api/cortes -> 201 / 400 / 403 / 404
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

    // E2: GET /api/cortes -> 200 (solo activos)
    @GetMapping("/api/cortes")
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(icorte.findByActivoTrue());
    }

    // E2: GET /api/cortes/{id} -> 200 / 404
    @GetMapping("/api/cortes/{id}")
    public ResponseEntity<?> obtener(@PathVariable Integer id) {
        Optional<Corte> corte = icorte.findById(id);
        if (corte.isEmpty() || !corte.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Corte no encontrado"));
        }
        return ResponseEntity.ok(corte.get());
    }

    // E2: GET /api/servicios/{id}/cortes -> 200 / 404
    @GetMapping("/api/servicios/{id}/cortes")
    public ResponseEntity<?> listarPorServicio(@PathVariable Integer id) {
        Optional<Servicio> servicio = iservicio.findById(id);
        if (servicio.isEmpty() || !servicio.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Servicio no encontrado"));
        }
        return ResponseEntity.ok(icorte.findByServicioIdAndActivoTrue(id));
    }
}
