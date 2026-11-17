package com.example.demo;

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
        if (horario.getDiaSemana() == null || horario.getDiaSemana() < 1 || horario.getDiaSemana() > 7) {
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

    // E3: PUT /api/horarios/{id} -> 200 / 400 / 404
    @PutMapping("/api/horarios/{id}")
    public ResponseEntity<?> editar(@PathVariable Integer id, @RequestBody HorarioBarbero cambios) {
        Optional<HorarioBarbero> actualOpt = ihorario.findById(id);
        if (actualOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Horario no encontrado"));
        }
        HorarioBarbero actual = actualOpt.get();
        int dia = cambios.getDiaSemana() != null ? cambios.getDiaSemana() : actual.getDiaSemana();
        java.time.LocalTime inicio = cambios.getHoraInicio() != null ? cambios.getHoraInicio() : actual.getHoraInicio();
        java.time.LocalTime fin = cambios.getHoraFin() != null ? cambios.getHoraFin() : actual.getHoraFin();

        if (dia < 1 || dia > 7) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "diaSemana debe ser 1 (lunes) a 7 (domingo)"));
        }
        if (inicio == null || fin == null || !fin.isAfter(inicio)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "La horaFin debe ser mayor que la horaInicio"));
        }
        Integer barberoId = actual.getBarbero() != null ? actual.getBarbero().getId() : null;
        if (barberoId != null) {
            for (HorarioBarbero existente : ihorario.findByBarberoIdAndDiaSemana(barberoId, dia)) {
                if (existente.getId().equals(actual.getId())) {
                    continue;
                }
                boolean traslapa = inicio.isBefore(existente.getHoraFin()) && fin.isAfter(existente.getHoraInicio());
                if (traslapa) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Map.of("error", "Conflicto", "mensaje", "El horario se traslapa con un bloque existente ese dia"));
                }
            }
        }
        actual.setDiaSemana(dia);
        actual.setHoraInicio(inicio);
        actual.setHoraFin(fin);
        return ResponseEntity.ok(ihorario.save(actual));
    }

    // E3: DELETE /api/horarios/{id} -> 204 / 404
    @DeleteMapping("/api/horarios/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        if (ihorario.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Horario no encontrado"));
        }
        ihorario.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
