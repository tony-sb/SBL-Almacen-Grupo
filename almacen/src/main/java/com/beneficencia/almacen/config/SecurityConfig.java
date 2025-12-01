package com.beneficencia.almacen.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Clase de configuración para la seguridad de la aplicación.
 * Define la configuración de autenticación, autorización y políticas de seguridad.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Configura el codificador de contraseñas usando BCrypt.
     * BCrypt es un algoritmo de hashing seguro para contraseñas.
     *
     * @return Instancia de PasswordEncoder con BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura el proveedor de autenticación que utiliza UserDetailsService
     * y el codificador de contraseñas.
     *
     * @return DaoAuthenticationProvider configurado
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Define la cadena de filtros de seguridad que establece las políticas
     * de acceso, login, logout y manejo de excepciones.
     *
     * @param http Objeto HttpSecurity para configurar la seguridad
     * @return SecurityFilterChain configurado
     * @throws Exception sí ocurre error en la configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilita CSRF para simplificar el desarrollo (en producción considerar habilitarlo)
                .csrf(csrf -> csrf.disable())

                // Configuración de autorización de requests
                .authorizeHttpRequests(authz -> authz
                        // Recursos estáticos accesibles sin autenticación
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**").permitAll()
                        // Páginas de login y registro accesibles sin autenticación
                        .requestMatchers("/login", "/registro").permitAll()
                        // Rutas de usuarios solo accesibles por ADMIN
                        .requestMatchers("/usuarios/**").hasRole("ADMIN")
                        // Rutas de órdenes, productos y dashboard accesibles por ADMIN y ALMACENERO
                        .requestMatchers("/ordenes-salida/**", "/ordenes-abastecimiento/**", "/productos/**", "/dashboard").hasAnyRole("ADMIN", "ALMACENERO")
                        // Rutas raíz e inicio requieren autenticación básica
                        .requestMatchers("/", "/inicio").authenticated()
                        // Cualquier otra request requiere autenticación
                        .anyRequest().authenticated()
                )

                // Configuración del formulario de login
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // Configuración del logout
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // Manejo de excepciones de acceso
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/access-denied")
                )

                // Establece el proveedor de autenticación personalizado
                .authenticationProvider(authenticationProvider());

        return http.build();
    }
}