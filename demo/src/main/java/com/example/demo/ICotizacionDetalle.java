package com.example.demo;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

// Repositorio E2 de Erick.
public interface ICotizacionDetalle extends CrudRepository<CotizacionDetalle, Integer> {

    List<CotizacionDetalle> findByCotizacionId(Integer cotizacionId);
}
