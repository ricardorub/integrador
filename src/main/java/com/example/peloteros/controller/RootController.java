package com.example.peloteros.controller;

import com.example.peloteros.model.Usuario;
import com.example.peloteros.service.CanchaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    private final CanchaService canchaService;

    public RootController(CanchaService canchaService) {
        this.canchaService = canchaService;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        // Get user from session
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        model.addAttribute("usuario", usuario);
        
        // Add any other data needed for the home page
        model.addAttribute("canchas", canchaService.obtenerTodasCanchas());
        
        return "index";
    }

    @GetMapping("/reservar")
    public String redirectToCreateReservation() {
        return "redirect:/reserva/reservar";
    }
}





