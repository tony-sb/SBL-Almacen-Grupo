package com.beneficencia.almacen.repository;

import com.beneficencia.almacen.model.MovimientoReciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoRecienteRepository extends JpaRepository<MovimientoReciente, Long> {

    List<MovimientoReciente> findAllByOrderByFechaSalidaDesc();

    @Query("SELECT mr FROM MovimientoReciente mr JOIN FETCH mr.producto ORDER BY mr.fechaSalida DESC")
    List<MovimientoReciente> findMovimientosRecientesConProductos();

    List<MovimientoReciente> findByProductoId(Long productoId);

    List<MovimientoReciente> findByDniBeneficiarioContaining(String dni);
}