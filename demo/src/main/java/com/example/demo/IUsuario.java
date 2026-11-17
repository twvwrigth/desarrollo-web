package com.example.demo;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface IUsuario extends CrudRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    // E3: lista filtrable por rol (solo usuarios activos, como en GET /{id})
    List<Usuario> findByActivoTrue();

    List<Usuario> findByRolAndActivoTrue(Rol rol);
}
