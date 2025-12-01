package com.beneficencia.almacen.repository;

import com.beneficencia.almacen.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para operaciones CRUD y consultas personalizadas de la entidad Usuario.
 * Proporciona métodos para gestionar usuarios del sistema con opciones de búsqueda
 * optimizadas que incluyen la carga de roles para operaciones de autenticación y autorización.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre de usuario único.
     * Utilizado principalmente en procesos de autenticación y login.
     *
     * @param username Nombre de usuario único a buscar
     * @return Optional con el usuario encontrado o vacío si no existe
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Verifica si existe un usuario con el nombre de usuario especificado.
     * Utilizado para validaciones de duplicados durante el registro de nuevos usuarios.
     *
     * @param username Nombre de usuario a verificar
     * @return true si existe un usuario con ese nombre, false en caso contrario
     */
    boolean existsByUsername(String username);

    /**
     * Busca un usuario por nombre de usuario cargando eagermente sus roles.
     * Optimiza las consultas de autenticación evitando el problema N+1.
     * Utilizado en el proceso de login y autorización de Spring Security.
     *
     * @param username Nombre de usuario único a buscar
     * @return Optional con el usuario encontrado y sus roles cargados
     */
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<Usuario> findByUsernameWithRoles(String username);

    /**
     * Busca un usuario por ID cargando eagermente sus roles.
     * Utilizado en operaciones de edición de usuarios para mostrar y modificar
     * los roles asignados sin necesidad de consultas adicionales.
     *
     * @param id ID del usuario a buscar
     * @return Optional con el usuario encontrado y sus roles cargados
     */
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<Usuario> findByIdWithRoles(Long id);
}