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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Las relaciones llegan como objetos con solo el id
// El controlador busca cada entidad, valida y la reemplaza por la entidad completa antes de guardar.
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

    @Autowired
    IHorarioBarbero ihorarioBarbero;
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

    @GetMapping("/api/citas/{id}")
    public ResponseEntity<?> obtener(@PathVariable Integer id) {
        Optional<Cita> cita = icita.findById(id);
        if (cita.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Cita no encontrada"));
        }
        return ResponseEntity.ok(cita.get());
    }

    @GetMapping("/api/clientes/{id}/citas")
    public ResponseEntity<?> listarPorCliente(@PathVariable Integer id) {
        Optional<Usuario> cliente = iusuario.findById(id);
        if (cliente.isEmpty() || !cliente.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Cliente no encontrado"));
        }
        return ResponseEntity.ok(icita.findByClienteId(id));
    }
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

    //El estado es PENDIENTE, CONFIRMADA, COMPLETADA o CANCELADA.
    @GetMapping("/api/citas")
    public ResponseEntity<?> listar(@RequestParam(required = false) Integer clienteId,
                                    @RequestParam(required = false) Integer barberoId,
                                    @RequestParam(required = false) String fecha,
                                    @RequestParam(required = false) String estado) {
        List<Cita> citas;
        if (clienteId != null) {
            citas = icita.findByClienteId(clienteId);
        } else if (barberoId != null) {
            citas = icita.findByBarberoId(barberoId);
        } else {
            citas = (List<Cita>) icita.findAll();
        }
        if (fecha != null) {
            LocalDate dia;
            try {
                dia = LocalDate.parse(fecha);
            } catch (DateTimeParseException e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Datos invalidos", "mensaje", "fecha debe ser yyyy-MM-dd"));
            }
            citas = citas.stream()
                    .filter(c -> c.getFechaHoraInicio().toLocalDate().equals(dia))
                    .toList();
        }
        if (estado != null) {
            EstadoCita estadoFiltro;
            try {
                estadoFiltro = EstadoCita.valueOf(estado.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Datos invalidos",
                                "mensaje", "estado no válido: PENDIENTE, CONFIRMADA, COMPLETADA o CANCELADA"));
            }
            citas = citas.stream()
                    .filter(c -> c.getEstado() == estadoFiltro)
                    .toList();
        }
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/api/citas/disponibilidad")
    public ResponseEntity<?> disponibilidad(@RequestParam(required = false) Integer barberoId,
                                           @RequestParam(required = false) Integer servicioId,
                                           @RequestParam(required = false) String fecha) {
        if (barberoId == null || servicioId == null || fecha == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos",
                            "mensaje", "barberoId, servicioId y fecha son obligatorios"));
        }
        LocalDate dia;
        try {
            dia = LocalDate.parse(fecha);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "fecha debe ser yyyy-MM-dd"));
        }
        Optional<Usuario> barbero = iusuario.findById(barberoId);
        if (barbero.isEmpty() || !barbero.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Barbero no encontrado"));
        }
        if (barbero.get().getRol() != Rol.BARBERO) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "El usuario no es un barbero"));
        }
        Optional<Servicio> servicio = iservicio.findById(servicioId);
        if (servicio.isEmpty() || !servicio.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Servicio no encontrado o inactivo"));
        }
        int duracion = servicio.get().getDuracionMin();
        int diaSemana = dia.getDayOfWeek().getValue();
        List<HorarioBarbero> horarios =
                ihorarioBarbero.findByBarberoIdAndDiaSemana(barberoId, diaSemana);
        // Citas de ese barbero ese día
        LocalDateTime inicioDia = dia.atStartOfDay();
        LocalDateTime finDia = dia.plusDays(1).atStartOfDay();
        List<Cita> citasDia = icita
                .findByBarberoIdAndFechaHoraInicioBetween(barberoId, inicioDia, finDia)
                .stream()
                .filter(c -> c.getEstado() != EstadoCita.CANCELADA)
                .toList();
        List<Map<String, String>> huecos = new ArrayList<>();
        for (HorarioBarbero horario : horarios) {
            LocalTime hora = horario.getHoraInicio();
            while (!hora.plusMinutes(duracion).isAfter(horario.getHoraFin())) {
                LocalTime finHueco = hora.plusMinutes(duracion);
                LocalDateTime inicioHueco = dia.atTime(hora);
                LocalDateTime finHuecoFecha = dia.atTime(finHueco);
                boolean libre = citasDia.stream().noneMatch(c ->
                        inicioHueco.isBefore(c.getFechaHoraFin())
                                && finHuecoFecha.isAfter(c.getFechaHoraInicio()));
                if (libre) {
                    huecos.add(Map.of("horaInicio", hora.toString(), "horaFin", finHueco.toString()));
                }
                hora = finHueco;
            }
        }
        return ResponseEntity.ok(Map.of(
                "barberoId", barberoId,
                "servicioId", servicioId,
                "fecha", fecha,
                "duracionMin", duracion,
                "horarios", huecos));
    }
    //Solo se puede reprogramar una cita PENDIENTE o CONFIRMADA
    @PutMapping("/api/citas/{id}")
    public ResponseEntity<?> reprogramar(@PathVariable Integer id, @RequestBody Cita cambios) {
        Optional<Cita> actual = icita.findById(id);
        if (actual.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Cita no encontrada"));
        }
        Cita cita = actual.get();
        if (cita.getEstado() == EstadoCita.COMPLETADA || cita.getEstado() == EstadoCita.CANCELADA) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Conflicto",
                            "mensaje", "No se puede reprogramar una cita " + cita.getEstado()));
        }
        Usuario barbero = cita.getBarbero();
        if (cambios.getBarbero() != null && cambios.getBarbero().getId() != null) {
            Optional<Usuario> nuevo = iusuario.findById(cambios.getBarbero().getId());
            if (nuevo.isEmpty() || !nuevo.get().isActivo()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No encontrado", "mensaje", "Barbero no encontrado"));
            }
            if (nuevo.get().getRol() != Rol.BARBERO) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Datos invalidos", "mensaje", "El usuario no es un barbero"));
            }
            barbero = nuevo.get();
        }
        LocalDateTime inicio = cita.getFechaHoraInicio();
        LocalDateTime fin = cita.getFechaHoraFin();
        if (cambios.getFechaHoraInicio() != null || cambios.getFechaHoraFin() != null) {
            if (cambios.getFechaHoraInicio() == null || cambios.getFechaHoraFin() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Datos invalidos",
                                "mensaje", "Para reprogramar la fecha se mandan fechaHoraInicio y fechaHoraFin"));
            }
            inicio = cambios.getFechaHoraInicio();
            fin = cambios.getFechaHoraFin();
            if (!fin.isAfter(inicio)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Datos invalidos",
                                "mensaje", "La fechaHoraFin debe ser mayor que la fechaHoraInicio"));
            }
        }
        // Traslape con otra cita del (nuevo) barbero
        for (Cita otra : icita.findByBarberoId(barbero.getId())) {
            if (otra.getId().equals(cita.getId()) || otra.getEstado() == EstadoCita.CANCELADA) {
                continue;
            }
            if (inicio.isBefore(otra.getFechaHoraFin()) && fin.isAfter(otra.getFechaHoraInicio())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Conflicto",
                                "mensaje", "El barbero ya tiene una cita en ese horario"));
            }
        }
        cita.setBarbero(barbero);
        cita.setFechaHoraInicio(inicio);
        cita.setFechaHoraFin(fin);
        if (cambios.getNotas() != null) {
            cita.setNotas(cambios.getNotas());
        }
        return ResponseEntity.ok(icita.save(cita));
    }

    @PutMapping("/api/citas/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id,
                                          @RequestBody(required = false) Map<String, String> cuerpo,
                                          @RequestHeader(value = "X-Usuario-Id", required = false) Integer solicitanteId) {
        Optional<Cita> actual = icita.findById(id);
        if (actual.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Cita no encontrada"));
        }
        Cita cita = actual.get();
        if (solicitanteId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Sin permiso", "mensaje", "Se requiere el header X-Usuario-Id"));
        }
        Optional<Usuario> solicitante = iusuario.findById(solicitanteId);
        boolean esAdmin = solicitante.isPresent() && solicitante.get().isActivo()
                && solicitante.get().getRol() == Rol.ADMIN;
        boolean esCliente = cita.getCliente().getId().equals(solicitanteId);
        boolean esBarbero = cita.getBarbero().getId().equals(solicitanteId);
        if (!esAdmin && !esCliente && !esBarbero) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Sin permiso",
                            "mensaje", "Solo el cliente, el barbero o un administrador pueden cambiar el estado"));
        }
        String nuevoTexto = cuerpo == null ? null : cuerpo.get("estado");
        if (nuevoTexto == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos",
                            "mensaje", "estado es obligatorio: PENDIENTE, CONFIRMADA, COMPLETADA o CANCELADA"));
        }
        EstadoCita nuevo;
        try {
            nuevo = EstadoCita.valueOf(nuevoTexto.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos",
                            "mensaje", "estado no válido: PENDIENTE, CONFIRMADA, COMPLETADA o CANCELADA"));
        }
        if (!transicionPermitida(cita.getEstado(), nuevo)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Conflicto",
                            "mensaje", "No se puede pasar de " + cita.getEstado() + " a " + nuevo));
        }
        cita.setEstado(nuevo);
        return ResponseEntity.ok(icita.save(cita));
    }
    private boolean transicionPermitida(EstadoCita actual, EstadoCita nuevo) {
        return switch (actual) {
            case PENDIENTE -> nuevo == EstadoCita.CONFIRMADA || nuevo == EstadoCita.CANCELADA;
            case CONFIRMADA -> nuevo == EstadoCita.COMPLETADA || nuevo == EstadoCita.CANCELADA;
            case COMPLETADA, CANCELADA -> false;
        };
    }

    //Pone el estado CANCELADA. Una cita COMPLETADA o CANCELADA no se puede cancelar
    @DeleteMapping("/api/citas/{id}")
    public ResponseEntity<?> cancelar(@PathVariable Integer id) {
        Optional<Cita> actual = icita.findById(id);
        if (actual.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Cita no encontrada"));
        }
        Cita cita = actual.get();
        if (cita.getEstado() == EstadoCita.COMPLETADA || cita.getEstado() == EstadoCita.CANCELADA) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Conflicto",
                            "mensaje", "No se puede cancelar una cita " + cita.getEstado()));
        }
        cita.setEstado(EstadoCita.CANCELADA);
        icita.save(cita);
        return ResponseEntity.noContent().build();
    }
}
