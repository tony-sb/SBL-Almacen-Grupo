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

    // ================= CONSULTAS BÁSICAS =================

    Optional<OrdenAbastecimiento> findByNumeroOA(String numeroOA); // CAMBIADO
    List<OrdenAbastecimiento> findByTipoOrden(OrdenAbastecimiento.TipoOrden tipoOrden); // CAMBIADO
    List<OrdenAbastecimiento> findByEstado(OrdenAbastecimiento.EstadoOrden estado); // CAMBIADO
    boolean existsByNumeroOA(String numeroOA); // CAMBIADO

    // ================= CONSULTAS CON FETCH JOIN =================

    @Query("SELECT DISTINCT oa FROM OrdenAbastecimiento oa " + // CAMBIADO
            "LEFT JOIN FETCH oa.proveedor " +
            "LEFT JOIN FETCH oa.usuario " +
            "ORDER BY oa.fechaCreacion DESC, oa.id DESC")
    List<OrdenAbastecimiento> findAllWithProveedorAndUsuario();

    @Query("SELECT DISTINCT oa FROM OrdenAbastecimiento oa " + // CAMBIADO
            "LEFT JOIN FETCH oa.proveedor " +
            "LEFT JOIN FETCH oa.usuario " +
            "LEFT JOIN FETCH oa.items i " +
            "LEFT JOIN FETCH i.producto " +
            "WHERE oa.id = :id")
    Optional<OrdenAbastecimiento> findByIdWithItems(@Param("id") Long id);

    // ================= CONSULTAS DE ESTADÍSTICAS =================

    @Query("SELECT COUNT(oa) FROM OrdenAbastecimiento oa WHERE oa.tipoOrden = :tipoOrden AND YEAR(oa.fechaOA) = :year") // CAMBIADO
    Long countByTipoOrdenAndFechaOAYear(@Param("tipoOrden") OrdenAbastecimiento.TipoOrden tipoOrden, // CAMBIADO
                                        @Param("year") int year);

    // CONSULTA CORREGIDA - SIN REGEXP
    @Query("SELECT MAX(CAST(SUBSTRING(oa.numeroOA, LENGTH(:prefijo) + 6, 3) AS long)) " + // CAMBIADO
            "FROM OrdenAbastecimiento oa " + // CAMBIADO
            "WHERE oa.numeroOA LIKE CONCAT(:prefijo, '-', :year, '-%') " + // CAMBIADO
            "AND LENGTH(oa.numeroOA) = LENGTH(:prefijo) + 9")
    Long findMaxNumeroByPrefijoAndYear(@Param("prefijo") String prefijo,
                                       @Param("year") String year);

    @Query("SELECT oa FROM OrdenAbastecimiento oa WHERE oa.numeroOA LIKE CONCAT(:prefijo, '-', :year, '-%')") // CAMBIADO
    List<OrdenAbastecimiento> findByNumeroOAStartingWith(@Param("prefijo") String prefijo, // CAMBIADO
                                                         @Param("year") String year);

    // ================= CONSULTAS POR FECHAS =================

    @Query("SELECT oa FROM OrdenAbastecimiento oa " + // CAMBIADO
            "WHERE oa.fechaOA BETWEEN :fechaInicio AND :fechaFin " + // CAMBIADO
            "ORDER BY oa.fechaOA DESC") // CAMBIADO
    List<OrdenAbastecimiento> findByFechaOABetween(@Param("fechaInicio") LocalDateTime fechaInicio, // CAMBIADO
                                                   @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT oa FROM OrdenAbastecimiento oa " + // CAMBIADO
            "WHERE YEAR(oa.fechaOA) = YEAR(CURRENT_DATE) AND MONTH(oa.fechaOA) = MONTH(CURRENT_DATE) " + // CAMBIADO
            "ORDER BY oa.fechaOA DESC") // CAMBIADO
    List<OrdenAbastecimiento> findOrdenesDelMesActual();

    @Query("SELECT oa FROM OrdenAbastecimiento oa WHERE oa.estado = 'PENDIENTE' ORDER BY oa.fechaOA ASC") // CAMBIADO
    List<OrdenAbastecimiento> findOrdenesPendientes();

    // ================= CONSULTAS DE REPORTES =================

    @Query("SELECT oa.tipoOrden, COUNT(oa) FROM OrdenAbastecimiento oa GROUP BY oa.tipoOrden") // CAMBIADO
    List<Object[]> countOrdenesByTipo();

    @Query("SELECT oa.tipoOrden, SUM(oa.total) FROM OrdenAbastecimiento oa WHERE oa.total > 0 GROUP BY oa.tipoOrden") // CAMBIADO
    List<Object[]> sumTotalByTipo();

    @Query("SELECT p.nombre, COUNT(oa), SUM(oa.total) " + // CAMBIADO
            "FROM OrdenAbastecimiento oa JOIN oa.proveedor p " + // CAMBIADO
            "GROUP BY p.id, p.nombre " +
            "ORDER BY SUM(oa.total) DESC")
    List<Object[]> findEstadisticasPorProveedor();

    @Query("SELECT oa FROM OrdenAbastecimiento oa " + // CAMBIADO
            "LEFT JOIN FETCH oa.proveedor " +
            "ORDER BY oa.fechaCreacion DESC")
    List<OrdenAbastecimiento> findUltimasOrdenes();

    // ================= CONSULTAS DE VALIDACIÓN =================

    @Query("SELECT COUNT(oa) > 0 FROM OrdenAbastecimiento oa WHERE oa.numeroOA = :numeroOA AND oa.id != :excludeId") // CAMBIADO
    boolean existsByNumeroOAAndIdNot(@Param("numeroOA") String numeroOA, // CAMBIADO
                                     @Param("excludeId") Long excludeId);

    @Query("SELECT DISTINCT oa FROM OrdenAbastecimiento oa " + // CAMBIADO
            "JOIN oa.items i " +
            "WHERE i.producto.id = :productoId " +
            "ORDER BY oa.fechaOA DESC") // CAMBIADO
    List<OrdenAbastecimiento> findByProductoId(@Param("productoId") Long productoId);

    // ================= CONSULTAS PARA DASHBOARD =================

    @Query("SELECT " +
            "COUNT(oa) as totalOrdenes, " + // CAMBIADO
            "SUM(CASE WHEN oa.estado = 'PENDIENTE' THEN 1 ELSE 0 END) as pendientes, " + // CAMBIADO
            "SUM(CASE WHEN oa.estado = 'APROBADA' THEN 1 ELSE 0 END) as aprobadas, " + // CAMBIADO
            "SUM(CASE WHEN oa.estado = 'COMPLETADA' THEN 1 ELSE 0 END) as completadas, " + // CAMBIADO
            "COALESCE(SUM(oa.total), 0) as totalMonto " + // CAMBIADO
            "FROM OrdenAbastecimiento oa " + // CAMBIADO
            "WHERE YEAR(oa.fechaOA) = YEAR(CURRENT_DATE)") // CAMBIADO
    Object[] findEstadisticasDashboard();
}