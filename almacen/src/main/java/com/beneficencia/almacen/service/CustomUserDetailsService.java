package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.Usuario;
import com.beneficencia.almacen.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * Servicio personalizado para la carga de detalles de usuario en Spring Security.
 * Implementa la interfaz UserDetailsService para integrar la autenticación
 * del sistema con la entidad Usuario personalizada de la aplicación.
 */
@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Carga los detalles de un usuario por su nombre de usuario para la autenticación de Spring Security.
     * Este método es llamado automáticamente por Spring Security durante el proceso de login.
     *
     * @param username Nombre de usuario proporcionado en el formulario de login
     * @return UserDetails con la información de autenticación y autorización del usuario
     * @throws UsernameNotFoundException si no se encuentra un usuario con el nombre de usuario proporcionado
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscar el usuario en la base de datos con sus roles cargados
        Usuario usuario = usuarioRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Convertir los roles del usuario en autoridades de Spring Security
        var authorities = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombre()))
                .collect(Collectors.toList());

        // Construir el objeto UserDetails que Spring Security utiliza para la autenticación
        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(authorities)
                .disabled(!usuario.isEnabled())
                .build();
    }
}