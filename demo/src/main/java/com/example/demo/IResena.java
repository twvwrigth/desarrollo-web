package com.example.demo;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

// Al estilo de IUsuario en clase: interfaz de repositorio con CrudRepository
public interface IResena extends CrudRepository<Resena, Integer> {

    boolean existsByCitaId(Integer citaId);

    List<Resena> findByCitaIdIn(List<Integer> citaIds);
}
