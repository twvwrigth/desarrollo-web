package com.example.demo;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface IHorarioBarbero extends CrudRepository<HorarioBarbero, Integer> {

    List<HorarioBarbero> findByBarberoId(Integer barberoId);

    List<HorarioBarbero> findByBarberoIdAndDiaSemana(Integer barberoId, int diaSemana);
}
