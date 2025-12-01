package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.Usuario;
import com.beneficencia.almacen.model.Rol;
import com.beneficencia.almacen.repository.UsuarioRepository;
import com.beneficencia.almacen.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

/**
 * Implementación del servicio de usuarios para la gestión del sistema de autenticación.
 * Proporciona la lógica de negocio para las operaciones CRUD de usuarios, incluyendo
 * gestión de roles, encriptación de contraseñas y validaciones de seguridad.
 */
@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Usuario> obtenerTodosUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Usuario> obtenerUsuarioPorId(Long id) {
        // Usa el método que carga los roles eagerly para evitar LazyInitializationException
        return usuarioRepository.findByIdWithRoles(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Usuario> obtenerUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsernameWithRoles(username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Usuario guardarUsuario(Usuario usuario) {
        // Si es un usuario nuevo, encriptar la contraseña
        if (usuario.getId() == null) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        } else {
            // Si está editando, mantener la contraseña actual si no se cambió
            Usuario usuarioExistente = usuarioRepository.findById(usuario.getId()).orElse(null);
            if (usuarioExistente != null && usuario.getPassword() != null &&
                    !usuario.getPassword().isEmpty() &&
                    !usuario.getPassword().equals(usuarioExistente.getPassword())) {
                // La contraseña fue cambiada, encriptar la nueva
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            } else if (usuarioExistente != null) {
                // Mantener la contraseña existente
                usuario.setPassword(usuarioExistente.getPassword());
            }
        }
        return usuarioRepository.save(usuario);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Usuario guardarUsuarioConRoles(Usuario usuario, List<Long> rolesIds) {
        Set<Rol> roles = new HashSet<>();
        if (rolesIds != null) {
            for (Long rolId : rolesIds) {
                rolRepository.findById(rolId).ifPresent(roles::add);
            }
        }
        usuario.setRoles(roles);
        return guardarUsuario(usuario);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existeUsuarioPorUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Rol> obtenerTodosRoles() {
        return rolRepository.findAll();
    }
}