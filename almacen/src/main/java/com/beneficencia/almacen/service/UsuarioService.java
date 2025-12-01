package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.Usuario;
import com.beneficencia.almacen.model.Rol;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de usuarios del sistema.
 * Define la interfaz de operaciones disponibles para usuarios incluyendo
 * operaciones CRUD, gestión de roles y validaciones de seguridad.
 */
public interface UsuarioService {

    /**
     * Obtiene todos los usuarios registrados en el sistema.
     * Incluye información básica de los usuarios sin cargar relaciones complejas.
     *
     * @return Lista completa de todos los usuarios del sistema
     */
    List<Usuario> obtenerTodosUsuarios();

    /**
     * Busca un usuario por su ID único.
     * Incluye la carga de roles para operaciones que requieren información de autorización.
     *
     * @param id ID del usuario a buscar
     * @return Optional con el usuario encontrado y sus roles cargados, o vacío si no existe
     */
    Optional<Usuario> obtenerUsuarioPorId(Long id);

    /**
     * Busca un usuario por su nombre de usuario único.
     * Utilizado principalmente en procesos de autenticación y validación.
     *
     * @param username Nombre de usuario único a buscar
     * @return Optional con el usuario encontrado, o vacío si no existe
     */
    Optional<Usuario> obtenerUsuarioPorUsername(String username);

    /**
     * Guarda un nuevo usuario en el sistema.
     * Realiza validaciones de duplicados y encriptación de contraseñas.
     *
     * @param usuario Usuario a guardar
     * @return Usuario guardado con ID generado y datos procesados
     */
    Usuario guardarUsuario(Usuario usuario);

    /**
     * Guarda un usuario con roles específicos asignados.
     * Asigna los roles indicados al usuario y maneja la encriptación de contraseñas.
     *
     * @param usuario Usuario a guardar o actualizar
     * @param rolesIds Lista de IDs de roles a asignar al usuario
     * @return Usuario guardado con los roles asignados
     */
    Usuario guardarUsuarioConRoles(Usuario usuario, List<Long> rolesIds);

    /**
     * Elimina un usuario del sistema por su ID.
     * Realiza validaciones de existencia antes de proceder con la eliminación.
     *
     * @param id ID del usuario a eliminar
     */
    void eliminarUsuario(Long id);

    /**
     * Verifica si existe un usuario con el nombre de usuario especificado.
     * Utilizado para validaciones de duplicados durante el registro de nuevos usuarios.
     *
     * @param username Nombre de usuario a verificar
     * @return true si existe un usuario con ese nombre, false en caso contrario
     */
    boolean existeUsuarioPorUsername(String username);

    /**
     * Obtiene todos los roles disponibles en el sistema.
     * Utilizado en formularios de creación y edición de usuarios para asignar permisos.
     *
     * @return Lista de todos los roles del sistema
     */
    List<Rol> obtenerTodosRoles();
}