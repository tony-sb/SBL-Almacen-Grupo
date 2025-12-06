package com.beneficencia.almacen.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entidad que representa los items individuales de una orden de abastecimiento.
 * Cada instancia de esta clase corresponde a un producto específico incluido
 * en una orden de abastecimiento, con su cantidad, precio y subtotal calculado.
 */
@Entity
@Table(name = "orden_abastecimiento_items")
public class OrdenAbastecimientoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Orden de abastecimiento a la que pertenece este item.
     * Relación Many-to-One con carga LAZY para optimización de rendimiento.
     * Campo obligatorio que vincula el item con su orden padre.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_abastecimiento_id", nullable = false)
    private OrdenAbastecimiento ordenAbastecimiento;

    /**
     * Producto asociado a este item de la orden.
     * Relación Many-to-One con carga EAGER para acceso inmediato a la información del producto.
     * Campo obligatorio que especifica qué producto se está solicitando.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    /**
     * Cantidad solicitada del producto en esta orden.
     * Representa el número de unidades del producto que se están solicitando.
     */
    private Integer cantidad;

    /**
     * Precio unitario del producto al momento de crear la orden.
     * Precisión de 10 dígitos con 2 decimales para valores monetarios.
     */
    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    /**
     * Subtotal calculado para este item (cantidad × precio unitario).
     * Precisión de 10 dígitos con 2 decimales para valores monetarios.
     * Se calcula automáticamente basado en cantidad y precio unitario.
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;

    /**
     * Método para romper la relación con la orden padre.
     * Importante antes de eliminar items.
     */
    public void desconectarDeOrden() {
        if (this.ordenAbastecimiento != null) {
            this.ordenAbastecimiento = null;
        }
    }

    /**
     * Método para conectar con una orden padre.
     */
    public void conectarAOrden(OrdenAbastecimiento orden) {
        this.ordenAbastecimiento = orden;
        if (orden != null && !orden.getItems().contains(this)) {
            orden.getItems().add(this);
        }
    }

    /**
     * Constructor por defecto requerido por JPA.
     */
    public OrdenAbastecimientoItem() {}

    // Getters y Setters

    /**
     * Obtiene el ID único del item de la orden.
     *
     * @return ID del item
     */
    public Long getId() { return id; }

    /**
     * Establece el ID único del item de la orden.
     *
     * @param id ID del item
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Obtiene la orden de abastecimiento a la que pertenece este item.
     *
     * @return Orden de abastecimiento padre
     */
    public OrdenAbastecimiento getOrdenAbastecimiento() { return ordenAbastecimiento; }

    /**
     * Establece la orden de abastecimiento a la que pertenece este item.
     *
     * @param ordenAbastecimiento Orden de abastecimiento padre
     */
    public void setOrdenAbastecimiento(OrdenAbastecimiento ordenAbastecimiento) { this.ordenAbastecimiento = ordenAbastecimiento; }

    /**
     * Obtiene el producto asociado a este item.
     *
     * @return Producto del item
     */
    public Producto getProducto() { return producto; }

    /**
     * Establece el producto asociado a este item.
     *
     * @param producto Producto del item
     */
    public void setProducto(Producto producto) { this.producto = producto; }

    /**
     * Obtiene la cantidad solicitada del producto.
     *
     * @return Cantidad solicitada
     */
    public Integer getCantidad() { return cantidad; }

    /**
     * Establece la cantidad solicitada del producto.
     *
     * @param cantidad Cantidad solicitada
     */
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    /**
     * Obtiene el precio unitario del producto.
     *
     * @return Precio unitario
     */
    public BigDecimal getPrecioUnitario() { return precioUnitario; }

    /**
     * Establece el precio unitario del producto.
     *
     * @param precioUnitario Precio unitario
     */
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    /**
     * Obtiene el subtotal calculado para este item.
     *
     * @return Subtotal (cantidad × precio unitario)
     */
    public BigDecimal getSubtotal() { return subtotal; }

    /**
     * Establece el subtotal calculado para este item.
     *
     * @param subtotal Subtotal (cantidad × precio unitario)
     */
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}