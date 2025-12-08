package com.beneficencia.almacen.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "beneficiarios")
public class Beneficiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener 8 dígitos numéricos")
    @Column(unique = true, nullable = false, length = 8)
    private String dni;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(min = 2, max = 100, message = "Los nombres deben tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String apellidos;

    @Pattern(regexp = "^(|\\d{9}|0\\d{2}-\\d{6})$",
            message = "Formato no válido.")
    @Column(length = 15)
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(min = 5, max = 500, message = "La dirección debe tener entre 5 y 100 caracteres")
    @Column(length = 500)
    private String direccion;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Constructor
    public Beneficiario() {
        this.fechaRegistro = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    // Getters y Setters (mantener los existentes)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    // Método para nombre completo
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}