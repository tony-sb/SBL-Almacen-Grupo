package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.model.Usuario;
import com.beneficencia.almacen.service.UsuarioServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioServiceImpl usuarioService;

    // 1. LISTAR USUARIOS
    @GetMapping
    public String listarUsuarios(Model model, Authentication authentication) {
        if (!esAdmin(authentication)) {
            return "redirect:/access-denied";
        }

        List<Usuario> usuarios = usuarioService.obtenerTodosUsuarios();
        model.addAttribute("usuarios", usuarios);
        return "usuarios/lista-usuarios";
    }

    // 2. FORMULARIO NUEVO USUARIO
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoUsuario(Model model, Authentication authentication) {
        if (!esAdmin(authentication)) {
            return "redirect:/access-denied";
        }

        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", usuarioService.obtenerTodosRoles());
        model.addAttribute("modo", "nuevo");
        return "usuarios/form-usuario";
    }

    // 3. EDITAR USUARIO (EL QUE TE DI)
    @GetMapping("/editar/{id}")
    public String editarUsuario(@PathVariable Long id, Model model, Authentication authentication) {
        if (!esAdmin(authentication)) {
            return "redirect:/access-denied";
        }

        try {
            Usuario usuario = usuarioService.obtenerUsuarioPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

            model.addAttribute("usuario", usuario);
            model.addAttribute("roles", usuarioService.obtenerTodosRoles());
            model.addAttribute("modo", "editar");

            return "usuarios/form-usuario";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/usuarios?error=Error al cargar usuario para editar: " + e.getMessage();
        }
    }

    // 4. GUARDAR USUARIO (NUEVO O EDITADO)
    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario,
                                 @RequestParam(value = "rolesIds", required = false) List<Long> rolesIds,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        if (!esAdmin(authentication)) {
            return "redirect:/access-denied";
        }

        try {
            usuarioService.guardarUsuarioConRoles(usuario, rolesIds);
            redirectAttributes.addFlashAttribute("success",
                    usuario.getId() == null ? "Usuario creado exitosamente" : "Usuario actualizado exitosamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar el usuario: " + e.getMessage());
        }

        return "redirect:/usuarios";
    }

    // 5. ELIMINAR USUARIO
    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (!esAdmin(authentication)) {
            return "redirect:/access-denied";
        }

        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("success", "Usuario eliminado exitosamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el usuario: " + e.getMessage());
        }

        return "redirect:/usuarios";
    }

    // MÉTODO AUXILIAR
    private boolean esAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}