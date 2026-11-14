package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

// Al estilo de SaludarControlador en clase: @RestController, @CrossOrigin y @Autowired
@CrossOrigin(origins = "*")
@RestController
public class UsuarioControlador {

    @Autowired
    IUsuario iusuario;

    @Autowired
    PasswordEncoder encoder;

    // E2: POST /api/usuarios -> 201 / 400 / 403 / 409
    // Si el rol es BARBERO o ADMIN, el header X-Usuario-Id debe ser de un ADMIN.
    @PostMapping("/api/usuarios")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario,
                                       @RequestHeader(value = "X-Usuario-Id", required = false) Integer adminId) {
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()
                || usuario.getEmail() == null || usuario.getEmail().isBlank()
                || usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Datos invalidos", "mensaje", "nombre, email y password son obligatorios"));
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

    // E2: GET /api/usuarios/{id} -> 200 / 404
    @GetMapping("/api/usuarios/{id}")
    public ResponseEntity<?> obtener(@PathVariable Integer id) {
        Optional<Usuario> usuario = iusuario.findById(id);
        if (usuario.isEmpty() || !usuario.get().isActivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No encontrado", "mensaje", "Usuario no encontrado"));
        }
        return ResponseEntity.ok(usuario.get());
    }

    // E2: POST /api/auth/login -> 200 / 401
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
