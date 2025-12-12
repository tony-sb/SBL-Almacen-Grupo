package com.beneficencia.almacen.repository;

import com.beneficencia.almacen.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByCodigoStartingWithOrderByIdDesc(String prefijo);

    boolean existsByCodigoIgnoreCase(String codigo);

    List<Producto> findByCategoria(String categoria);

    @Query("SELECT p FROM Producto p WHERE p.cantidad <= p.stockMinimo")
    List<Producto> findProductosConStockBajo();

    boolean existsByCodigo(String codigo);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.cantidad <= p.stockMinimo")
    Long countByCantidadLessThanEqualStockMinimo();

    @Query("SELECT p FROM Producto p WHERE p.id NOT IN :ids")
    List<Producto> findByIdNotIn(List<Long> ids);

    Optional<Producto> findByCodigo(String codigo);

    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Producto> findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(@Param("termino") String termino, String busqueda);
}