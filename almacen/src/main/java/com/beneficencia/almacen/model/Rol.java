package com.beneficencia.almacen.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa un rol de usuario en el sistema de almacén.
 * Define los permisos y niveles de acceso que pueden tener los usuarios
 * del sistema mediante el sistema de autorización basado en roles.
 */
@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre único del rol.
     * Campo único y obligatorio para identificación del rol (ej: ADMIN, ALMACENERO).
     * Se utiliza en el sistema de autorización para control de acceso.
     */
    @Column(unique = true, nullable = false)
    private String nombre;

    /**
     * Descripción del rol y sus permisos.
     * Información adicional sobre las capacidades y responsabilidades del rol.
     */
    private String descripcion;

    /**
     * Conjunto de usuarios que tienen asignado este rol.
     * Relación Many-to-Many bidireccional con la entidad Usuario.
     * Mapeada por el campo "roles" en la entidad Usuario.
     */
    @ManyToMany(mappedBy = "roles")
    private Set<Usuario> usuarios = new HashSet<>();

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Rol() {}

    /**
     * Constructor con parámetro nombre para crear instancias de roles.
     *
     * @param nombre Nombre del rol a crear
     */
    public Rol(String nombre) {
        this.nombre = nombre;
    }

    // Getters y Setters

    /**
     * Obtiene el ID único del rol.
     *
     * @return ID del rol
     */
    public Long getId() { return id; }

    /**
     * Establece el ID único del rol.
     *
     * @param id ID del rol
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Obtiene el nombre único del rol.
     *
     * @return Nombre del rol
     */
    public String getNombre() { return nombre; }

    /**
     * Establece el nombre único del rol.
     *
     * @param nombre Nombre del rol
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Obtiene la descripción del rol.
     *
     * @return Descripción del rol
     */
    public String getDescripcion() { return descripcion; }

    /**
     * Establece la descripción del rol.
     *
     * @param descripcion Descripción del rol
     */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /**
     * Obtiene el conjunto de usuarios que tienen este rol asignado.
     *
     * @return Conjunto de usuarios con este rol
     */
    public Set<Usuario> getUsuarios() { return usuarios; }

    /**
     * Establece el conjunto de usuarios que tienen este rol asignado.
     *
     * @param usuarios Conjunto de usuarios con este rol
     */
    public void setUsuarios(Set<Usuario> usuarios) { this.usuarios = usuarios; }
}