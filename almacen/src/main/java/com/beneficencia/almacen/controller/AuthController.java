package com.beneficencia.almacen.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String mostrarLogin(
            @RequestParam(value = "error", required = false) String errorParam,
            @RequestParam(value = "logout", required = false) String logout,
            Model model,
            HttpServletRequest request) {

        // Recuperar mensaje de error de la sesión si existe
        String errorMessage = (String) request.getSession().getAttribute("errorMessage");

        // Limpiar atributo de sesión después de usarlo
        if (errorMessage != null) {
            model.addAttribute("error", errorMessage);
            request.getSession().removeAttribute("errorMessage");
        }
        // Si hay parámetro de error pero no mensaje en sesión
        else if (errorParam != null) {
            // Solo para errores que no sean de usuario inactivo
            if ("bad_credentials".equals(errorParam)) {
                model.addAttribute("error", "Usuario o contraseña incorrectos.");
            } else if ("locked".equals(errorParam)) {
                model.addAttribute("error", "Usuario bloqueado. Contacte al administrador.");
            } else if ("expired".equals(errorParam)) {
                model.addAttribute("error", "Cuenta expirada. Contacte al administrador.");
            } else {
                model.addAttribute("error", "Error de autenticación. Intente nuevamente.");
            }
        }

        if (logout != null) {
            model.addAttribute("message", "Sesión cerrada exitosamente");
        }

        return "login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "registro";
    }
}