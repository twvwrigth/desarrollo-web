package com.example.demo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface IUsuario extends CrudRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByActivoTrue();

    List<Usuario> findByRolAndActivoTrue(Rol rol);
}
