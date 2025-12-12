package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.Usuario;
import com.beneficencia.almacen.model.Rol;
import java.util.List;
import java.util.Optional;

public interface UsuarioService {

    List<Usuario> obtenerTodosUsuarios();

    Optional<Usuario> obtenerUsuarioPorId(Long id);

    Optional<Usuario> obtenerUsuarioPorUsername(String username);

    Usuario guardarUsuario(Usuario usuario);

    Usuario guardarUsuarioConRoles(Usuario usuario, List<Long> rolesIds);

    void eliminarUsuario(Long id);

    boolean existeUsuarioPorUsername(String username);

    List<Rol> obtenerTodosRoles();
}