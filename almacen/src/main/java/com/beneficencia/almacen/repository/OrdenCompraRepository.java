package com.beneficencia.almacen.repository;

import com.beneficencia.almacen.model.OrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {

    Optional<OrdenCompra> findByNumeroOC(String numeroOC);

    List<OrdenCompra> findByTipoOrden(OrdenCompra.TipoOrden tipoOrden);

    List<OrdenCompra> findByEstado(OrdenCompra.EstadoOrden estado);

    // CONSULTA CORREGIDA - Ordenar por fecha DESC para que las más recientes aparezcan primero
    @Query("SELECT oc FROM OrdenCompra oc LEFT JOIN FETCH oc.proveedor LEFT JOIN FETCH oc.usuario ORDER BY oc.fechaCreacion DESC, oc.id DESC")
    List<OrdenCompra> findAllWithProveedorAndUsuario();

    // NUEVA CONSULTA para cargar orden con items
    @Query("SELECT oc FROM OrdenCompra oc " +
            "LEFT JOIN FETCH oc.proveedor " +
            "LEFT JOIN FETCH oc.usuario " +
            "LEFT JOIN FETCH oc.items i " +
            "LEFT JOIN FETCH i.producto " +
            "WHERE oc.id = :id")
    Optional<OrdenCompra> findByIdWithItems(Long id);

    // Verificar si un número ya existe
    boolean existsByNumeroOC(String numeroOC);

    // Contar órdenes por tipo y año
    @Query("SELECT COUNT(oc) FROM OrdenCompra oc WHERE oc.tipoOrden = :tipoOrden AND YEAR(oc.fechaOC) = :year")
    Long countByTipoOrdenAndFechaOCYear(@Param("tipoOrden") OrdenCompra.TipoOrden tipoOrden,
                                        @Param("year") int year);

    // NUEVO: Encontrar el máximo número secuencial por tipo y año
    @Query("SELECT MAX(CAST(SUBSTRING(oc.numeroOC, LENGTH(:prefijo) + 6, 3) AS long)) " +
            "FROM OrdenCompra oc " +
            "WHERE oc.numeroOC LIKE CONCAT(:prefijo, '-', :year, '-%')")
    Long findMaxNumeroByPrefijoAndYear(@Param("prefijo") String prefijo,
                                       @Param("year") String year);

    // NUEVO: Buscar órdenes por patrón de número
    @Query("SELECT oc FROM OrdenCompra oc WHERE oc.numeroOC LIKE CONCAT(:prefijo, '-', :year, '-%')")
    List<OrdenCompra> findByNumeroOCStartingWith(@Param("prefijo") String prefijo,
                                                 @Param("year") String year);
}