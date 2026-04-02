package com.example.peloteros.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import lombok.Data;

@Entity
@Data
public class Cancha {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String tipo;
    private String ubicacion;
    private String direccion;
    private double precioPorHora;
    private String descripcion;
    private String horarioApertura;
    private String horarioCierre;
    private String fotoUrl;
    private String mapaUrl;

    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] foto;

    public String getFotoBase64() {
        if (foto == null || foto.length == 0) return null;
        return java.util.Base64.getEncoder().encodeToString(foto);
    }
}