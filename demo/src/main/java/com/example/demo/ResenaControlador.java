package com.example.demo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@CrossOrigin(origins = "*")
@RestController
public class ResenaControlador {

    @Autowired
    IResena iresena;

    @Autowired
    ICita icita;

    @Autowired
    IUsuario iusuario;

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

    @GetMapping("/api/resenas/resumen")
    public ResponseEntity<?> resumen(@RequestParam(value = "barberoId", required = false) Integer barberoId) {
        if (barberoId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "barberoId es obligatorio"));
        }

        Optional<Usuario> barbero = iusuario.findById(barberoId);
        if (barbero.isEmpty() || barbero.get().getRol() != Rol.BARBERO) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Barbero no encontrado"));
        }

        // FIX 23/11: las citas del barbero se buscan en memoria sobre todas
        // las citas, para que el resumen use exactamente las mismas citas que
        // el listado de resenas y no dependa de la consulta derivada.
        Set<Integer> citaIds = new HashSet<>();
        for (Cita cita : icita.findAll()) {
            if (cita.getBarbero() != null && barberoId.equals(cita.getBarbero().getId())) {
                citaIds.add(cita.getId());
            }
        }

        if (citaIds.isEmpty()) {
            return ResponseEntity.ok(Map.of("barberoId", barberoId, "promedio", 0.0, "total", 0));
        }

        List<Resena> resenas = iresena.findByCitaIdIn(new ArrayList<>(citaIds));
        double promedio = resenas.stream()
                .mapToInt(Resena::getCalificacion)
                .average()
                .orElse(0.0);
        promedio = Math.round(promedio * 10.0) / 10.0;

        return ResponseEntity.ok(Map.of("barberoId", barberoId, "promedio", promedio, "total", resenas.size()));
    }

    @PutMapping("/api/resenas/{id}")
    public ResponseEntity<?> editar(@PathVariable Integer id,
                                    @RequestBody Resena cambios,
                                    @RequestHeader(value = "X-Usuario-Id", required = false) Integer usuarioId) {
        Optional<Resena> existente = iresena.findById(id);
        if (existente.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Resena no encontrada"));
        }

        if (cambios.getCalificacion() != null
                && (cambios.getCalificacion() < 1 || cambios.getCalificacion() > 5)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "La calificacion debe ser de 1 a 5"));
        }


        if (usuarioId != null) {
            Integer clienteId = clienteDeLaResena(existente.get());
            if (clienteId != null && !usuarioId.equals(clienteId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Sin permiso", "mensaje", "Solo el cliente de la cita puede editar su resena"));
            }
        }

        Resena resena = existente.get();
        if (cambios.getCalificacion() != null) {
            resena.setCalificacion(cambios.getCalificacion());
        }
        if (cambios.getComentario() != null) {
            resena.setComentario(cambios.getComentario());
        }

        return ResponseEntity.ok(iresena.save(resena));
    }


    @DeleteMapping("/api/resenas/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id,
                                      @RequestHeader(value = "X-Usuario-Id", required = false) Integer usuarioId) {
        Optional<Resena> existente = iresena.findById(id);
        if (existente.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Resena no encontrada"));
        }

        if (usuarioId != null) {
            Integer clienteId = clienteDeLaResena(existente.get());
            if (clienteId != null && !usuarioId.equals(clienteId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Sin permiso", "mensaje", "Solo el cliente de la cita puede eliminar su resena"));
            }
        }

        iresena.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    private Integer clienteDeLaResena(Resena resena) {
        Optional<Cita> cita = icita.findById(resena.getCitaId());
        if (cita.isEmpty() || cita.get().getCliente() == null) {
            return null;
        }
        return cita.get().getCliente().getId();
    }
}
