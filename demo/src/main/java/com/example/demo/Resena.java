package com.example.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;

import java.time.LocalDateTime;

// Entidad del diagrama ER (tabla resenas en esquema.sql), al estilo de Usuario/HorarioBarbero:
// un solo paquete com.example.demo, sin DTOs ni servicios.
// Campos segun esquema.sql: id, cita_id (UNIQUE), calificacion 1-5, comentario, fecha_creacion.
// Se guarda citaId como Integer (no @ManyToOne) para que el JSON de entrada sea
// exactamente el de endpoints.md: { "citaId": 15, "calificacion": 5, "comentario": "..." }.
// La cita se valida en el controlador con la entidad Cita provisional (ver Cita.java).
// Sin @Table, igual que Usuario/HorarioBarbero: Hibernate crea la tabla "resena" por defecto.
@Entity
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "cita_id", unique = true, nullable = false)
    private Integer citaId;

    @Column(name = "calificacion", nullable = false)
    private Integer calificacion;

    private String comentario;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void alCrear() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    public Resena() {
    }

    public Resena(Integer citaId, Integer calificacion, String comentario) {
        this.citaId = citaId;
        this.calificacion = calificacion;
        this.comentario = comentario;
    }

    public Integer getId() {
        return id;
    }

    public Integer getCitaId() {
        return citaId;
    }

    public void setCitaId(Integer citaId) {
        this.citaId = citaId;
    }

    public Integer getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Integer calificacion) {
        this.calificacion = calificacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
