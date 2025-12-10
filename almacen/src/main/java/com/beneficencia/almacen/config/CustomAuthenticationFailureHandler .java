package com.beneficencia.almacen.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        // Depuración
        System.out.println("=== DEPURACIÓN: Excepción de autenticación ===");
        System.out.println("Tipo de excepción: " + exception.getClass().getName());
        System.out.println("Mensaje: " + exception.getMessage());

        String redirectUrl;

        // Verificar si es un usuario deshabilitado
        if (exception instanceof DisabledException) {
            // SOLO PARA USUARIOS INACTIVOS: redirige a la página de acceso denegado
            System.out.println("Usuario inactivo detectado - Redirigiendo a access-denied");
            redirectUrl = "/error/access-denied?reason=disabled";
        } else if (exception instanceof BadCredentialsException) {
            // Para credenciales incorrectas, redirige a login con error
            System.out.println("Credenciales incorrectas - Redirigiendo a login");
            request.getSession().setAttribute("errorMessage", "Usuario o contraseña incorrectos");
            redirectUrl = "/login?error=bad_credentials";
        } else if (exception instanceof LockedException) {
            request.getSession().setAttribute("errorMessage", "Usuario bloqueado. Contacte al administrador.");
            redirectUrl = "/login?error=locked";
        } else if (exception instanceof AccountExpiredException) {
            request.getSession().setAttribute("errorMessage", "Cuenta expirada. Contacte al administrador.");
            redirectUrl = "/login?error=expired";
        } else {
            // Cualquier otro error
            request.getSession().setAttribute("errorMessage", "Error de autenticación. Intente nuevamente.");
            redirectUrl = "/login?error";
        }

        // Redirigir
        response.sendRedirect(request.getContextPath() + redirectUrl);
    }
}