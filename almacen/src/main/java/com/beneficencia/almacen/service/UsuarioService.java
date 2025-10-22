package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.Usuario;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de usuarios
 * Define la interfaz de operaciones disponibles para usuarios
 *
 * @author Equipo de Desarrollo
 */
public interface UsuarioService {

    /**
     * Obtiene todos los usuarios del sistema
     *
     * @return Lista de usuarios
     */
    List<Usuario> obtenerTodosUsuarios();

    /**
     * Busca un usuario por su ID
     *
     * @param id ID del usuario
     * @return Optional con el usuario encontrado
     */
    Optional<Usuario> obtenerUsuarioPorId(Long id);

    /**
     * Busca un usuario por su nombre de usuario
     *
     * @param username Nombre de usuario
     * @return Optional con el usuario encontrado
     */
    Optional<Usuario> obtenerUsuarioPorUsername(String username);

    /**
     * Guarda un nuevo usuario en el sistema
     *
     * @param usuario Usuario a guardar
     * @return Usuario guardado
     */
    Usuario guardarUsuario(Usuario usuario);

    /**
     * Elimina un usuario por su ID
     *
     * @param id ID del usuario a eliminar
     */
    void eliminarUsuario(Long id);

    /**
     * Verifica si existe un usuario con el nombre de usuario especificado
     *
     * @param username Nombre de usuario a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existeUsuarioPorUsername(String username);
}