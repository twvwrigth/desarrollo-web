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
public class HorarioControlador {

    @Autowired
    IHorarioBarbero ihorario;

    @Autowired
    IUsuario iusuario;

    // E2: GET /api/barberos/{id}/horarios -> 200 / 404
    @GetMapping("/api/barberos/{barberoId}/horarios")
    public ResponseEntity<?> listar(@PathVariable Integer barberoId) {
        Optional<Usuario> barbero = iusuario.findById(barberoId);
        if (barbero.isEmpty() || !barbero.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Barbero no encontrado"));
        }
        if (barbero.get().getRol() != Rol.BARBERO) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "El usuario no es un barbero"));
        }
        return ResponseEntity.ok(ihorario.findByBarberoId(barberoId));
    }

    // E2: POST /api/barberos/{id}/horarios -> 201 / 400 / 404 / 409
    @PostMapping("/api/barberos/{barberoId}/horarios")
    public ResponseEntity<?> agregar(@PathVariable Integer barberoId, @RequestBody HorarioBarbero horario) {
        Optional<Usuario> barbero = iusuario.findById(barberoId);
        if (barbero.isEmpty() || !barbero.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Barbero no encontrado"));
        }
        if (barbero.get().getRol() != Rol.BARBERO) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "El usuario no es un barbero"));
        }
        if (horario.getDiaSemana() < 1 || horario.getDiaSemana() > 7) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "diaSemana debe ser 1 (lunes) a 7 (domingo)"));
        }
        if (horario.getHoraInicio() == null || horario.getHoraFin() == null
                || !horario.getHoraFin().isAfter(horario.getHoraInicio())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "La horaFin debe ser mayor que la horaInicio"));
        }

        // Evita bloques traslapados el mismo dia para el mismo barbero
        for (HorarioBarbero existente : ihorario.findByBarberoIdAndDiaSemana(barberoId, horario.getDiaSemana())) {
            boolean traslapa = horario.getHoraInicio().isBefore(existente.getHoraFin())
                    && horario.getHoraFin().isAfter(existente.getHoraInicio());
            if (traslapa) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Conflicto", "mensaje", "El horario se traslapa con un bloque existente ese dia"));
            }
        }

        horario.setBarbero(barbero.get());
        HorarioBarbero guardado = ihorario.save(horario);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }
}
