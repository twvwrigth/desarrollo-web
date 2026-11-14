package com.example.demo;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ICita extends CrudRepository<Cita, Integer> {

    List<Cita> findByClienteId(Integer clienteId);

    List<Cita> findByBarberoId(Integer barberoId);
}
