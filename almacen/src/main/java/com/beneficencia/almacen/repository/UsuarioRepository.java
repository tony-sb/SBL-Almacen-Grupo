package com.beneficencia.almacen.repository;

import com.beneficencia.almacen.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<Usuario> findByUsernameWithRoles(String username);

    //CARGAR ROLES AL EDITAR
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<Usuario> findByIdWithRoles(Long id);
}