package com.example.peloteros.controller.admin;

import com.example.peloteros.model.*;
import com.example.peloteros.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final UsuarioService usuarioService;
    private final CanchaService canchaService;
    private final ReservaService reservaService;
    private final PagoService pagoService;

    @Autowired
    public AdminController(UsuarioService usuarioService,
            CanchaService canchaService,
            ReservaService reservaService,
            PagoService pagoService) {
        this.usuarioService = usuarioService;
        this.canchaService = canchaService;
        this.reservaService = reservaService;
        this.pagoService = pagoService;
    }
    //comentariox

    // Panel principal
    @GetMapping("/paneladmin")
    public String panelAdmin(Model model) {
        try {
            model.addAttribute("page", "paneladmin");
            model.addAttribute("totalUsuarios", usuarioService.contarUsuarios());
            model.addAttribute("totalCanchas", canchaService.contarCanchas());
            model.addAttribute("reservasHoy", reservaService.contarReservasHoy());
            model.addAttribute("ingresosHoy", pagoService.calcularIngresosHoy());
            return "admin/paneladmin"; // CORREGIDO
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el panel: " + e.getMessage());
            return "admin/paneladmin"; // CORREGIDO
        }
    }

    // 1. Gestión de usuarios (CRUD completo)
    @GetMapping("/usuarios")
    public String gestionUsuarios(
            Model model,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "") String search) {

        try {
            Pageable pageable = PageRequest.of(page - 1, DEFAULT_PAGE_SIZE);
            Page<Usuario> usuariosPage = usuarioService.listarTodosPaginados(search, pageable);

            model.addAttribute("usuariosPage", usuariosPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", usuariosPage.getTotalPages());
            model.addAttribute("searchQuery", search);

            // Para mostrar números de página en la vista
            int totalPages = usuariosPage.getTotalPages();
            if (totalPages > 0) {
                List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                        .boxed()
                        .collect(Collectors.toList());
                model.addAttribute("pageNumbers", pageNumbers);
            }
            model.addAttribute("nuevoUsuario", new Usuario());

            return "admin/usuarios";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar usuarios: " + e.getMessage());
            return "admin/usuarios";
        }
    }

    @GetMapping("/usuarios/nuevo")
    public String mostrarFormularioNuevoUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "admin/usuario-form";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@Valid @ModelAttribute("nuevoUsuario") Usuario usuario,
                        BindingResult result,
                             RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
        redirectAttributes.addFlashAttribute("errorMessage", "Por favor, completa los campos correctamente.");
        return "redirect:/admin/usuarios";
    }

    try {
        usuarioService.crearUsuario(usuario);
        redirectAttributes.addFlashAttribute("successMessage", "Usuario creado exitosamente.");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("errorMessage", "Error al crear usuario: " + e.getMessage());
    }

    return "redirect:/admin/usuarios";
}


    @GetMapping("/usuarios/{id}/editar")
    public String mostrarFormularioEditarUsuario(@PathVariable Long id, Model model) {
        try {
            Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
            model.addAttribute("usuario", usuario);
            return "admin/editar-usuario";
        } catch (Exception e) {
            model.addAttribute("error", "Usuario no encontrado");
            return "redirect:/admin/usuarios";
        }
    }

    @PostMapping("/usuarios/{id}")
    public String actualizarUsuario(@PathVariable Long id,
            @Valid @ModelAttribute Usuario usuario,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/usuario-form";
        }
        try {
            usuario.setId(id);
            usuarioService.actualizarUsuario(usuario);
            redirectAttributes.addFlashAttribute("success", "Usuario actualizado exitosamente");
            return "redirect:/admin/usuarios";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar usuario: " + e.getMessage());
            return "redirect:/admin/usuarios/" + id + "/editar";
        }
    }

    @PostMapping("/usuarios/{id}/eliminar")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("success", "Usuario eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar usuario: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/toggle-status")
    public String toggleUsuarioStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.toggleActivo(id);
            redirectAttributes.addFlashAttribute("success", "Estado del usuario actualizado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar estado: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/update-roles")
    public String updateUserRoles(@PathVariable Long id,
            @RequestParam Set<String> roles,
            RedirectAttributes redirectAttributes) {
        try {
            usuarioService.actualizarRoles(id, roles);
            redirectAttributes.addFlashAttribute("success", "Roles actualizados correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar roles: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    // 2. Gestión de reservas
    @GetMapping("/reservas")
    public String gestionReservas(Model model,
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) Long canchaId) {
        LocalDate fechaFiltro = fecha != null ? LocalDate.parse(fecha) : LocalDate.now();
        List<Reserva> reservas;

        if (canchaId != null) {
            Cancha cancha = canchaService.obtenerCanchaPorId(canchaId);
            reservas = reservaService.obtenerReservasPorCanchaYFecha(cancha, fechaFiltro);
            model.addAttribute("canchaSeleccionada", cancha);
        } else {
            reservas = reservaService.obtenerReservasPorFecha(fechaFiltro);
        }

        model.addAttribute("reservas", reservas);
        model.addAttribute("canchas", canchaService.listarTodas());
        model.addAttribute("fecha", fechaFiltro);
        return "admin/reservas";
    }

    @PostMapping("/reservas/{id}/confirmar")
    public String confirmarReserva(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reservaService.cambiarEstado(id, "CONFIRMADA");
        redirectAttributes.addFlashAttribute("success", "Reserva confirmada");
        return "redirect:/admin/reservas";
    }

    @PostMapping("/reservas/{id}/cancelar")
    public String cancelarReserva(@PathVariable Long id,
            @RequestParam(required = false) String motivo,
            RedirectAttributes redirectAttributes) {
        reservaService.cancelarReserva(id, motivo);
        redirectAttributes.addFlashAttribute("success", "Reserva cancelada");
        return "redirect:/admin/reservas";
    }

    // 3. Gestión de canchas y horarios
    @GetMapping("/canchas")
    public String gestionCanchas(Model model) {
        model.addAttribute("canchas", canchaService.listarTodas());
        model.addAttribute("nuevaCancha", new Cancha());
        return "admin/canchas";
    }

    @PostMapping("/canchas")
    public String crearCancha(@ModelAttribute Cancha cancha, 
                              @RequestParam("fotoFile") MultipartFile fotoFile,
                              RedirectAttributes redirectAttributes) {
        try {
            if (!fotoFile.isEmpty()) {
                cancha.setFoto(fotoFile.getBytes());
            }
            canchaService.guardarCancha(cancha);
            redirectAttributes.addFlashAttribute("success", "Cancha creada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar la cancha: " + e.getMessage());
        }
        return "redirect:/admin/canchas";
    }

    @PostMapping("/canchas/{id}/horarios")
    public String actualizarHorarios(@PathVariable Long id,
            @RequestParam String horarioApertura,
            @RequestParam String horarioCierre,
            RedirectAttributes redirectAttributes) {
        canchaService.actualizarHorarios(id, horarioApertura, horarioCierre);
        redirectAttributes.addFlashAttribute("success", "Horarios actualizados");
        return "redirect:/admin/canchas";
    }
    

    // 4. Historial de pagos
    @GetMapping("/pagos")
    public String historialPagos(Model model,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) Long usuarioId) {
        LocalDate inicio = fechaInicio != null ? LocalDate.parse(fechaInicio) : LocalDate.now().minusDays(30);
        LocalDate fin = fechaFin != null ? LocalDate.parse(fechaFin) : LocalDate.now();

        List<Pago> pagos;
        if (usuarioId != null) {
            Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);
            pagos = pagoService.obtenerPagosPorUsuarioYFecha(usuario, inicio, fin);
            model.addAttribute("usuarioSeleccionado", usuario);
        } else {
            pagos = pagoService.obtenerPagosPorFecha(inicio, fin);
        }

        model.addAttribute("pagos", pagos);
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("fechaInicio", inicio);
        model.addAttribute("fechaFin", fin);
        model.addAttribute("totalIngresos", pagoService.calcularTotalIngresos(inicio, fin));
        return "admin/pagos";
    }

    @PostMapping("/pagos/registrar")
    public String registrarPago(@RequestParam Long reservaId,
            @RequestParam double monto,
            @RequestParam String metodoPago,
            RedirectAttributes redirectAttributes) {
        pagoService.registrarPago(reservaId, monto, metodoPago);
        redirectAttributes.addFlashAttribute("success", "Pago registrado exitosamente");
        return "redirect:/admin/pagos";
    }
}
