package com.example.peloteros.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.peloteros.dao.ReservaRepository;
import com.example.peloteros.model.Cancha;
import com.example.peloteros.model.Reserva;
import com.example.peloteros.model.Usuario;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final CanchaService canchaService;

    @Autowired
    public ReservaService(ReservaRepository reservaRepository, CanchaService canchaService) {
        this.reservaRepository = reservaRepository;
        this.canchaService = canchaService;
    }

    public Reserva crearReserva(Reserva reserva) {
        return reservaRepository.save(reserva);
    }

    public List<Reserva> obtenerReservasPorUsuario(Usuario usuario) {
        return reservaRepository.findByUsuario(usuario);
    }

    public void cancelarReserva(Long reservaId) {
        reservaRepository.deleteById(reservaId);
    }

    public List<Reserva> obtenerReservasActivasPorUsuario(Usuario usuario) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from
        // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public List<Reserva> obtenerReservasPasadasPorUsuario(Usuario usuario) {
        return reservaRepository.findByUsuarioAndFechaHoraFinBeforeAndEstadoNot(
                usuario, LocalDateTime.now(), "CANCELADA");
    }

    public List<Reserva> obtenerReservasCanceladasPorUsuario(Usuario usuario) {
        return reservaRepository.findByUsuarioAndEstado(usuario, "CANCELADA");
    }

    public Reserva obtenerReservaPorId(Long id) {
        return reservaRepository.findById(id).orElse(null);
    }

    public boolean validarDisponibilidad(Cancha cancha, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        // Verificar si hay reservas que se superpongan con el horario solicitado
        List<Reserva> reservasExistentes = reservaRepository
                .findByCanchaAndFechaHoraInicioLessThanEqualAndFechaHoraFinGreaterThanEqualAndEstadoNot(
                        cancha, fechaHoraFin, fechaHoraInicio, "CANCELADA");
        return reservasExistentes.isEmpty();
    }

    public List<Reserva> obtenerReservasPorUsuarioYEstado(Usuario usuario, String estado) {
        return reservaRepository.findByUsuarioAndEstado(usuario, estado);
    }

    public void cancelarReserva(Long reservaId, String motivo) {
        Reserva reserva = obtenerReservaPorId(reservaId);
        if (reserva != null) {
            reserva.setEstado("CANCELADA");
            if (motivo != null && !motivo.trim().isEmpty()) {
                reserva.setComentarios(motivo);
            }
            reservaRepository.save(reserva);
        } else {
            throw new RuntimeException("Reserva no encontrada");
        }
    }

    public long contarReservasHoy() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicio = hoy.atStartOfDay();
        LocalDateTime fin = hoy.atTime(LocalTime.MAX);
        return reservaRepository.countByFechaHoraInicioBetweenAndEstadoNot(inicio, fin, "CANCELADA");
    }

    public List<Reserva> obtenerReservasPorCanchaYFecha(Cancha cancha, LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(23, 59, 59);
        return reservaRepository.findByCanchaAndFechaHoraInicioBetween(cancha, inicio, fin);
    }

    public List<Reserva> obtenerReservasPorFecha(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(23, 59, 59);
        return reservaRepository.findByFechaHoraInicioBetween(inicio, fin);
    }

    public void cambiarEstado(Long id, String nuevoEstado) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + id));
        reserva.setEstado(nuevoEstado);
        reservaRepository.save(reserva);
    }

    public List<Reserva> obtenerReservasConfirmadasPasadas(Usuario usuario) {
        return reservaRepository.findByUsuarioAndEstado(
                usuario,
                "CONFIRMADA");
    }

    public Map<String, Boolean> obtenerDisponibilidadHorarios(Long canchaId, LocalDate fecha) {
        Cancha cancha = canchaService.obtenerCanchaPorId(canchaId);
        if (cancha == null) {
            return new TreeMap<>();
        }

        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.atTime(23, 59, 59);
        List<Reserva> reservas = reservaRepository.findByCanchaAndFechaHoraInicioBetween(cancha, inicioDia, finDia);

        Map<Integer, Boolean> horasOcupadas = reservas.stream()
                .filter(r -> !r.getEstado().equals("CANCELADA"))
                .flatMap(r -> {
                    int horaInicio = r.getFechaHoraInicio().getHour();
                    int horaFin = r.getFechaHoraFin().getHour();
                    return IntStream.range(horaInicio, horaFin).boxed().map(Stream::of).reduce(Stream.empty(),
                            Stream::concat);
                })
                .collect(Collectors.toSet())
                .stream()
                .collect(Collectors.toMap(h -> h, h -> false));

        Map<String, Boolean> disponibilidad = new TreeMap<>();
        for (int i = 7; i <= 22; i++) {
            String hora = String.format("%02d:00", i);
            disponibilidad.put(hora, horasOcupadas.getOrDefault(i, true));
        }

        return disponibilidad;
    }

}
