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

/**
 * Controlador para la gestión de usuarios del sistema.
 * Maneja las operaciones CRUD de usuarios incluyendo creación, edición, eliminación
 * y asignación de roles. Solo accesible para usuarios con rol ADMIN.
 */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioServiceImpl usuarioService;

    /**
     * Muestra la lista de todos los usuarios registrados en el sistema.
     * Acceso restringido exclusivamente a usuarios con rol ADMIN.
     *
     * @param model Modelo para pasar datos a la vista
     * @param authentication Información de autenticación del usuario actual
     * @return Nombre de la vista 'usuarios/lista-usuarios' o redirección a acceso denegado
     */
    @GetMapping
    public String listarUsuarios(Model model, Authentication authentication) {
        if (!esAdmin(authentication)) {
            return "redirect:/access-denied";
        }

        List<Usuario> usuarios = usuarioService.obtenerTodosUsuarios();
        model.addAttribute("usuarios", usuarios);
        return "usuarios/lista-usuarios";
    }

    /**
     * Muestra el formulario para crear un nuevo usuario.
     * Incluye la lista de roles disponibles para asignar al nuevo usuario.
     * Acceso restringido exclusivamente a usuarios con rol ADMIN.
     *
     * @param model Modelo para pasar datos a la vista
     * @param authentication Información de autenticación del usuario actual
     * @return Nombre de la vista 'usuarios/form-usuario' o redirección a acceso denegado
     */
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

    /**
     * Muestra el formulario para editar un usuario existente.
     * Carga los datos del usuario especificado por ID y los roles disponibles.
     * Acceso restringido exclusivamente a usuarios con rol ADMIN.
     *
     * @param id ID del usuario a editar
     * @param model Modelo para pasar datos a la vista
     * @param authentication Información de autenticación del usuario actual
     * @return Nombre de la vista 'usuarios/form-usuario' o redirección en caso de error
     */
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

    /**
     * Procesa el guardado de un usuario (nuevo o editado).
     * Asigna los roles seleccionados al usuario y maneja la encriptación de contraseñas.
     * Acceso restringido exclusivamente a usuarios con rol ADMIN.
     *
     * @param usuario Objeto Usuario con los datos del formulario
     * @param rolesIds Lista de IDs de roles a asignar al usuario
     * @param authentication Información de autenticación del usuario actual
     * @param redirectAttributes Atributos para mensajes flash en redirección
     * @return Redirección a la lista de usuarios
     */
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

    /**
     * Elimina un usuario del sistema.
     * Realiza la eliminación del usuario especificado por ID.
     * Acceso restringido exclusivamente a usuarios con rol ADMIN.
     *
     * @param id ID del usuario a eliminar
     * @param authentication Información de autenticación del usuario actual
     * @param redirectAttributes Atributos para mensajes flash en redirección
     * @return Redirección a la lista de usuarios
     */
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

    /**
     * Método auxiliar para verificar si el usuario autenticado tiene rol ADMIN.
     * Utilizado para controlar el acceso a las funcionalidades de administración de usuarios.
     *
     * @param authentication Información de autenticación del usuario actual
     * @return true si el usuario tiene rol ADMIN, false en caso contrario
     */
    private boolean esAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}