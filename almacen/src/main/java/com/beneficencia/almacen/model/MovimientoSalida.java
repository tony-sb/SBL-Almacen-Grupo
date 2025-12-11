package com.beneficencia.almacen.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_salida")
public class MovimientoSalida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "dni_beneficiario")
    private String dniBeneficiario;

    @Column(name = "nombre_beneficiario")
    private String nombreBeneficiario;

    @Column(name = "numero_tramite")
    private String numeroTramite;

    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    public MovimientoSalida() {
        this.fechaRegistro = LocalDateTime.now();
        this.fechaSalida = LocalDate.now();
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Producto getProducto() { return producto; }

    public void setProducto(Producto producto) { this.producto = producto; }

    public LocalDate getFechaSalida() { return fechaSalida; }

    public void setFechaSalida(LocalDate fechaSalida) { this.fechaSalida = fechaSalida; }

    public Integer getCantidad() { return cantidad; }

    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public String getDniBeneficiario() { return dniBeneficiario; }

    public void setDniBeneficiario(String dniBeneficiario) { this.dniBeneficiario = dniBeneficiario; }

    public String getNombreBeneficiario() { return nombreBeneficiario; }

    public void setNombreBeneficiario(String nombreBeneficiario) { this.nombreBeneficiario = nombreBeneficiario; }

    public String getNumeroTramite() { return numeroTramite; }

    public void setNumeroTramite(String numeroTramite) { this.numeroTramite = numeroTramite; }

    public String getDescripcion() { return descripcion; }

    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Usuario getUsuario() { return usuario; }

    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }

    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}