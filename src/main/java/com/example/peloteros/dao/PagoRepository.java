package com.example.peloteros.dao;

import com.example.peloteros.model.Pago;
import com.example.peloteros.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByFechaPagoBetween(LocalDateTime inicio, LocalDateTime fin);
    
    List<Pago> findByUsuarioAndFechaPagoBetween(Usuario usuario, LocalDateTime inicio, LocalDateTime fin);
    
    @Query("SELECT SUM(p.monto) FROM Pago p WHERE p.fechaPago BETWEEN :inicio AND :fin")
    Double sumMontoByFechaPagoBetween(@Param("inicio") LocalDateTime inicio, 
                                     @Param("fin") LocalDateTime fin);
}