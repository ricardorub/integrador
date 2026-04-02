package com.example.peloteros.service;

import com.example.peloteros.model.*;
import com.example.peloteros.dao.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final ReservaService reservaService;
    private final UsuarioService usuarioService;

    @Autowired
    public PagoService(PagoRepository pagoRepository,
                      ReservaService reservaService,
                      UsuarioService usuarioService) {
        this.pagoRepository = pagoRepository;
        this.reservaService = reservaService;
        this.usuarioService = usuarioService;
    }

    public void registrarPago(Long reservaId, double monto, String metodoPago) {
        Reserva reserva = reservaService.obtenerReservaPorId(reservaId);
        if (reserva == null) {
            throw new IllegalArgumentException("Reserva no encontrada");
        }
        
        Pago pago = new Pago();
        pago.setReserva(reserva);
        pago.setUsuario(reserva.getUsuario());
        pago.setMonto(monto);
        pago.setFechaPago(LocalDateTime.now());
        pago.setMetodoPago(metodoPago);
        pago.setEstado("COMPLETADO");
        
        pagoRepository.save(pago);
        
        // Actualizar estado de la reserva si es necesario
        if ("PENDIENTE".equals(reserva.getEstado())) {
            reserva.setEstado("CONFIRMADA");
            reservaService.crearReserva(reserva);
        }
    }

    public List<Pago> obtenerPagosPorFecha(LocalDate inicio, LocalDate fin) {
        return pagoRepository.findByFechaPagoBetween(
            inicio.atStartOfDay(),
            fin.plusDays(1).atStartOfDay()
        );
    }

    public List<Pago> obtenerPagosPorUsuarioYFecha(Usuario usuario, LocalDate inicio, LocalDate fin) {
        return pagoRepository.findByUsuarioAndFechaPagoBetween(
            usuario,
            inicio.atStartOfDay(),
            fin.plusDays(1).atStartOfDay()
        );
    }
public double calcularTotalIngresos(LocalDate inicio, LocalDate fin) {
    LocalDateTime inicioDateTime = inicio.atStartOfDay(); // 00:00:00
    LocalDateTime finDateTime = fin.atTime(LocalTime.MAX); // 23:59:59.999999999

    Double total = pagoRepository.sumMontoByFechaPagoBetween(inicioDateTime, finDateTime);
    return total != null ? total : 0.0;
}

    public double calcularIngresosHoy() {
        LocalDate hoy = LocalDate.now();
        return pagoRepository.sumMontoByFechaPagoBetween(
            hoy.atStartOfDay(),
            hoy.plusDays(1).atStartOfDay()
        );
    }
}