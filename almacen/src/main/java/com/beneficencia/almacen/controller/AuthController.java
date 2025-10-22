package com.beneficencia.almacen.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador para manejar la autenticación y páginas públicas
 *
 * @author Equipo de Desarrollo
 */
@Controller
public class AuthController {

    /**
     * Muestra la página de inicio de sesión
     *
     * @param error Parámetro opcional para indicar error en login
     * @param logout Parámetro opcional para indicar logout exitoso
     * @param model Modelo para pasar datos a la vista
     * @return Nombre de la vista JSP
     */
    @GetMapping("/login")
    public String mostrarLogin(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
        }

        if (logout != null) {
            model.addAttribute("message", "Sesión cerrada exitosamente");
        }

        return "login";
    }
}