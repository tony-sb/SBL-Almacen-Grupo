package com.beneficencia.almacen.repository;

import com.beneficencia.almacen.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para operaciones CRUD y consultas personalizadas de la entidad Rol.
 * Proporciona métodos para gestionar los roles del sistema de autorización,
 * incluyendo la búsqueda por nombre único del rol.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    /**
     * Busca un rol por su nombre único.
     * Utilizado en procesos de autorización y asignación de permisos a usuarios.
     * Los nombres de roles son únicos en el sistema (ej: "ADMIN", "ALMACENERO").
     *
     * @param nombre Nombre único del rol a buscar
     * @return Optional con el rol encontrado o vacío si no existe
     */
    Optional<Rol> findByNombre(String nombre);
}