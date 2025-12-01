package com.beneficencia.almacen.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa un proveedor en el sistema de almacén.
 * Gestiona la información de las empresas o personas que suministran productos
 * al almacén, incluyendo datos de contacto e identificación fiscal.
 */
@Entity
@Table(name = "proveedores")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Número de RUC único del proveedor.
     * Campo único y obligatorio para identificación fiscal del proveedor.
     * Se valida como único en el sistema para evitar duplicados.
     */
    @Column(unique = true, nullable = false)
    private String ruc;

    /**
     * Nombre o razón social del proveedor.
     * Campo obligatorio para identificación del proveedor.
     */
    @Column(nullable = false)
    private String nombre;

    /**
     * Dirección física del proveedor.
     * Información de ubicación para contacto y envíos.
     */
    private String direccion;

    /**
     * Número de teléfono de contacto del proveedor.
     * Medio de comunicación principal con el proveedor.
     */
    private String telefono;

    /**
     * Correo electrónico de contacto del proveedor.
     * Medio de comunicación digital con el proveedor.
     */
    private String email;

    /**
     * Fecha y hora de registro del proveedor en el sistema.
     * Se establece automáticamente al crear el proveedor.
     */
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    /**
     * Constructor por defecto.
     * Inicializa automáticamente la fecha de registro con la fecha/hora actual.
     */
    public Proveedor() {
        this.fechaRegistro = LocalDateTime.now();
    }

    // Getters y Setters

    /**
     * Obtiene el ID único del proveedor.
     *
     * @return ID del proveedor
     */
    public Long getId() { return id; }

    /**
     * Establece el ID único del proveedor.
     *
     * @param id ID del proveedor
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Obtiene el RUC del proveedor.
     *
     * @return RUC del proveedor
     */
    public String getRuc() { return ruc; }

    /**
     * Establece el RUC del proveedor.
     *
     * @param ruc RUC del proveedor
     */
    public void setRuc(String ruc) { this.ruc = ruc; }

    /**
     * Obtiene el nombre o razón social del proveedor.
     *
     * @return Nombre del proveedor
     */
    public String getNombre() { return nombre; }

    /**
     * Establece el nombre o razón social del proveedor.
     *
     * @param nombre Nombre del proveedor
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Obtiene la dirección del proveedor.
     *
     * @return Dirección del proveedor
     */
    public String getDireccion() { return direccion; }

    /**
     * Establece la dirección del proveedor.
     *
     * @param direccion Dirección del proveedor
     */
    public void setDireccion(String direccion) { this.direccion = direccion; }

    /**
     * Obtiene el teléfono del proveedor.
     *
     * @return Teléfono del proveedor
     */
    public String getTelefono() { return telefono; }

    /**
     * Establece el teléfono del proveedor.
     *
     * @param telefono Teléfono del proveedor
     */
    public void setTelefono(String telefono) { this.telefono = telefono; }

    /**
     * Obtiene el email del proveedor.
     *
     * @return Email del proveedor
     */
    public String getEmail() { return email; }

    /**
     * Establece el email del proveedor.
     *
     * @param email Email del proveedor
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Obtiene la fecha de registro del proveedor en el sistema.
     *
     * @return Fecha de registro
     */
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }

    /**
     * Establece la fecha de registro del proveedor en el sistema.
     *
     * @param fechaRegistro Fecha de registro
     */
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}