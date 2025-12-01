package com.beneficencia.almacen.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entidad que representa los movimientos recientes de salida de productos del almacén.
 * Registra las transacciones de salida de productos para seguimiento y reportes.
 */
@Entity
@Table(name = "movimientos_recientes")
public class MovimientoReciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Producto asociado al movimiento de salida.
     * Relación Many-to-One con carga EAGER para acceso inmediato a la información del producto.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    /**
     * Fecha en que se realizó el movimiento de salida del producto.
     */
    @Column(name = "fecha_salida")
    private LocalDate fechaSalida;

    /**
     * Cantidad de productos que fueron retirados en este movimiento.
     */
    private Integer cantidad;

    /**
     * DNI del beneficiario que recibió los productos.
     * Identifica a la persona que se benefició con la salida de productos.
     */
    @Column(name = "dni_beneficiario")
    private String dniBeneficiario;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public MovimientoReciente() {}

    /**
     * Constructor con parámetros para crear instancias de movimientos recientes.
     *
     * @param producto Producto asociado al movimiento
     * @param fechaSalida Fecha en que se realizó la salida
     * @param cantidad Cantidad de productos retirados
     * @param dniBeneficiario DNI del beneficiario que recibió los productos
     */
    public MovimientoReciente(Producto producto, LocalDate fechaSalida, Integer cantidad, String dniBeneficiario) {
        this.producto = producto;
        this.fechaSalida = fechaSalida;
        this.cantidad = cantidad;
        this.dniBeneficiario = dniBeneficiario;
    }

    // Getters y Setters

    /**
     * Obtiene el ID único del movimiento.
     *
     * @return ID del movimiento
     */
    public Long getId() {
        return id;
    }

    /**
     * Establece el ID único del movimiento.
     *
     * @param id ID del movimiento
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtiene el producto asociado al movimiento.
     *
     * @return Producto del movimiento
     */
    public Producto getProducto() {
        return producto;
    }

    /**
     * Establece el producto asociado al movimiento.
     *
     * @param producto Producto del movimiento
     */
    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    /**
     * Obtiene la fecha de salida del producto.
     *
     * @return Fecha de salida
     */
    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    /**
     * Establece la fecha de salida del producto.
     *
     * @param fechaSalida Fecha de salida
     */
    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    /**
     * Obtiene la cantidad de productos retirados.
     *
     * @return Cantidad retirada
     */
    public Integer getCantidad() {
        return cantidad;
    }

    /**
     * Establece la cantidad de productos retirados.
     *
     * @param cantidad Cantidad retirada
     */
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    /**
     * Obtiene el DNI del beneficiario.
     *
     * @return DNI del beneficiario
     */
    public String getDniBeneficiario() {
        return dniBeneficiario;
    }

    /**
     * Establece el DNI del beneficiario.
     *
     * @param dniBeneficiario DNI del beneficiario
     */
    public void setDniBeneficiario(String dniBeneficiario) {
        this.dniBeneficiario = dniBeneficiario;
    }

    /**
     * Representación en String del movimiento reciente.
     * Incluye información básica para debugging y logging.
     *
     * @return String con la representación del movimiento
     */
    @Override
    public String toString() {
        return "MovimientoReciente{" +
                "id=" + id +
                ", producto=" + (producto != null ? producto.getNombre() : "null") +
                ", fechaSalida=" + fechaSalida +
                ", cantidad=" + cantidad +
                ", dniBeneficiario='" + dniBeneficiario + '\'' +
                '}';
    }
}