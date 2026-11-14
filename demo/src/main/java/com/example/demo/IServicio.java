package com.example.demo;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface IServicio extends CrudRepository<Servicio, Integer> {

    Optional<Servicio> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    List<Servicio> findByActivoTrue();
}
