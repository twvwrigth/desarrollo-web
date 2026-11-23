package com.example.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class UsuarioControlador {

    @Autowired
    IUsuario iusuario;

    @Autowired
    PasswordEncoder encoder;

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

    @PostMapping("/api/usuarios")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario,
                                       @RequestHeader(value = "X-Usuario-Id", required = false) Integer adminId) {
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()
                || usuario.getEmail() == null || usuario.getEmail().isBlank()
                || usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "nombre, email y password son obligatorios"));
        }

        if (usuario.getPassword().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "password debe tener al menos 6 caracteres"));
        }

        String email = usuario.getEmail().trim().toLowerCase();
        usuario.setEmail(email);

        if (iusuario.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Conflicto", "mensaje", "El email ya esta registrado"));
        }

        if (usuario.getRol() == null) {
            usuario.setRol(Rol.CLIENTE);
        }

        if (usuario.getRol() != Rol.CLIENTE) {
            if (adminId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Sin permiso", "mensaje", "Solo un administrador puede crear BARBERO o ADMIN"));
            }
            Optional<Usuario> admin = iusuario.findById(adminId);
            if (admin.isEmpty() || !admin.get().isActivo() || admin.get().getRol() != Rol.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Sin permiso", "mensaje", "Solo un administrador puede crear BARBERO o ADMIN"));
            }
        }

        usuario.setPassword(encoder.encode(usuario.getPassword()));
        usuario.setActivo(true);
        Usuario guardado = iusuario.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @GetMapping("/api/usuarios/{id}")
    public ResponseEntity<?> obtener(@PathVariable Integer id,
                                     @RequestHeader(value = "X-Usuario-Id", required = false) Integer sesionId) {
        Optional<Usuario> usuario = iusuario.findById(id);
        if (usuario.isEmpty() || !usuario.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Usuario no encontrado"));
        }
        if (sesionId == null || sesion(sesionId).isEmpty()) return sinSesion();
        if (!esAdmin(sesionId) && !sesionId.equals(id)) {
            return sinPermiso("Solo puedes ver tu propio perfil");
        }
        return ResponseEntity.ok(usuario.get());
    }

    @GetMapping("/api/usuarios")
    public ResponseEntity<?> listar(@RequestParam(value = "rol", required = false) String rol,
                                    @RequestParam(value = "activos", required = false, defaultValue = "true") boolean soloActivos,
                                    @RequestHeader(value = "X-Usuario-Id", required = false) Integer sesionId) {
        Rol rolEnum = null;
        if (rol != null && !rol.isBlank()) {
            try {
                rolEnum = Rol.valueOf(rol.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Datos invalidos", "mensaje", "rol debe ser CLIENTE, BARBERO o ADMIN"));
            }
        }
        boolean listaBarberos = rolEnum == Rol.BARBERO && soloActivos;
        if (!listaBarberos) {
            if (sesionId == null || sesion(sesionId).isEmpty()) return sinSesion();
            if (!esAdmin(sesionId)) return sinPermiso("Solo un administrador puede listar usuarios");
        }
        if (soloActivos) {
            if (rolEnum == null) {
                return ResponseEntity.ok(iusuario.findByActivoTrue());
            }
            return ResponseEntity.ok(iusuario.findByRolAndActivoTrue(rolEnum));
        }

        List<Usuario> todos = new ArrayList<>();
        iusuario.findAll().forEach(todos::add);
        if (rolEnum != null) {
            Rol filtro = rolEnum;
            todos.removeIf(u -> u.getRol() != filtro);
        }
        return ResponseEntity.ok(todos);
    }

    @PutMapping("/api/usuarios/{id}")
    public ResponseEntity<?> editar(@PathVariable Integer id, @RequestBody Usuario cambios,
                                    @RequestHeader(value = "X-Usuario-Id", required = false) Integer sesionId) {
        Optional<Usuario> actualOpt = iusuario.findById(id);
        if (actualOpt.isEmpty() || !actualOpt.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Usuario no encontrado"));
        }
        if (sesionId == null || sesion(sesionId).isEmpty()) return sinSesion();
        if (!esAdmin(sesionId) && !sesionId.equals(id)) {
            return sinPermiso("Solo puedes editar tu propio perfil");
        }
        Usuario actual = actualOpt.get();

        if (cambios.getNombre() != null) {
            if (cambios.getNombre().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Datos invalidos", "mensaje", "nombre no puede ir vacio"));
            }
            actual.setNombre(cambios.getNombre().trim());
        }
        if (cambios.getEmail() != null) {
            if (cambios.getEmail().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Datos invalidos", "mensaje", "email no puede ir vacio"));
            }
            String email = cambios.getEmail().trim().toLowerCase();
            Optional<Usuario> conEseEmail = iusuario.findByEmail(email);
            if (conEseEmail.isPresent() && !conEseEmail.get().getId().equals(actual.getId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Conflicto", "mensaje", "El email ya esta registrado"));
            }
            actual.setEmail(email);
        }
        if (cambios.getTelefono() != null) {
            actual.setTelefono(cambios.getTelefono());
        }
        if (cambios.getPassword() != null) {
            if (cambios.getPassword().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Datos invalidos", "mensaje", "password no puede ir vacio"));
            }

            if (cambios.getPassword().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Datos invalidos", "mensaje", "password debe tener al menos 6 caracteres"));
            }
            actual.setPassword(encoder.encode(cambios.getPassword()));
        }
        return ResponseEntity.ok(iusuario.save(actual));
    }

    @PutMapping("/api/usuarios/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id, @RequestBody Map<String, Object> cuerpo,
                                          @RequestHeader(value = "X-Usuario-Id", required = false) Integer sesionId) {
        Optional<Usuario> actualOpt = iusuario.findById(id);
        if (actualOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Usuario no encontrado"));
        }
        Object activo = cuerpo == null ? null : cuerpo.get("activo");
        if (!(activo instanceof Boolean)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "activo debe ser true o false"));
        }
        if (sesionId == null || sesion(sesionId).isEmpty()) return sinSesion();
        if (!esAdmin(sesionId)) return sinPermiso("Solo un administrador puede activar o desactivar usuarios");
        Usuario actual = actualOpt.get();
        actual.setActivo((Boolean) activo);
        return ResponseEntity.ok(iusuario.save(actual));
    }

    @DeleteMapping("/api/usuarios/{id}")
    public ResponseEntity<?> baja(@PathVariable Integer id,
                                  @RequestHeader(value = "X-Usuario-Id", required = false) Integer sesionId) {
        Optional<Usuario> usuario = iusuario.findById(id);
        if (usuario.isEmpty() || !usuario.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Usuario no encontrado"));
        }
        if (sesionId == null || sesion(sesionId).isEmpty()) return sinSesion();
        if (!esAdmin(sesionId)) return sinPermiso("Solo un administrador puede dar de baja usuarios");
        usuario.get().setActivo(false);
        iusuario.save(usuario.get());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody Usuario datos) {
        if (datos.getEmail() == null || datos.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales incorrectas", "mensaje", "Credenciales incorrectas"));
        }
        Optional<Usuario> usuario = iusuario.findByEmail(datos.getEmail().trim().toLowerCase());
        if (usuario.isEmpty() || !usuario.get().isActivo()
                || !encoder.matches(datos.getPassword(), usuario.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales incorrectas", "mensaje", "Credenciales incorrectas"));
        }
        return ResponseEntity.ok(Map.of(
                "id", usuario.get().getId(),
                "nombre", usuario.get().getNombre(),
                "rol", usuario.get().getRol()));
    }
}
