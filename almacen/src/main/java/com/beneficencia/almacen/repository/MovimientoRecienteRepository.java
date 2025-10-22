package com.beneficencia.almacen.repository;

import com.beneficencia.almacen.model.MovimientoReciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoRecienteRepository extends JpaRepository<MovimientoReciente, Long> {

    // Obtener movimientos recientes ordenados por fecha descendente
    List<MovimientoReciente> findAllByOrderByFechaSalidaDesc();

    // Obtener movimientos con información de productos cargada
    @Query("SELECT mr FROM MovimientoReciente mr JOIN FETCH mr.producto ORDER BY mr.fechaSalida DESC")
    List<MovimientoReciente> findMovimientosRecientesConProductos();

    // Buscar movimientos por producto
    List<MovimientoReciente> findByProductoId(Long productoId);

    // Buscar movimientos por DNI beneficiario
    List<MovimientoReciente> findByDniBeneficiarioContaining(String dni);
}