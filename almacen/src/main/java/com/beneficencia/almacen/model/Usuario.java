package com.beneficencia.almacen.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa un usuario del sistema de almacén.
 * Gestiona la información de autenticación, autorización y datos personales
 * de los usuarios que acceden al sistema, incluyendo la asignación de roles.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre de usuario único para autenticación.
     * Campo único y obligatorio utilizado como identificador de inicio de sesión.
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Contraseña encriptada del usuario.
     * Campo obligatorio que almacena la contraseña de forma segura.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Nombre(s) del usuario.
     * Campo obligatorio para identificación personal.
     */
    @Column(nullable = false)
    private String nombre;

    /**
     * Apellido(s) del usuario.
     * Campo obligatorio para identificación personal.
     */
    @Column(nullable = false)
    private String apellido;

    /**
     * Correo electrónico del usuario.
     * Medio de contacto y recuperación de cuenta.
     */
    private String email;

    /**
     * Estado de habilitación del usuario.
     * Controla si el usuario puede acceder al sistema (true) o está deshabilitado (false).
     * Valor por defecto: true
     */
    private boolean enabled = true;

    /**
     * Fecha y hora de registro del usuario en el sistema.
     * Se establece automáticamente al crear el usuario.
     */
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    /**
     * Conjunto de roles asignados al usuario.
     * Relación Many-to-Many con carga EAGER para acceso inmediato a los permisos.
     * Utiliza tabla intermedia 'usuarios_roles' para la relación.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuarios_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Usuario() {}

    /**
     * Constructor con parámetros principales para crear instancias de usuarios.
     *
     * @param username Nombre de usuario único
     * @param password Contraseña del usuario
     * @param nombre Nombre(s) del usuario
     * @param apellido Apellido(s) del usuario
     */
    public Usuario(String username, String password, String nombre, String apellido) {
        this.username = username;
        this.password = password;
        this.nombre = nombre;
        this.apellido = apellido;
    }

    // Getters y Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Set<Rol> getRoles() { return roles; }
    public void setRoles(Set<Rol> roles) { this.roles = roles; }

    /**
     * Método útil para obtener el nombre completo del usuario.
     * Combina nombre y apellido en un solo string.
     *
     * @return Nombre completo del usuario (nombre + apellido)
     */
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}