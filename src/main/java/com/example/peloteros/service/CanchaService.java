package com.example.peloteros.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.peloteros.dao.CanchaRepository;
import com.example.peloteros.model.Cancha;
import com.example.peloteros.model.Reserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CanchaService {
    private final CanchaRepository canchaRepository;

    @Autowired
    public CanchaService(CanchaRepository canchaRepository) {
        this.canchaRepository = canchaRepository;
    }

    public List<Cancha> obtenerTodasCanchas() {
        return canchaRepository.findAll();
    }

    public Cancha obtenerCanchaPorId(Long id) {
        return canchaRepository.findById(id).orElse(null);
    }
    
    public Cancha guardarCancha(Cancha cancha) {
        return canchaRepository.save(cancha);
    }
     public long contarCanchas() {
        return canchaRepository.count();
    }
   
        public List<Cancha> listarTodas() {
        return canchaRepository.findAll();
    }
    public void actualizarHorarios(Long id, String horarioApertura, String horarioCierre) {
        Cancha cancha = canchaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cancha no encontrada con ID: " + id));
        cancha.setHorarioApertura(horarioApertura);
        cancha.setHorarioCierre(horarioCierre);
        canchaRepository.save(cancha);
    }



   
}