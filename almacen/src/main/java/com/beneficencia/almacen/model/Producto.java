package com.beneficencia.almacen.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un producto en el inventario del almacén.
 * Define la estructura de datos para los productos gestionados en el sistema,
 * incluyendo información de stock, categorías, precios y unidades de medida.
 */
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Código único identificador del producto.
     * Campo obligatorio para identificación y búsqueda del producto.
     */
    @Column(nullable = false)
    private String codigo;

    /**
     * Nombre descriptivo del producto.
     * Campo obligatorio para identificación visual del producto.
     */
    @Column(nullable = false)
    private String nombre;

    /**
     * Descripción detallada del producto.
     * Información adicional sobre características o uso del producto.
     */
    private String descripcion;

    /**
     * Cantidad actual disponible en inventario.
     * Campo obligatorio que representa el stock actual del producto.
     */
    @Column(nullable = false)
    private Integer cantidad;

    /**
     * Unidad de medida del producto (Unidad, Caja, Paquete, Litro, Kilogramo, etc.).
     * Campo obligatorio para estandarizar las mediciones del producto.
     */
    @Column(nullable = false)
    private String unidadMedida;

    /**
     * Stock mínimo requerido para generar alertas de reabastecimiento.
     * Campo obligatorio para control de inventario y alertas de stock bajo.
     */
    @Column(nullable = false)
    private Integer stockMinimo;

    /**
     * Categoría del producto para agrupación y filtrado.
     * Campo obligatorio para organización del inventario (Medicamentos, Insumos Médicos, etc.).
     */
    @Column(nullable = false)
    private String categoria;

    /**
     * Precio unitario del producto.
     * Precisión de 10 dígitos con 2 decimales para valores monetarios.
     * Valor por defecto: BigDecimal.ZERO
     */
    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario = BigDecimal.ZERO;

    /**
     * Fecha y hora de registro del producto en el sistema.
     * Se establece automáticamente al crear el producto.
     */
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Producto() {}

    /**
     * Constructor con parámetros principales para crear instancias de productos.
     *
     * @param codigo Código único del producto
     * @param nombre Nombre del producto
     * @param cantidad Cantidad inicial en inventario
     * @param unidadMedida Unidad de medida del producto
     * @param stockMinimo Stock mínimo para alertas
     * @param categoria Categoría del producto
     * @param precioUnitario Precio unitario del producto
     */
    public Producto(String codigo, String nombre, Integer cantidad, String unidadMedida,
                    Integer stockMinimo, String categoria, BigDecimal precioUnitario) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.unidadMedida = unidadMedida;
        this.stockMinimo = stockMinimo;
        this.categoria = categoria;
        this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
    }

    // Getters y Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
    }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    /**
     * Método toString para debugging y logging.
     * Proporciona una representación legible de la información básica del producto.
     *
     * @return String con la representación del producto
     */
    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", categoria='" + categoria + '\'' +
                '}';
    }
}