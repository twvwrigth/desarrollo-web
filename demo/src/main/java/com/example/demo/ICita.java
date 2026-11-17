package com.example.demo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface ICita extends CrudRepository<Cita, Integer> {

    List<Cita> findByClienteId(Integer clienteId);

    List<Cita> findByBarberoId(Integer barberoId);

    List<Cita> findByBarberoIdAndFechaHoraInicioBetween(Integer barberoId, LocalDateTime inicio, LocalDateTime fin);
}
