package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.Usuario;
import com.beneficencia.almacen.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de usuarios
 *
 * @author Equipo de Desarrollo
 */
@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

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
        return usuarioRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Usuario> obtenerUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Usuario guardarUsuario(Usuario usuario) {
        // Encriptar contraseña antes de guardar
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
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
}