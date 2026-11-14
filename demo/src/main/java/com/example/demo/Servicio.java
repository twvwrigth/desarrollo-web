package com.example.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

// Es el catálogo de lo que la barbería ofrece: cada servicio agrupa uno o varios cortes
@Entity
@Table(name = "servicios")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    // Nombre único en la base: el controlador envía 409 cuando alguien intenta crear un servicio repetido.
    @Column(unique = true)
    private String nombre;

    private String descripcion;

    @Column(name = "duracion_min")
    private Integer duracionMin;

    private BigDecimal precio;

    private boolean activo = true;

    public Servicio() {
    }

    public Servicio(String nombre, String descripcion, Integer duracionMin, BigDecimal precio) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.duracionMin = duracionMin;
        this.precio = precio;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getDuracionMin() {
        return duracionMin;
    }

    public void setDuracionMin(Integer duracionMin) {
        this.duracionMin = duracionMin;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
