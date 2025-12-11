package com.beneficencia.almacen.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "orden_abastecimiento_items")
public class OrdenAbastecimientoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_abastecimiento_id", nullable = false)
    private OrdenAbastecimiento ordenAbastecimiento;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;

    public void desconectarDeOrden() {
        if (this.ordenAbastecimiento != null) {
            this.ordenAbastecimiento = null;
        }
    }

    public void conectarAOrden(OrdenAbastecimiento orden) {
        this.ordenAbastecimiento = orden;
        if (orden != null && !orden.getItems().contains(this)) {
            orden.getItems().add(this);
        }
    }

    public OrdenAbastecimientoItem() {}

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public OrdenAbastecimiento getOrdenAbastecimiento() { return ordenAbastecimiento; }

    public void setOrdenAbastecimiento(OrdenAbastecimiento ordenAbastecimiento) { this.ordenAbastecimiento = ordenAbastecimiento; }

    public Producto getProducto() { return producto; }

    public void setProducto(Producto producto) { this.producto = producto; }

    public Integer getCantidad() { return cantidad; }

    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }

    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getSubtotal() { return subtotal; }

    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}