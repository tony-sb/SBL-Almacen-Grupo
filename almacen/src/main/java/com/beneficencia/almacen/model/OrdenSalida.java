package com.beneficencia.almacen.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa una orden de salida de productos del almacén.
 * Registra las transacciones de salida de productos a beneficiarios,
 * incluyendo información del trámite, beneficiario y productos entregados.
 */
@Entity
@Table(name = "ordenes_salida")
public class OrdenSalida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Número único identificador de la orden de salida.
     * Campo único y obligatorio para identificación y seguimiento de la orden.
     */
    @Column(name = "numero_orden", unique = true, nullable = false)
    private String numeroOrden;

    /**
     * Fecha en que se realiza la salida física de los productos.
     * Campo obligatorio que indica cuándo se entregaron los productos.
     */
    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;

    /**
     * DNI del usuario o beneficiario que recibe los productos.
     * Campo obligatorio para identificación del receptor.
     */
    @Column(name = "dni_usuario", nullable = false)
    private String dniUsuario;

    /**
     * Nombre completo del usuario o beneficiario que recibe los productos.
     * Campo obligatorio para registro del receptor.
     */
    @Column(name = "nombre_usuario", nullable = false)
    private String nombreUsuario;

    /**
     * Número de trámite asociado a la salida de productos.
     * Campo obligatorio que referencia el procedimiento administrativo relacionado.
     */
    @Column(name = "numero_tramite", nullable = false)
    private String numeroTramite;

    /**
     * Cantidad total de productos entregados en esta orden.
     * Campo obligatorio que representa el volumen de la entrega.
     */
    @Column(name = "cantidad_productos", nullable = false)
    private Integer cantidadProductos;

    /**
     * Descripción adicional o motivo de la salida de productos.
     * Información complementaria sobre el propósito de la entrega.
     */
    private String descripcion;

    /**
     * Fecha y hora en que se registró la orden en el sistema.
     * Se establece automáticamente al crear el registro.
     */
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    /**
     * Constructor por defecto.
     * Inicializa automáticamente la fecha de registro y establece
     * la cantidad de productos en 0 por defecto.
     */
    public OrdenSalida() {
        this.fechaRegistro = LocalDateTime.now();
        this.cantidadProductos = 0;
    }

    // Getters y Setters

    /**
     * Obtiene el ID único de la orden de salida.
     *
     * @return ID de la orden
     */
    public Long getId() { return id; }

    /**
     * Establece el ID único de la orden de salida.
     *
     * @param id ID de la orden
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Obtiene el número único identificador de la orden.
     *
     * @return Número de orden
     */
    public String getNumeroOrden() { return numeroOrden; }

    /**
     * Establece el número único identificador de la orden.
     *
     * @param numeroOrden Número de orden
     */
    public void setNumeroOrden(String numeroOrden) { this.numeroOrden = numeroOrden; }

    /**
     * Obtiene la fecha de salida de los productos.
     *
     * @return Fecha de salida
     */
    public LocalDate getFechaSalida() { return fechaSalida; }

    /**
     * Establece la fecha de salida de los productos.
     *
     * @param fechaSalida Fecha de salida
     */
    public void setFechaSalida(LocalDate fechaSalida) { this.fechaSalida = fechaSalida; }

    /**
     * Obtiene el DNI del usuario beneficiario.
     *
     * @return DNI del usuario
     */
    public String getDniUsuario() { return dniUsuario; }

    /**
     * Establece el DNI del usuario beneficiario.
     *
     * @param dniUsuario DNI del usuario
     */
    public void setDniUsuario(String dniUsuario) { this.dniUsuario = dniUsuario; }

    /**
     * Obtiene el nombre del usuario beneficiario.
     *
     * @return Nombre del usuario
     */
    public String getNombreUsuario() { return nombreUsuario; }

    /**
     * Establece el nombre del usuario beneficiario.
     *
     * @param nombreUsuario Nombre del usuario
     */
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

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
     * Obtiene la cantidad total de productos entregados.
     *
     * @return Cantidad de productos
     */
    public Integer getCantidadProductos() { return cantidadProductos; }

    /**
     * Establece la cantidad total de productos entregados.
     *
     * @param cantidadProductos Cantidad de productos
     */
    public void setCantidadProductos(Integer cantidadProductos) { this.cantidadProductos = cantidadProductos; }

    /**
     * Obtiene la descripción de la orden.
     *
     * @return Descripción de la orden
     */
    public String getDescripcion() { return descripcion; }

    /**
     * Establece la descripción de la orden.
     *
     * @param descripcion Descripción de la orden
     */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

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