package com.example.peloteros.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.peloteros.model.Cancha;

public interface CanchaRepository extends JpaRepository<Cancha, Long> {
}