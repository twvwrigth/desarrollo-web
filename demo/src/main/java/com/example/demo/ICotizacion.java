package com.example.demo;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ICotizacion extends CrudRepository<Cotizacion, Integer> {

    List<Cotizacion> findByClienteId(Integer clienteId);
}
