package com.beneficencia.almacen.repository;

import com.beneficencia.almacen.model.CuadreInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CuadreInventarioRepository extends JpaRepository<CuadreInventario, Long> {

    List<CuadreInventario> findByEstado(String estado);
    List<CuadreInventario> findByProductoId(Long productoId);
    List<CuadreInventario> findByAccion(String accion);
    List<CuadreInventario> findAllByOrderByFechaRegistroDesc();
}