package com.beneficencia.almacen.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa un producto en el inventario del almacén
 *
 * @author Equipo de Desarrollo
 */
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private String unidadMedida;

    @Column(nullable = false)
    private Integer stockMinimo;

    @Column(nullable = false)
    private String categoria;

    private LocalDateTime fechaRegistro = LocalDateTime.now();

    /**
     * Constructor por defecto
     */
    public Producto() {}

    /**
     * Constructor con parámetros principales
     */
    public Producto(String codigo, String nombre, Integer cantidad, String unidadMedida,
                    Integer stockMinimo, String categoria) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.unidadMedida = unidadMedida;
        this.stockMinimo = stockMinimo;
        this.categoria = categoria;
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

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}