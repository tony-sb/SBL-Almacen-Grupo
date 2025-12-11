package com.beneficencia.almacen.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "movimientos_recientes")
public class MovimientoReciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(name = "fecha_salida")
    private LocalDate fechaSalida;

    private Integer cantidad;

    @Column(name = "dni_beneficiario")
    private String dniBeneficiario;

    public MovimientoReciente() {}

    public MovimientoReciente(Producto producto, LocalDate fechaSalida, Integer cantidad, String dniBeneficiario) {
        this.producto = producto;
        this.fechaSalida = fechaSalida;
        this.cantidad = cantidad;
        this.dniBeneficiario = dniBeneficiario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getDniBeneficiario() {
        return dniBeneficiario;
    }

    public void setDniBeneficiario(String dniBeneficiario) {
        this.dniBeneficiario = dniBeneficiario;
    }

    @Override
    public String toString() {
        return "MovimientoReciente{" +
                "id=" + id +
                ", producto=" + (producto != null ? producto.getNombre() : "null") +
                ", fechaSalida=" + fechaSalida +
                ", cantidad=" + cantidad +
                ", dniBeneficiario='" + dniBeneficiario + '\'' +
                '}';
    }
}