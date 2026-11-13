package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Configuracion simple, todo en el mismo paquete como en clase.
// Solo se usa el encoder BCrypt, NO se activa Spring Security completo
// (decision documentada en endpoints.md: frontend guarda el usuario en
// sessionStorage y manda el header X-Usuario-Id).
@Configuration
public class Configuracion {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Admin inicial solo para poder probar la creacion de barberos en el E2.
    // Credenciales de DEMO: admin@barberia.com / admin123 (cambiarla despues).
    @Bean
    public CommandLineRunner initAdmin(IUsuario iusuario, PasswordEncoder encoder) {
        return args -> {
            if (iusuario.findByEmail("admin@barberia.com").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setNombre("Administrador");
                admin.setEmail("admin@barberia.com");
                admin.setPassword(encoder.encode("admin123"));
                admin.setTelefono("0000000000");
                admin.setRol(Rol.ADMIN);
                admin.setActivo(true);
                iusuario.save(admin);
                System.out.println("Admin inicial creado: admin@barberia.com / admin123 (solo demo)");
            }
        };
    }
}
