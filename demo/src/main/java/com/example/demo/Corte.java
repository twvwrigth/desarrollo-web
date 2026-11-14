package com.example.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cortes")
//Cada corte pertenece a un servicio: el servicio agrupa cortes
public class Corte {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String nombre;

    private String descripcion;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "overlay_url")
    private String overlayUrl;

    @ManyToOne
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;

    private boolean activo = true;

    public Corte() {
    }

    public Corte(String nombre, String descripcion, Servicio servicio) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.servicio = servicio;
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

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getOverlayUrl() {
        return overlayUrl;
    }

    public void setOverlayUrl(String overlayUrl) {
        this.overlayUrl = overlayUrl;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public void setServicio(Servicio servicio) {
        this.servicio = servicio;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
