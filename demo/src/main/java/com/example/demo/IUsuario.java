package com.example.demo;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

// Al estilo de ISaludador en clase: interfaz de repositorio con CrudRepository
public interface IUsuario extends CrudRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
