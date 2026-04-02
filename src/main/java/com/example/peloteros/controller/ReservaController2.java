package com.example.peloteros.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.peloteros.model.Cancha;
import com.example.peloteros.service.CanchaService;
import com.example.peloteros.service.ReservaService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.time.LocalTime;
import java.time.LocalDate;

@Controller
@RequestMapping("/reserva2")
public class ReservaController2 {

    private final CanchaService canchaService;
    private final ReservaService reservaService;

    @Autowired
    public ReservaController2(CanchaService canchaService,
            ReservaService reservaService) {
        this.canchaService = canchaService;
        this.reservaService = reservaService;
    }

    @GetMapping("/disponibilidad")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> verificarDisponibilidad(
            @RequestParam Long canchaId,
            @RequestParam String fecha,
            @RequestParam String horaInicio,
            @RequestParam String horaFin) {

        Cancha cancha = canchaService.obtenerCanchaPorId(canchaId);
        if (cancha == null) {
            return ResponseEntity.badRequest().build();
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate fechaSeleccionada = LocalDate.parse(fecha);
        LocalTime inicio = LocalTime.parse(horaInicio, timeFormatter);
        LocalTime fin = LocalTime.parse(horaFin, timeFormatter);

        LocalDateTime fechaHoraInicio = fechaSeleccionada.atTime(inicio);
        LocalDateTime fechaHoraFin = fechaSeleccionada.atTime(fin);

        boolean disponible = reservaService.validarDisponibilidad(cancha, fechaHoraInicio, fechaHoraFin);

        return ResponseEntity.ok(Map.of("disponible", disponible));
    }
}
