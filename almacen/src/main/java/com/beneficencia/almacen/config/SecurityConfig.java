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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    // ¡IMPORTANTE! Debes tener esto:
    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        // Recursos estáticos accesibles sin autenticación
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**").permitAll()
                        // Páginas de login, registro y ERROR deben ser accesibles sin autenticación
                        .requestMatchers("/login", "/registro", "/error/**").permitAll() // ← ¡AGREGA ESTO!
                        // Rutas de usuarios solo accesibles por ADMIN
                        .requestMatchers("/usuarios/**").hasRole("ADMIN")
                        // Rutas de órdenes, productos y dashboard accesibles por ADMIN, ALMACENERO y USUARIO
                        .requestMatchers("/ordenes-salida/**", "/ordenes-abastecimiento/**", "/productos/**", "/dashboard").hasAnyRole("ADMIN", "ALMACENERO", "USUARIO")
                        // Rutas raíz e inicio requieren autenticación básica
                        .requestMatchers("/", "/inicio").authenticated()
                        // Cualquier otra request requiere autenticación
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/error/access-denied?reason=disabled") // Esta ruta ahora será permitida
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/error/access-denied")
                )
                .authenticationProvider(authenticationProvider());

        return http.build();
    }
}