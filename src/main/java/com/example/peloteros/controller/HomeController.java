package com.example.peloteros.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.peloteros.model.Cancha;
import com.example.peloteros.model.Usuario;
import com.example.peloteros.service.CanchaService;

@Controller
@RequestMapping("/home")
public class HomeController {

    private final CanchaService canchaService;

    @Autowired
    public HomeController(CanchaService canchaService) {
        this.canchaService = canchaService;
    }

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        // Obtener todas las canchas disponibles
        List<Cancha> canchas = canchaService.obtenerTodasCanchas();
        model.addAttribute("canchas", canchas);

        // Verificar si hay un usuario en sesión
        Object usuario = session.getAttribute("usuario");
        model.addAttribute("usuario", usuario);

        return "index";
    }

    @GetMapping("/login")
    public String login(Model model, HttpSession session) {
        // Redirect to the main login page handled by UsuarioController & Spring Security
        return "redirect:/usuario/login";
    }

    @GetMapping("/registro")
    public String registro(Model model, HttpSession session) {
        // Redirect to the main registration page handled by UsuarioController & Spring Security
        return "redirect:/usuario/home/registro";
    }

    

    @GetMapping("/eventos")
    public String eventos() {
        return "eventos"; // Página de eventos (a implementar)
    }

    @GetMapping("/contacto")
    public String contacto() {
        return "contacto"; // Página de contacto (a implementar)
    }
}

