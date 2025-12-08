package com.beneficencia.almacen.repository;

import com.beneficencia.almacen.model.OrdenSalidaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenSalidaItemRepository extends JpaRepository<OrdenSalidaItem, Long> {

    List<OrdenSalidaItem> findByOrdenSalidaId(Long ordenSalidaId);

    List<OrdenSalidaItem> findByProductoId(Long productoId);

    @Query("SELECT oi FROM OrdenSalidaItem oi WHERE oi.ordenSalida.numeroOrden = :numeroOrden")
    List<OrdenSalidaItem> findByNumeroOrden(@Param("numeroOrden") String numeroOrden);

    @Query("SELECT oi FROM OrdenSalidaItem oi JOIN FETCH oi.producto WHERE oi.ordenSalida.id = :ordenId")
    List<OrdenSalidaItem> findItemsConProductosPorOrdenId(@Param("ordenId") Long ordenId);
}