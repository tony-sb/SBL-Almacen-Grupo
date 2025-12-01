package com.beneficencia.almacen.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una orden de abastecimiento en el sistema de almacén.
 * Gestiona las solicitudes de compra y donación de productos para el inventario.
 * Reemplaza la anterior entidad OrdenCompra para un manejo más amplio de tipos de abastecimiento.
 */
@Entity
@Table(name = "ordenes_abastecimiento")
public class OrdenAbastecimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Número único identificador de la orden de abastecimiento.
     * Campo único y obligatorio para identificación de la orden.
     */
    @Column(name = "numero_oa", unique = true, nullable = false)
    private String numeroOA;

    /**
     * Fecha en que se genera la orden de abastecimiento.
     * Campo obligatorio que indica la fecha de creación de la orden.
     */
    @Column(name = "fecha_oa", nullable = false)
    private LocalDate fechaOA;

    /**
     * Proveedor asociado a la orden de abastecimiento.
     * Relación Many-to-One con carga EAGER para acceso inmediato a la información del proveedor.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    /**
     * Usuario que crea la orden de abastecimiento.
     * Relación Many-to-One con carga EAGER para acceso inmediato a la información del usuario.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Tipo de orden de abastecimiento.
     * Define la naturaleza y propósito de la orden mediante enumeración.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_orden", nullable = false)
    private TipoOrden tipoOrden;

    /**
     * Estado actual de la orden de abastecimiento.
     * Controla el flujo de trabajo de la orden mediante enumeración.
     */
    @Enumerated(EnumType.STRING)
    private EstadoOrden estado;

    /**
     * Monto total calculado de la orden de abastecimiento.
     * Suma de los subtotales de todos los items incluidos en la orden.
     */
    private BigDecimal total;

    /**
     * Observaciones o comentarios adicionales sobre la orden.
     * Información complementaria para el procesamiento de la orden.
     */
    private String observaciones;

    /**
     * Fecha y hora de creación del registro en el sistema.
     * Se establece automáticamente al crear la orden.
     */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de la última actualización del registro.
     * Se actualiza automáticamente al modificar la orden.
     */
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /**
     * Lista de items incluidos en la orden de abastecimiento.
     * Relación One-to-Many con carga LAZY para optimización.
     * Cascade ALL permite operaciones en cascada sobre los items.
     */
    @OneToMany(mappedBy = "ordenAbastecimiento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrdenAbastecimientoItem> items = new ArrayList<>();

    /**
     * Enumeración que define los tipos de órdenes de abastecimiento disponibles.
     */
    public enum TipoOrden {
        SOLIDAS, DONACIONES, U_OFICINA, INVENTARIO, REPORTE, R_DONACION, R_UTILES, R_TOTAL
    }

    /**
     * Enumeración que define los estados del flujo de trabajo de la orden.
     */
    public enum EstadoOrden {
        PENDIENTE, APROBADA, RECHAZADA, COMPLETADA
    }

    /**
     * Constructor por defecto.
     * Inicializa automáticamente las fechas, estado y total de la orden.
     */
    public OrdenAbastecimiento() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.estado = EstadoOrden.PENDIENTE;
        this.total = BigDecimal.ZERO;
    }

    // Getters y Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroOA() { return numeroOA; }
    public void setNumeroOA(String numeroOA) { this.numeroOA = numeroOA; }

    public LocalDate getFechaOA() { return fechaOA; }
    public void setFechaOA(LocalDate fechaOA) { this.fechaOA = fechaOA; }

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

    public List<OrdenAbastecimientoItem> getItems() { return items; }
    public void setItems(List<OrdenAbastecimientoItem> items) { this.items = items; }
}