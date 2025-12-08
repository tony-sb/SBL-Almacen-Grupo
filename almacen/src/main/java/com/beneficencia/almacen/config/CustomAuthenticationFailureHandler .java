package com.beneficencia.almacen.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
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

        System.out.println("=== DEBUG: ANTES DE REDIRIGIR ===");
        System.out.println("Request URL: " + request.getRequestURL());
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Context Path: " + request.getContextPath());

        // PRUEBA esto primero:
        response.setStatus(HttpServletResponse.SC_FOUND); // 302
        response.setHeader("Location", "/error/access-denied?reason=disabled");
        System.out.println("=== DEBUG: DESPUÉS DE REDIRIGIR ===");
    }
}