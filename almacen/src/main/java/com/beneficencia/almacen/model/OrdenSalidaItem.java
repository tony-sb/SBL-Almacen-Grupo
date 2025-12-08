package com.beneficencia.almacen.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orden_salida_items")
public class OrdenSalidaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_salida_id", nullable = false)
    private OrdenSalida ordenSalida;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    // Constructor
    public OrdenSalidaItem() {
        this.fechaRegistro = LocalDateTime.now();
    }

    public OrdenSalidaItem(OrdenSalida ordenSalida, Producto producto, Integer cantidad, BigDecimal precioUnitario) {
        this();
        this.ordenSalida = ordenSalida;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
        calcularSubtotal();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public OrdenSalida getOrdenSalida() { return ordenSalida; }
    public void setOrdenSalida(OrdenSalida ordenSalida) { this.ordenSalida = ordenSalida; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
        calcularSubtotal();
    }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
        calcularSubtotal();
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    // Métodos auxiliares
    private void calcularSubtotal() {
        if (this.cantidad != null && this.precioUnitario != null) {
            this.subtotal = this.precioUnitario.multiply(BigDecimal.valueOf(this.cantidad));
        }
    }

    @Override
    public String toString() {
        return "OrdenSalidaItem{" +
                "id=" + id +
                ", producto=" + (producto != null ? producto.getNombre() : "null") +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", subtotal=" + subtotal +
                '}';
    }
}