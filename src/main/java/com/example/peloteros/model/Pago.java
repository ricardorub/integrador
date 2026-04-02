package com.example.peloteros.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private Reserva reserva;
    
    @ManyToOne
    private Usuario usuario;
    
    private double monto;
    private LocalDateTime fechaPago;
    private String metodoPago; // EFECTIVO, TARJETA, TRANSFERENCIA
    private String estado; // COMPLETADO, PENDIENTE, RECHAZADO
    private String referencia;
}