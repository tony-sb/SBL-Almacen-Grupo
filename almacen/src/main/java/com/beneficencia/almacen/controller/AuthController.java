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
        String errorMessage = (String) request.getSession().getAttribute("errorMessage");

        if (errorMessage != null) {
            model.addAttribute("error", errorMessage);
            request.getSession().removeAttribute("errorMessage");
        }
        else if (errorParam != null) {
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