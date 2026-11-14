package com.example.demo;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ICorte extends CrudRepository<Corte, Integer> {

    List<Corte> findByActivoTrue();

    List<Corte> findByServicioId(Integer servicioId);

    List<Corte> findByServicioIdAndActivoTrue(Integer servicioId);
}
