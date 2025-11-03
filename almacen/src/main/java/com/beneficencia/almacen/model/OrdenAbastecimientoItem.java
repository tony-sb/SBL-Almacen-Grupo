package com.beneficencia.almacen.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "orden_abastecimiento_items") // CAMBIADO: orden_compra_items → orden_abastecimiento_items
public class OrdenAbastecimientoItem { // CAMBIADO: OrdenCompraItem → OrdenAbastecimientoItem

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_abastecimiento_id", nullable = false) // CAMBIADO
    private OrdenAbastecimiento ordenAbastecimiento; // CAMBIADO

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;

    // Constructores
    public OrdenAbastecimientoItem() {} // CAMBIADO

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public OrdenAbastecimiento getOrdenAbastecimiento() { return ordenAbastecimiento; } // CAMBIADO
    public void setOrdenAbastecimiento(OrdenAbastecimiento ordenAbastecimiento) { this.ordenAbastecimiento = ordenAbastecimiento; } // CAMBIADO

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}