package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.Usuario;
import com.beneficencia.almacen.model.Rol;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de usuarios
 * Define la interfaz de operaciones disponibles para usuarios
 */
public interface UsuarioService {

    /**
     * Obtiene todos los usuarios del sistema
     */
    List<Usuario> obtenerTodosUsuarios();

    /**
     * Busca un usuario por su ID
     */
    Optional<Usuario> obtenerUsuarioPorId(Long id);

    /**
     * Busca un usuario por su nombre de usuario
     */
    Optional<Usuario> obtenerUsuarioPorUsername(String username);

    /**
     * Guarda un nuevo usuario en el sistema
     */
    Usuario guardarUsuario(Usuario usuario);

    /**
     * Guarda un usuario con roles específicos
     */
    Usuario guardarUsuarioConRoles(Usuario usuario, List<Long> rolesIds);

    /**
     * Elimina un usuario por su ID
     */
    void eliminarUsuario(Long id);

    /**
     * Verifica si existe un usuario con el nombre de usuario especificado
     */
    boolean existeUsuarioPorUsername(String username);

    /**
     * Obtiene todos los roles disponibles
     */
    List<Rol> obtenerTodosRoles();
}