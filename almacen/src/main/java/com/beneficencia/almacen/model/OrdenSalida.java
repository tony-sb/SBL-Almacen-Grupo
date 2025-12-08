package com.beneficencia.almacen.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
     * Número adicional de orden de salida (nueva columna).
     */
    @Column(name = "numero_orden_salida", unique = true, nullable = false)
    private String numeroOrdenSalida;

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
    @Column(name = "dni_usuario", nullable = false, length = 8)
    private String dniUsuario;

    /**
     * Nombre completo del usuario o beneficiario que recibe los productos.
     * Campo obligatorio para registro del receptor.
     */
    @Column(name = "nombre_usuario", nullable = false)
    private String nombreUsuario;

    /**
     * Relación con beneficiario para integridad referencial.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiario_id")
    private Beneficiario beneficiario;

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
     * Usuario del sistema que registra la orden.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Estado de la orden.
     */
    @Column(nullable = false)
    private String estado = "COMPLETADA";

    /**
     * Observaciones adicionales.
     */
    private String observaciones;

    /**
     * Fecha y hora en que se registró la orden en el sistema.
     * Se establece automáticamente al crear el registro.
     */
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /**
     * Items de la orden de salida.
     */
    @OneToMany(mappedBy = "ordenSalida", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrdenSalidaItem> items = new ArrayList<>();

    /**
     * Constructor por defecto.
     * Inicializa automáticamente la fecha de registro y establece
     * la cantidad de productos en 0 por defecto.
     */
    public OrdenSalida() {
        this.fechaRegistro = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.cantidadProductos = 0;
        this.estado = "COMPLETADA";
        this.numeroOrdenSalida = "OS-" + System.currentTimeMillis();
    }

    // Getters y Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroOrden() { return numeroOrden; }
    public void setNumeroOrden(String numeroOrden) { this.numeroOrden = numeroOrden; }

    public String getNumeroOrdenSalida() { return numeroOrdenSalida; }
    public void setNumeroOrdenSalida(String numeroOrdenSalida) { this.numeroOrdenSalida = numeroOrdenSalida; }

    public LocalDate getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDate fechaSalida) { this.fechaSalida = fechaSalida; }

    public String getDniUsuario() { return dniUsuario; }
    public void setDniUsuario(String dniUsuario) { this.dniUsuario = dniUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public Beneficiario getBeneficiario() { return beneficiario; }
    public void setBeneficiario(Beneficiario beneficiario) { this.beneficiario = beneficiario; }

    public String getNumeroTramite() { return numeroTramite; }
    public void setNumeroTramite(String numeroTramite) { this.numeroTramite = numeroTramite; }

    public Integer getCantidadProductos() { return cantidadProductos; }
    public void setCantidadProductos(Integer cantidadProductos) { this.cantidadProductos = cantidadProductos; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public List<OrdenSalidaItem> getItems() { return items; }
    public void setItems(List<OrdenSalidaItem> items) { this.items = items; }

    /**
     * Método para agregar un item a la orden.
     */
    public void agregarItem(OrdenSalidaItem item) {
        items.add(item);
        item.setOrdenSalida(this);
        actualizarCantidadTotal();
    }

    /**
     * Método para actualizar la cantidad total de productos.
     */
    private void actualizarCantidadTotal() {
        this.cantidadProductos = items.stream()
                .mapToInt(OrdenSalidaItem::getCantidad)
                .sum();
    }

    /**
     * Método para calcular el total de la orden.
     */
    public Double getTotalOrden() {
        return items.stream()
                .mapToDouble(item -> item.getSubtotal() != null ? item.getSubtotal().doubleValue() : 0.0)
                .sum();
    }
}