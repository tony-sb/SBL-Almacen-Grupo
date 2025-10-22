package com.beneficencia.almacen.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordenes_compra")
public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_oc", unique = true, nullable = false)
    private String numeroOC;

    @Column(name = "fecha_oc", nullable = false)
    private LocalDate fechaOC;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_orden", nullable = false)
    private TipoOrden tipoOrden;

    @Enumerated(EnumType.STRING)
    private EstadoOrden estado;

    private BigDecimal total;
    private String observaciones;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrdenCompraItem> items = new ArrayList<>();

    // Enums
    public enum TipoOrden {
        SOLIDAS, DONACIONES, U_OFICINA, INVENTARIO, REPORTE, R_DONACION, R_UTILES, R_TOTAL
    }

    public enum EstadoOrden {
        PENDIENTE, APROBADA, RECHAZADA, COMPLETADA
    }

    // Constructores
    public OrdenCompra() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.estado = EstadoOrden.PENDIENTE;
        this.total = BigDecimal.ZERO;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroOC() { return numeroOC; }
    public void setNumeroOC(String numeroOC) { this.numeroOC = numeroOC; }

    public LocalDate getFechaOC() { return fechaOC; }
    public void setFechaOC(LocalDate fechaOC) { this.fechaOC = fechaOC; }

    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public TipoOrden getTipoOrden() { return tipoOrden; }
    public void setTipoOrden(TipoOrden tipoOrden) { this.tipoOrden = tipoOrden; }

    public EstadoOrden getEstado() { return estado; }
    public void setEstado(EstadoOrden estado) { this.estado = estado; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public List<OrdenCompraItem> getItems() { return items; }
    public void setItems(List<OrdenCompraItem> items) { this.items = items; }
}