package com.example.demo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Compatible con la Cita REAL de Erick (commit 2a6af52, 13 nov 21:46):
// getEstado() devuelve el enum EstadoCita, no String, y no hay
// getClienteId()/getBarberoId(): el cliente es Usuario (getCliente().getId()).
// La Cita provisional ya NO se usa: borrar Cita.java/ICita.java locales
// antes de hacer git pull, o el pull va a chocar con los de Erick.
@CrossOrigin(origins = "*")
@RestController
public class ResenaControlador {

    @Autowired
    IResena iresena;

    @Autowired
    ICita icita;

    // E2: POST /api/resenas -> 201 / 400 / 403 / 404 / 409
    // Reglas: RF-20 / RN-05: solo se resena una cita COMPLETADA, una resena por
    // cita y solo el cliente de esa cita. Calificacion solo de 1 a 5.
    @PostMapping("/api/resenas")
    public ResponseEntity<?> crear(@RequestBody Resena resena,
                                   @RequestHeader(value = "X-Usuario-Id", required = false) Integer usuarioId) {
        if (resena.getCitaId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "citaId es obligatorio"));
        }

        if (resena.getCalificacion() == null || resena.getCalificacion() < 1 || resena.getCalificacion() > 5) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "La calificacion debe ser de 1 a 5"));
        }

        Optional<Cita> cita = icita.findById(resena.getCitaId());

        if (cita.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Cita no encontrada"));
        }

        if (cita.get().getEstado() != EstadoCita.COMPLETADA) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Conflicto", "mensaje", "Solo se puede resenar una cita COMPLETADA"));
        }

        // RN-05: solo el cliente de esa cita. Si el frontend no manda el header,
        // no se puede comprobar: se deja pasar, igual que el resto del equipo
        // documenta X-Usuario-Id como simplificacion sin Spring Security.
        if (usuarioId != null && cita.get().getCliente() != null
                && !usuarioId.equals(cita.get().getCliente().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Sin permiso", "mensaje", "Solo el cliente de la cita puede resenarla"));
        }

        if (iresena.existsByCitaId(resena.getCitaId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Conflicto", "mensaje", "Esa cita ya tiene una resena"));
        }

        Resena guardada = iresena.save(resena);

        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    // E2: GET /api/resenas?barberoId= -> 200
    // RF-21: consultar resenas por barbero. Sin barberoId, lista todas.
    // El barbero se resuelve por la cita (cita.barbero_id), segun esquema.sql.
    @GetMapping("/api/resenas")
    public ResponseEntity<?> listar(@RequestParam(value = "barberoId", required = false) Integer barberoId) {
        if (barberoId == null) {
            return ResponseEntity.ok(iresena.findAll());
        }

        List<Integer> citaIds = icita.findByBarberoId(barberoId).stream()
                .map(Cita::getId)
                .collect(Collectors.toList());

        if (citaIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(iresena.findByCitaIdIn(citaIds));
    }
}