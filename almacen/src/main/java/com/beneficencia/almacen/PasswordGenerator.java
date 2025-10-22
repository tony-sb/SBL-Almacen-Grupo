package com.beneficencia.almacen;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        System.out.println("Contraseñas encriptadas para '123456':");
        System.out.println("admin: " + passwordEncoder.encode("123456"));
        System.out.println("almacenero: " + passwordEncoder.encode("123456"));
        System.out.println("usuario: " + passwordEncoder.encode("123456"));
    }
}