package com.beneficencia.almacen.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa los movimientos de salida de productos del almacén.
 * Registra información detallada de cada transacción de salida incluyendo
 * beneficiarios, trámites asociados y usuario responsable.
 *
 * @author Equipo de Desarrollo
 */
@Entity
@Table(name = "movimientos_salida")
public class MovimientoSalida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Producto asociado al movimiento de salida.
     * Relación Many-to-One con producto, campo obligatorio.
     */
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    /**
     * Fecha en que se realizó la salida física del producto.
     * Campo obligatorio, por defecto fecha actual.
     */
    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;

    /**
     * Cantidad de productos retirados en este movimiento.
     * Campo obligatorio.
     */
    @Column(nullable = false)
    private Integer cantidad;

    /**
     * DNI del beneficiario que recibió los productos.
     * Identificación del receptor de los productos.
     */
    @Column(name = "dni_beneficiario")
    private String dniBeneficiario;

    /**
     * Nombre completo del beneficiario que recibió los productos.
     */
    @Column(name = "nombre_beneficiario")
    private String nombreBeneficiario;

    /**
     * Número de trámite asociado a la salida de productos.
     * Referencia a procedimientos administrativos relacionados.
     */
    @Column(name = "numero_tramite")
    private String numeroTramite;

    /**
     * Descripción adicional del movimiento o motivo de la salida.
     */
    private String descripcion;

    /**
     * Usuario del sistema que registró el movimiento.
     * Relación Many-to-One con usuario, campo obligatorio.
     */
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Fecha y hora en que se registró el movimiento en el sistema.
     * Se establece automáticamente al crear el registro.
     */
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    /**
     * Constructor por defecto.
     * Inicializa automáticamente la fecha de registro con la fecha/hora actual
     * y la fecha de salida con la fecha actual.
     */
    public MovimientoSalida() {
        this.fechaRegistro = LocalDateTime.now();
        this.fechaSalida = LocalDate.now();
    }

    // Getters y Setters

    /**
     * Obtiene el ID único del movimiento de salida.
     *
     * @return ID del movimiento
     */
    public Long getId() { return id; }

    /**
     * Establece el ID único del movimiento de salida.
     *
     * @param id ID del movimiento
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Obtiene el producto asociado al movimiento de salida.
     *
     * @return Producto del movimiento
     */
    public Producto getProducto() { return producto; }

    /**
     * Establece el producto asociado al movimiento de salida.
     *
     * @param producto Producto del movimiento
     */
    public void setProducto(Producto producto) { this.producto = producto; }

    /**
     * Obtiene la fecha de salida del producto.
     *
     * @return Fecha de salida
     */
    public LocalDate getFechaSalida() { return fechaSalida; }

    /**
     * Establece la fecha de salida del producto.
     *
     * @param fechaSalida Fecha de salida
     */
    public void setFechaSalida(LocalDate fechaSalida) { this.fechaSalida = fechaSalida; }

    /**
     * Obtiene la cantidad de productos retirados.
     *
     * @return Cantidad retirada
     */
    public Integer getCantidad() { return cantidad; }

    /**
     * Establece la cantidad de productos retirados.
     *
     * @param cantidad Cantidad retirada
     */
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    /**
     * Obtiene el DNI del beneficiario.
     *
     * @return DNI del beneficiario
     */
    public String getDniBeneficiario() { return dniBeneficiario; }

    /**
     * Establece el DNI del beneficiario.
     *
     * @param dniBeneficiario DNI del beneficiario
     */
    public void setDniBeneficiario(String dniBeneficiario) { this.dniBeneficiario = dniBeneficiario; }

    /**
     * Obtiene el nombre del beneficiario.
     *
     * @return Nombre del beneficiario
     */
    public String getNombreBeneficiario() { return nombreBeneficiario; }

    /**
     * Establece el nombre del beneficiario.
     *
     * @param nombreBeneficiario Nombre del beneficiario
     */
    public void setNombreBeneficiario(String nombreBeneficiario) { this.nombreBeneficiario = nombreBeneficiario; }

    /**
     * Obtiene el número de trámite asociado.
     *
     * @return Número de trámite
     */
    public String getNumeroTramite() { return numeroTramite; }

    /**
     * Establece el número de trámite asociado.
     *
     * @param numeroTramite Número de trámite
     */
    public void setNumeroTramite(String numeroTramite) { this.numeroTramite = numeroTramite; }

    /**
     * Obtiene la descripción del movimiento.
     *
     * @return Descripción del movimiento
     */
    public String getDescripcion() { return descripcion; }

    /**
     * Establece la descripción del movimiento.
     *
     * @param descripcion Descripción del movimiento
     */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /**
     * Obtiene el usuario que registró el movimiento.
     *
     * @return Usuario responsable
     */
    public Usuario getUsuario() { return usuario; }

    /**
     * Establece el usuario que registró el movimiento.
     *
     * @param usuario Usuario responsable
     */
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    /**
     * Obtiene la fecha y hora de registro en el sistema.
     *
     * @return Fecha y hora de registro
     */
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }

    /**
     * Establece la fecha y hora de registro en el sistema.
     *
     * @param fechaRegistro Fecha y hora de registro
     */
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}