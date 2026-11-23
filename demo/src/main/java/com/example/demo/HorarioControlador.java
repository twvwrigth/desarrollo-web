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

@CrossOrigin(origins = "*")
@RestController
public class HorarioControlador {

    @Autowired
    IHorarioBarbero ihorario;

    @Autowired
    IUsuario iusuario;

    private Optional<Usuario> sesion(Integer sesionId) {
        if (sesionId == null) return Optional.empty();
        Optional<Usuario> u = iusuario.findById(sesionId);
        if (u.isEmpty() || !u.get().isActivo()) return Optional.empty();
        return u;
    }

    private boolean esAdmin(Integer sesionId) {
        Optional<Usuario> u = sesion(sesionId);
        return u.isPresent() && u.get().getRol() == Rol.ADMIN;
    }

    private ResponseEntity<?> sinSesion() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Sin sesion", "mensaje", "Inicia sesión para continuar"));
    }

    private ResponseEntity<?> sinPermiso(String mensaje) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Sin permiso", "mensaje", mensaje));
    }

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

    @PostMapping("/api/barberos/{barberoId}/horarios")
    public ResponseEntity<?> agregar(@PathVariable Integer barberoId, @RequestBody HorarioBarbero horario,
                                     @RequestHeader(value = "X-Usuario-Id", required = false) Integer sesionId) {
        Optional<Usuario> barbero = iusuario.findById(barberoId);
        if (barbero.isEmpty() || !barbero.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Barbero no encontrado"));
        }
        if (barbero.get().getRol() != Rol.BARBERO) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "El usuario no es un barbero"));
        }
        if (sesionId == null || sesion(sesionId).isEmpty()) return sinSesion();
        if (!esAdmin(sesionId) && !sesionId.equals(barberoId)) {
            return sinPermiso("Solo el barbero dueño o un administrador puede crear horarios");
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

    @PutMapping("/api/horarios/{id}")
    public ResponseEntity<?> editar(@PathVariable Integer id, @RequestBody HorarioBarbero cambios,
                                    @RequestHeader(value = "X-Usuario-Id", required = false) Integer sesionId) {
        Optional<HorarioBarbero> actualOpt = ihorario.findById(id);
        if (actualOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Horario no encontrado"));
        }
        HorarioBarbero actual = actualOpt.get();
        Integer duenoId = actual.getBarbero() != null ? actual.getBarbero().getId() : null;
        if (sesionId == null || sesion(sesionId).isEmpty()) return sinSesion();
        if (!esAdmin(sesionId) && (duenoId == null || !sesionId.equals(duenoId))) {
            return sinPermiso("Solo el barbero dueño o un administrador puede editar horarios");
        }
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

    @DeleteMapping("/api/horarios/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id,
                                      @RequestHeader(value = "X-Usuario-Id", required = false) Integer sesionId) {
        Optional<HorarioBarbero> actualOpt = ihorario.findById(id);
        if (actualOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Horario no encontrado"));
        }
        Integer duenoId = actualOpt.get().getBarbero() != null ? actualOpt.get().getBarbero().getId() : null;
        if (sesionId == null || sesion(sesionId).isEmpty()) return sinSesion();
        if (!esAdmin(sesionId) && (duenoId == null || !sesionId.equals(duenoId))) {
            return sinPermiso("Solo el barbero dueño o un administrador puede eliminar horarios");
        }
        ihorario.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
