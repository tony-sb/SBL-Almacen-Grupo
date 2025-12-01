package com.beneficencia.almacen;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Utilidad para generar contraseñas encriptadas usando BCrypt.
 * Esta clase es útil para crear contraseñas iniciales para usuarios de prueba
 * o para resetear contraseñas en el sistema de manera segura.
 * BCrypt es un algoritmo de hashing seguro que incluye salt automáticamente,
 * lo que garantiza que incluso contraseñas idénticas tengan hashes diferentes.
 */
public class PasswordGenerator {

    /**
     * Método principal que genera contraseñas encriptadas para usuarios de prueba.
     * Ejecuta BCrypt para encriptar la contraseña "123456" y muestra los resultados.
     * @param args Argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        // Crear el codificador de contraseñas usando BCrypt
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        System.out.println("Contraseñas encriptadas para '123456':");

        // Generar y mostrar contraseñas encriptadas para diferentes usuarios
        System.out.println("admin: " + passwordEncoder.encode("123456"));
        System.out.println("almacenero: " + passwordEncoder.encode("123456"));
        System.out.println("usuario: " + passwordEncoder.encode("123456"));

        System.out.println("\nNota: Cada ejecución generará hashes diferentes debido al salt incorporado.");
        System.out.println("Esto es normal y esperado en BCrypt.");
    }
}