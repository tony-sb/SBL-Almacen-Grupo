package com.beneficencia.almacen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación Spring Boot para el Sistema de Gestión de Almacén.
 * Esta clase inicia la aplicación y configura automáticamente el contexto de Spring.
 * La anotación @SpringBootApplication combina tres anotaciones principales:
 * - @Configuration: Marca la clase como fuente de definiciones de beans
 * - @EnableAutoConfiguration: Habilita la configuración automática de Spring Boot
 * - @ComponentScan: Escanea los componentes en el paquete actual y sus subpaquetes
 */
@SpringBootApplication
public class AlmacenApplication {

    /**
     * Método principal que inicia la aplicación Spring Boot.
     * Este método es el punto de entrada de la aplicación y se encarga de:
     * - Crear el ApplicationContext de Spring
     * - Cargar todas las configuraciones automáticas
     * - Iniciar el servidor web embebido (Tomcat)
     * - Escanear y registrar todos los componentes (@Component, @Service, @Repository, @Controller)
     * @param args Argumentos de línea de comandos (opcionales)
     */
    public static void main(String[] args) {
        SpringApplication.run(AlmacenApplication.class, args);
    }
}