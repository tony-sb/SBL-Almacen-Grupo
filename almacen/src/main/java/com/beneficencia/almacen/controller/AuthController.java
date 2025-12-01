package com.beneficencia.almacen.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador para manejar la autenticación y páginas públicas del sistema.
 * Gestiona las operaciones relacionadas con el inicio de sesión y registro de usuarios.
 */
@Controller
public class AuthController {

    /**
     * Muestra la página de inicio de sesión y maneja los parámetros de error y logout.
     * Procesa las solicitudes GET a la ruta /login y configura los mensajes apropiados
     * según el resultado de la operación de autenticación.
     *
     * @param error Parámetro opcional que indica si hubo un error en el intento de login
     * @param logout Parámetro opcional que indica si el usuario cerró sesión exitosamente
     * @param model Modelo para pasar datos y mensajes a la vista
     * @return Nombre de la vista JSP 'login' que será renderizada
     */
    @GetMapping("/login")
    public String mostrarLogin(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        // Agrega mensaje de error al modelo si las credenciales son incorrectas
        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
        }

        // Agrega mensaje de confirmación al modelo si el logout fue exitoso
        if (logout != null) {
            model.addAttribute("message", "Sesión cerrada exitosamente");
        }

        return "login";
    }

    /**
     * Muestra la página de registro de nuevos usuarios.
     * Procesa las solicitudes GET a la ruta /registro para mostrar el formulario
     * de creación de nuevas cuentas de usuario.
     *
     * @return Nombre de la vista JSP 'registro' que será renderizada
     */
    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "registro";
    }
}