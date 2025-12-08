package com.beneficencia.almacen.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.DisabledException; // ← ¡IMPORTANTE!
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String mostrarLogin(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model,
            HttpServletRequest request) {

        if (error != null) {
            Exception exception = (Exception) request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");

            // ¡¡¡CORRECCIÓN AQUÍ!!! Debes verificar DisabledException
            if (exception instanceof DisabledException) {
                return "redirect:/access-denied?reason=disabled";
            } else {
                model.addAttribute("error", "Usuario o contraseña incorrectos");
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