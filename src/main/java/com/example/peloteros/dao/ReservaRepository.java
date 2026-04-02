/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.peloteros.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.peloteros.model.Cancha;
import com.example.peloteros.model.Reserva;
import com.example.peloteros.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
  List<Reserva> findByUsuario(Usuario usuario);

  @Query("SELECT r FROM Reserva r WHERE r.usuario = :usuario AND r.estado = :estado")
  List<Reserva> findByUsuarioAndEstado(
      @Param("usuario") Usuario usuario,
      @Param("estado") String estado);

  @Query("SELECT r FROM Reserva r WHERE r.usuario = :usuario AND r.fechaHoraFin < :fechaHora AND r.estado != :estado")
  List<Reserva> findByUsuarioAndFechaHoraFinBeforeAndEstadoNot(
      @Param("usuario") Usuario usuario,
      @Param("fechaHora") LocalDateTime fechaHora,
      @Param("estado") String estado);

  @Query("SELECT r FROM Reserva r WHERE r.cancha = :cancha AND r.fechaHoraInicio <= :fechaHoraFin AND r.fechaHoraFin >= :fechaHoraInicio AND r.estado != :estado")
  List<Reserva> findByCanchaAndFechaHoraInicioLessThanEqualAndFechaHoraFinGreaterThanEqualAndEstadoNot(
      @Param("cancha") Cancha cancha,
      @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
      @Param("fechaHoraFin") LocalDateTime fechaHoraFin,
      @Param("estado") String estado);

  long countByFechaHoraInicioBetweenAndEstadoNot(LocalDateTime inicio, LocalDateTime fin, String estado);

  List<Reserva> findByCanchaAndFechaHoraInicioBetween(Cancha cancha, LocalDateTime inicio, LocalDateTime fin);

  List<Reserva> findByFechaHoraInicioBetween(LocalDateTime inicio, LocalDateTime fin);

}
