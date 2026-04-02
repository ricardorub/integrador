package com.example.peloteros.controller;

import jakarta.servlet.http.HttpSession; // Will be removed or replaced for profile management later
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.peloteros.model.Usuario;
import com.example.peloteros.service.UsuarioService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String mostrarLogin(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) { // Removed HttpSession

        if (error != null) {
            model.addAttribute("errorMessage", "Credenciales incorrectas. Por favor intenta nuevamente.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Has cerrado sesión exitosamente.");
        }

        // Check if user is already authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/"; // Or some default logged-in page
        }
        return "login"; // Return the name of the login view (e.g., "login.html")
    }

    // POST /usuario/login is removed, Spring Security handles it.

    @GetMapping("/home/registro")
    public String mostrarRegistro(Model model) { // Removed HttpSession
        // Check if user is already authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/"; // Or some default logged-in page
        }
        model.addAttribute("usuario", new Usuario()); // For form binding if needed
        return "registro"; // Return the name of the registration view (e.g., "registro.html")
    }

    @PostMapping("/home/registro")
    public String procesarRegistro(
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String telefono,
            RedirectAttributes redirectAttributes) {

        if (usuarioService.obtenerUsuarioPorEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "El correo electrónico ya está registrado");
            return "redirect:/usuario/home/registro";
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPassword(password); // Plain password, will be encoded by service
        usuario.setTelefono(telefono);
        // Roles will be set by service in registrarUsuario

        usuarioService.registrarUsuario(usuario);

        redirectAttributes.addFlashAttribute("success", "¡Registro exitoso! Por favor inicia sesión.");
        return "redirect:/usuario/login"; // Redirect to login page
    }

    @GetMapping("/logout")
    public String logoutRedirect() {
        // Spring Security handles the actual logout at POST /logout or configured logout URL.
        // This GET mapping is just to redirect to login page with a logout message,
        // assuming SecurityConfig is set up for .logoutSuccessUrl("/usuario/login?logout=true")
        return "redirect:/usuario/login?logout=true";
    }

    // The /perfil and /actualizar-perfil methods still use HttpSession.
    // These will need to be updated in a separate step to use Spring Security's AuthenticationPrincipal.
    // For now, they might not work as expected after Spring Security integration.
    @GetMapping("/perfil")
    public String mostrarPerfil(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            // This check might need to be replaced with Spring Security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || (authentication instanceof AnonymousAuthenticationToken)) {
                 return "redirect:/usuario/login";
            }
            // If authenticated, ideally load user details from service using authentication.getName() (email)
            // For now, this part will likely fail or show stale data if session 'usuario' isn't set post-Spring Security login
             Object principal = authentication.getPrincipal();
             if (principal instanceof org.springframework.security.core.userdetails.User) {
                 String email = ((org.springframework.security.core.userdetails.User) principal).getUsername();
                 usuario = usuarioService.obtenerUsuarioPorEmail(email).orElse(null);
             } else {
                 // Handle cases where principal is not the User object, or user not found
                 return "redirect:/usuario/login?error=User_details_not_found";
             }

        }
        model.addAttribute("usuario", usuario);
        return "perfil";
    }

    @PostMapping("/actualizar-perfil")
    public String actualizarPerfil(
            @RequestParam String nombre,
            @RequestParam String telefono,
            @RequestParam(required = false) String password, // Optional: allow password change
            HttpSession session, // To be replaced
            RedirectAttributes redirectAttributes) {

        Usuario currentUsuario = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            Object principal = authentication.getPrincipal();
            String email;
            if (principal instanceof org.springframework.security.core.userdetails.User) {
                email = ((org.springframework.security.core.userdetails.User) principal).getUsername();
            } else { // Assuming it's a String if not UserDetails (e.g. in tests or custom setups)
                email = principal.toString();
            }
            currentUsuario = usuarioService.obtenerUsuarioPorEmail(email)
                                  .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado para actualizar"));
        } else {
            return "redirect:/usuario/login";
        }

        currentUsuario.setNombre(nombre);
        currentUsuario.setTelefono(telefono);
        
        // Handle password update if provided
        if (password != null && !password.isEmpty()) {
            // The service's actualizarUsuario should handle encoding if password is to be changed
            currentUsuario.setPassword(password); 
        } else {
            
            if (password == null || password.trim().isEmpty()) {
                // To ensure the existing password is kept, we must not set it if the input is empty.
                // to ensure the service's logic doesn't misinterpret an empty field.
                 Usuario dbUser = usuarioService.obtenerUsuarioPorEmail(currentUsuario.getEmail()).orElse(null);
                 if(dbUser != null) currentUsuario.setPassword(dbUser.getPassword());
            }
        }


        usuarioService.actualizarUsuario(currentUsuario);
        redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente");
        
        // Update Authentication object if username/email changes (not the case here)


        return "redirect:/usuario/perfil";
    }
    
    @GetMapping("/api/usuario/actual")
    @ResponseBody
    public Map<String, Object> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            Usuario usuario = usuarioService.obtenerUsuarioPorEmail(email).orElse(null);
            
            if (usuario != null) {
                response.put("id", usuario.getId());
                response.put("nombre", usuario.getNombre());
                response.put("email", usuario.getEmail());
                response.put("roles", usuario.getRoles());
                return response;
            }
        }
        
        response.put("error", "Usuario no autenticado");
        return response;
    }
}




