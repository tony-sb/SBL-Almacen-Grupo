package com.beneficencia.almacen.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_inventario")
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 10)
    private TipoMovimiento tipoMovimiento;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_salida_id")
    private OrdenSalida ordenSalida;

    @Column(name = "fecha_movimiento")
    private LocalDateTime fechaMovimiento;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    public enum TipoMovimiento {
        ENTRADA, SALIDA
    }

    public MovimientoInventario() {
        this.fechaMovimiento = LocalDateTime.now();
        this.tipoMovimiento = TipoMovimiento.SALIDA; // Valor por defecto
    }

    public MovimientoInventario(Producto producto, TipoMovimiento tipoMovimiento,
                                Integer cantidad, String motivo, Usuario usuario) {
        this();
        this.producto = producto;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.usuario = usuario;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public TipoMovimiento getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public OrdenSalida getOrdenSalida() { return ordenSalida; }
    public void setOrdenSalida(OrdenSalida ordenSalida) { this.ordenSalida = ordenSalida; }

    public LocalDateTime getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDateTime fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    @Override
    public String toString() {
        return "MovimientoInventario{" +
                "id=" + id +
                ", producto=" + (producto != null ? producto.getNombre() : "null") +
                ", tipoMovimiento=" + tipoMovimiento +
                ", cantidad=" + cantidad +
                ", motivo='" + motivo + '\'' +
                ", usuario=" + (usuario != null ? usuario.getUsername() : "null") +
                ", fechaMovimiento=" + fechaMovimiento +
                '}';
    }
}