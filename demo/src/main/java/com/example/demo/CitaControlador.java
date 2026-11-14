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

import java.util.Map;
import java.util.Optional;


@CrossOrigin(origins = "*")
@RestController
public class CitaControlador {

    @Autowired
    ICita icita;

    @Autowired
    IUsuario iusuario;

    @Autowired
    IServicio iservicio;

    @Autowired
    ICorte icorte;

    // La cita nueva siempre nace PENDIENTE
    @PostMapping("/api/citas")
    public ResponseEntity<?> crear(@RequestBody Cita cita) {
        if (cita.getCliente() == null || cita.getCliente().getId() == null
                || cita.getBarbero() == null || cita.getBarbero().getId() == null
                || cita.getServicio() == null || cita.getServicio().getId() == null
                || cita.getCorte() == null || cita.getCorte().getId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos",
                            "mensaje", "cliente, barbero, servicio y corte son obligatorios"));
        }

        Optional<Usuario> cliente = iusuario.findById(cita.getCliente().getId());
        if (cliente.isEmpty() || !cliente.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Cliente no encontrado"));
        }
        if (cliente.get().getRol() != Rol.CLIENTE) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "El cliente debe tener rol CLIENTE"));
        }

        Optional<Usuario> barbero = iusuario.findById(cita.getBarbero().getId());
        if (barbero.isEmpty() || !barbero.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Barbero no encontrado"));
        }
        if (barbero.get().getRol() != Rol.BARBERO) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "El usuario no es un barbero"));
        }

        Optional<Servicio> servicio = iservicio.findById(cita.getServicio().getId());
        if (servicio.isEmpty() || !servicio.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Servicio no encontrado o inactivo"));
        }

        Optional<Corte> corte = icorte.findById(cita.getCorte().getId());
        if (corte.isEmpty() || !corte.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Corte no encontrado o inactivo"));
        }
        if (!corte.get().getServicio().getId().equals(servicio.get().getId())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "El corte no pertenece a ese servicio"));
        }

        if (cita.getFechaHoraInicio() == null || cita.getFechaHoraFin() == null
                || !cita.getFechaHoraFin().isAfter(cita.getFechaHoraInicio())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos",
                            "mensaje", "La fechaHoraFin debe ser mayor que la fechaHoraInicio"));
        }

        // No se puede reservar al mismo barbero dos veces en el mismo horario
        // (igual que el traslape de horarios en HorarioControlador).
        for (Cita existente : icita.findByBarberoId(barbero.get().getId())) {
            if (existente.getEstado() == EstadoCita.CANCELADA) {
                continue;
            }
            boolean traslapa = cita.getFechaHoraInicio().isBefore(existente.getFechaHoraFin())
                    && cita.getFechaHoraFin().isAfter(existente.getFechaHoraInicio());
            if (traslapa) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Conflicto",
                                "mensaje", "El barbero ya tiene una cita en ese horario"));
            }
        }

        cita.setCliente(cliente.get());
        cita.setBarbero(barbero.get());
        cita.setServicio(servicio.get());
        cita.setCorte(corte.get());
        cita.setEstado(EstadoCita.PENDIENTE);
        Cita guardada = icita.save(cita);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    // E2: GET /api/citas/{id} -> 200 / 404
    @GetMapping("/api/citas/{id}")
    public ResponseEntity<?> obtener(@PathVariable Integer id) {
        Optional<Cita> cita = icita.findById(id);
        if (cita.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Cita no encontrada"));
        }
        return ResponseEntity.ok(cita.get());
    }

    // E2: GET /api/clientes/{id}/citas -> 200 / 404
    @GetMapping("/api/clientes/{id}/citas")
    public ResponseEntity<?> listarPorCliente(@PathVariable Integer id) {
        Optional<Usuario> cliente = iusuario.findById(id);
        if (cliente.isEmpty() || !cliente.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Cliente no encontrado"));
        }
        return ResponseEntity.ok(icita.findByClienteId(id));
    }

    // E2: GET /api/barberos/{id}/citas -> 200 / 400 / 404
    @GetMapping("/api/barberos/{id}/citas")
    public ResponseEntity<?> listarPorBarbero(@PathVariable Integer id) {
        Optional<Usuario> barbero = iusuario.findById(id);
        if (barbero.isEmpty() || !barbero.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Barbero no encontrado"));
        }
        if (barbero.get().getRol() != Rol.BARBERO) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "El usuario no es un barbero"));
        }
        return ResponseEntity.ok(icita.findByBarberoId(id));
    }
}
