package com.beneficencia.almacen.repository;

import com.beneficencia.almacen.model.OrdenAbastecimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenAbastecimientoRepository extends JpaRepository<OrdenAbastecimiento, Long> {

    Optional<OrdenAbastecimiento> findByNumeroOA(String numeroOA);

    List<OrdenAbastecimiento> findByTipoOrden(OrdenAbastecimiento.TipoOrden tipoOrden);

    List<OrdenAbastecimiento> findByEstado(OrdenAbastecimiento.EstadoOrden estado);

    boolean existsByNumeroOA(String numeroOA);

    @Query("SELECT DISTINCT oa FROM OrdenAbastecimiento oa " +
            "LEFT JOIN FETCH oa.proveedor " +
            "LEFT JOIN FETCH oa.usuario " +
            "ORDER BY oa.fechaCreacion DESC, oa.id DESC")
    List<OrdenAbastecimiento> findAllWithProveedorAndUsuario();

    @Query("SELECT DISTINCT oa FROM OrdenAbastecimiento oa " +
            "LEFT JOIN FETCH oa.proveedor " +
            "LEFT JOIN FETCH oa.usuario " +
            "LEFT JOIN FETCH oa.items i " +
            "LEFT JOIN FETCH i.producto " +
            "WHERE oa.id = :id")
    Optional<OrdenAbastecimiento> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT COUNT(oa) FROM OrdenAbastecimiento oa WHERE oa.tipoOrden = :tipoOrden AND YEAR(oa.fechaOA) = :year")
    Long countByTipoOrdenAndFechaOAYear(@Param("tipoOrden") OrdenAbastecimiento.TipoOrden tipoOrden,
                                        @Param("year") int year);

    @Query("SELECT MAX(CAST(SUBSTRING(oa.numeroOA, LENGTH(:prefijo) + 6, 3) AS long)) " +
            "FROM OrdenAbastecimiento oa " +
            "WHERE oa.numeroOA LIKE CONCAT(:prefijo, '-', :year, '-%') " +
            "AND LENGTH(oa.numeroOA) = LENGTH(:prefijo) + 9")
    Long findMaxNumeroByPrefijoAndYear(@Param("prefijo") String prefijo,
                                       @Param("year") String year);

    @Query("SELECT oa FROM OrdenAbastecimiento oa WHERE oa.numeroOA LIKE CONCAT(:prefijo, '-', :year, '-%')")
    List<OrdenAbastecimiento> findByNumeroOAStartingWith(@Param("prefijo") String prefijo,
                                                         @Param("year") String year);

    @Query("SELECT oa FROM OrdenAbastecimiento oa " +
            "WHERE oa.fechaOA BETWEEN :fechaInicio AND :fechaFin " +
            "ORDER BY oa.fechaOA DESC")
    List<OrdenAbastecimiento> findByFechaOABetween(@Param("fechaInicio") LocalDateTime fechaInicio,
                                                   @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT oa FROM OrdenAbastecimiento oa " +
            "WHERE YEAR(oa.fechaOA) = YEAR(CURRENT_DATE) AND MONTH(oa.fechaOA) = MONTH(CURRENT_DATE) " +
            "ORDER BY oa.fechaOA DESC")
    List<OrdenAbastecimiento> findOrdenesDelMesActual();

    @Query("SELECT oa FROM OrdenAbastecimiento oa WHERE oa.estado = 'PENDIENTE' ORDER BY oa.fechaOA ASC")
    List<OrdenAbastecimiento> findOrdenesPendientes();

    @Query("SELECT oa.tipoOrden, COUNT(oa) FROM OrdenAbastecimiento oa GROUP BY oa.tipoOrden")
    List<Object[]> countOrdenesByTipo();

    @Query("SELECT oa.tipoOrden, SUM(oa.total) FROM OrdenAbastecimiento oa WHERE oa.total > 0 GROUP BY oa.tipoOrden")
    List<Object[]> sumTotalByTipo();

    @Query("SELECT p.nombre, COUNT(oa), SUM(oa.total) " +
            "FROM OrdenAbastecimiento oa JOIN oa.proveedor p " +
            "GROUP BY p.id, p.nombre " +
            "ORDER BY SUM(oa.total) DESC")
    List<Object[]> findEstadisticasPorProveedor();

    @Query("SELECT oa FROM OrdenAbastecimiento oa " +
            "LEFT JOIN FETCH oa.proveedor " +
            "ORDER BY oa.fechaCreacion DESC")
    List<OrdenAbastecimiento> findUltimasOrdenes();

    @Query("SELECT COUNT(oa) > 0 FROM OrdenAbastecimiento oa WHERE oa.numeroOA = :numeroOA AND oa.id != :excludeId")
    boolean existsByNumeroOAAndIdNot(@Param("numeroOA") String numeroOA,
                                     @Param("excludeId") Long excludeId);

    @Query("SELECT DISTINCT oa FROM OrdenAbastecimiento oa " +
            "JOIN oa.items i " +
            "WHERE i.producto.id = :productoId " +
            "ORDER BY oa.fechaOA DESC")
    List<OrdenAbastecimiento> findByProductoId(@Param("productoId") Long productoId);

    @Query("SELECT " +
            "COUNT(oa) as totalOrdenes, " +
            "SUM(CASE WHEN oa.estado = 'PENDIENTE' THEN 1 ELSE 0 END) as pendientes, " +
            "SUM(CASE WHEN oa.estado = 'APROBADA' THEN 1 ELSE 0 END) as aprobadas, " +
            "SUM(CASE WHEN oa.estado = 'COMPLETADA' THEN 1 ELSE 0 END) as completadas, " +
            "COALESCE(SUM(oa.total), 0) as totalMonto " +
            "FROM OrdenAbastecimiento oa " +
            "WHERE YEAR(oa.fechaOA) = YEAR(CURRENT_DATE)")
    Object[] findEstadisticasDashboard();
}