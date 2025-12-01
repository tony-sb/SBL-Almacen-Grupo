package com.beneficencia.almacen.repository;

import com.beneficencia.almacen.model.OrdenAbastecimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD y consultas personalizadas de la entidad OrdenAbastecimiento.
 * Proporciona métodos para gestionar órdenes de abastecimiento con diversas opciones de filtrado,
 * estadísticas y consultas optimizadas para el dashboard y reportes.
 */
@Repository
public interface OrdenAbastecimientoRepository extends JpaRepository<OrdenAbastecimiento, Long> {

    // ================= CONSULTAS BÁSICAS =================

    /**
     * Busca una orden de abastecimiento por su número único.
     *
     * @param numeroOA Número de la orden de abastecimiento a buscar
     * @return Optional con la orden encontrada o vacío si no existe
     */
    Optional<OrdenAbastecimiento> findByNumeroOA(String numeroOA);

    /**
     * Busca órdenes de abastecimiento por tipo de orden.
     *
     * @param tipoOrden Tipo de orden a filtrar
     * @return Lista de órdenes que coinciden con el tipo especificado
     */
    List<OrdenAbastecimiento> findByTipoOrden(OrdenAbastecimiento.TipoOrden tipoOrden);

    /**
     * Busca órdenes de abastecimiento por estado.
     *
     * @param estado Estado de la orden a filtrar
     * @return Lista de órdenes que coinciden con el estado especificado
     */
    List<OrdenAbastecimiento> findByEstado(OrdenAbastecimiento.EstadoOrden estado);

    /**
     * Verifica si existe una orden de abastecimiento con el número especificado.
     *
     * @param numeroOA Número de orden a verificar
     * @return true si existe una orden con ese número, false en caso contrario
     */
    boolean existsByNumeroOA(String numeroOA);

    // ================= CONSULTAS CON FETCH JOIN =================

    /**
     * Obtiene todas las órdenes de abastecimiento con información de proveedor y usuario cargada.
     * Utiliza JOIN FETCH para optimizar las consultas y evitar el problema N+1.
     * Ordena los resultados por fecha de creación descendente.
     *
     * @return Lista de órdenes con proveedor y usuario cargados
     */
    @Query("SELECT DISTINCT oa FROM OrdenAbastecimiento oa " +
            "LEFT JOIN FETCH oa.proveedor " +
            "LEFT JOIN FETCH oa.usuario " +
            "ORDER BY oa.fechaCreacion DESC, oa.id DESC")
    List<OrdenAbastecimiento> findAllWithProveedorAndUsuario();

    /**
     * Busca una orden de abastecimiento por ID con todos sus items y productos cargados.
     * Carga eagermente la relación con items y productos para operaciones de edición y visualización.
     *
     * @param id ID de la orden a buscar
     * @return Optional con la orden encontrada y sus items cargados
     */
    @Query("SELECT DISTINCT oa FROM OrdenAbastecimiento oa " +
            "LEFT JOIN FETCH oa.proveedor " +
            "LEFT JOIN FETCH oa.usuario " +
            "LEFT JOIN FETCH oa.items i " +
            "LEFT JOIN FETCH i.producto " +
            "WHERE oa.id = :id")
    Optional<OrdenAbastecimiento> findByIdWithItems(@Param("id") Long id);

    // ================= CONSULTAS DE ESTADÍSTICAS =================

    /**
     * Cuenta el número de órdenes de abastecimiento por tipo y año.
     * Útil para reportes anuales y análisis de tendencias.
     *
     * @param tipoOrden Tipo de orden a contar
     * @param year Año para filtrar las órdenes
     * @return Número de órdenes que coinciden con el tipo y año especificados
     */
    @Query("SELECT COUNT(oa) FROM OrdenAbastecimiento oa WHERE oa.tipoOrden = :tipoOrden AND YEAR(oa.fechaOA) = :year")
    Long countByTipoOrdenAndFechaOAYear(@Param("tipoOrden") OrdenAbastecimiento.TipoOrden tipoOrden,
                                        @Param("year") int year);

    /**
     * Encuentra el número máximo de orden para un prefijo y año específicos.
     * Utilizado para generar números de orden consecutivos automáticamente.
     *
     * @param prefijo Prefijo del número de orden (ej: "OA")
     * @param year Año para el filtro
     * @return El número máximo encontrado o null si no hay órdenes
     */
    @Query("SELECT MAX(CAST(SUBSTRING(oa.numeroOA, LENGTH(:prefijo) + 6, 3) AS long)) " +
            "FROM OrdenAbastecimiento oa " +
            "WHERE oa.numeroOA LIKE CONCAT(:prefijo, '-', :year, '-%') " +
            "AND LENGTH(oa.numeroOA) = LENGTH(:prefijo) + 9")
    Long findMaxNumeroByPrefijoAndYear(@Param("prefijo") String prefijo,
                                       @Param("year") String year);

    /**
     * Busca órdenes de abastecimiento por prefijo y año en el número de orden.
     *
     * @param prefijo Prefijo del número de orden
     * @param year Año para filtrar
     * @return Lista de órdenes que coinciden con el prefijo y año
     */
    @Query("SELECT oa FROM OrdenAbastecimiento oa WHERE oa.numeroOA LIKE CONCAT(:prefijo, '-', :year, '-%')")
    List<OrdenAbastecimiento> findByNumeroOAStartingWith(@Param("prefijo") String prefijo,
                                                         @Param("year") String year);

    // ================= CONSULTAS POR FECHAS =================

    /**
     * Busca órdenes de abastecimiento dentro de un rango de fechas.
     *
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de órdenes dentro del rango de fechas especificado
     */
    @Query("SELECT oa FROM OrdenAbastecimiento oa " +
            "WHERE oa.fechaOA BETWEEN :fechaInicio AND :fechaFin " +
            "ORDER BY oa.fechaOA DESC")
    List<OrdenAbastecimiento> findByFechaOABetween(@Param("fechaInicio") LocalDateTime fechaInicio,
                                                   @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Obtiene las órdenes de abastecimiento del mes actual.
     * Útil para dashboards y reportes mensuales.
     *
     * @return Lista de órdenes del mes actual ordenadas por fecha descendente
     */
    @Query("SELECT oa FROM OrdenAbastecimiento oa " +
            "WHERE YEAR(oa.fechaOA) = YEAR(CURRENT_DATE) AND MONTH(oa.fechaOA) = MONTH(CURRENT_DATE) " +
            "ORDER BY oa.fechaOA DESC")
    List<OrdenAbastecimiento> findOrdenesDelMesActual();

    /**
     * Obtiene las órdenes de abastecimiento con estado PENDIENTE.
     * Ordenadas por fecha de orden ascendente para priorizar las más antiguas.
     *
     * @return Lista de órdenes pendientes
     */
    @Query("SELECT oa FROM OrdenAbastecimiento oa WHERE oa.estado = 'PENDIENTE' ORDER BY oa.fechaOA ASC")
    List<OrdenAbastecimiento> findOrdenesPendientes();

    // ================= CONSULTAS DE REPORTES =================

    /**
     * Cuenta el número de órdenes por tipo.
     * Utilizado para reportes estadísticos y gráficos.
     *
     * @return Lista de arrays de objetos con tipo de orden y conteo
     */
    @Query("SELECT oa.tipoOrden, COUNT(oa) FROM OrdenAbastecimiento oa GROUP BY oa.tipoOrden")
    List<Object[]> countOrdenesByTipo();

    /**
     * Suma los totales de órdenes por tipo (excluyendo órdenes con total cero).
     * Útil para análisis financieros y reportes de gastos.
     *
     * @return Lista de arrays de objetos con tipo de orden y suma de totales
     */
    @Query("SELECT oa.tipoOrden, SUM(oa.total) FROM OrdenAbastecimiento oa WHERE oa.total > 0 GROUP BY oa.tipoOrden")
    List<Object[]> sumTotalByTipo();

    /**
     * Obtiene estadísticas de órdenes por proveedor.
     * Incluye conteo de órdenes y suma de totales por cada proveedor.
     *
     * @return Lista de arrays de objetos con nombre de proveedor, conteo y suma de totales
     */
    @Query("SELECT p.nombre, COUNT(oa), SUM(oa.total) " +
            "FROM OrdenAbastecimiento oa JOIN oa.proveedor p " +
            "GROUP BY p.id, p.nombre " +
            "ORDER BY SUM(oa.total) DESC")
    List<Object[]> findEstadisticasPorProveedor();

    /**
     * Obtiene las últimas órdenes de abastecimiento para el dashboard.
     * Incluye información del proveedor para visualización.
     *
     * @return Lista de las últimas órdenes ordenadas por fecha de creación descendente
     */
    @Query("SELECT oa FROM OrdenAbastecimiento oa " +
            "LEFT JOIN FETCH oa.proveedor " +
            "ORDER BY oa.fechaCreacion DESC")
    List<OrdenAbastecimiento> findUltimasOrdenes();

    // ================= CONSULTAS DE VALIDACIÓN =================

    /**
     * Verifica si existe una orden con el mismo número excluyendo una orden específica.
     * Utilizado durante la edición para evitar duplicados.
     *
     * @param numeroOA Número de orden a verificar
     * @param excludeId ID de la orden a excluir de la verificación
     * @return true si existe otra orden con el mismo número, false en caso contrario
     */
    @Query("SELECT COUNT(oa) > 0 FROM OrdenAbastecimiento oa WHERE oa.numeroOA = :numeroOA AND oa.id != :excludeId")
    boolean existsByNumeroOAAndIdNot(@Param("numeroOA") String numeroOA,
                                     @Param("excludeId") Long excludeId);

    /**
     * Busca órdenes de abastecimiento que contengan un producto específico.
     * Útil para rastrear el historial de compras de un producto.
     *
     * @param productoId ID del producto a buscar
     * @return Lista de órdenes que contienen el producto especificado
     */
    @Query("SELECT DISTINCT oa FROM OrdenAbastecimiento oa " +
            "JOIN oa.items i " +
            "WHERE i.producto.id = :productoId " +
            "ORDER BY oa.fechaOA DESC")
    List<OrdenAbastecimiento> findByProductoId(@Param("productoId") Long productoId);

    // ================= CONSULTAS PARA DASHBOARD =================

    /**
     * Obtiene estadísticas consolidadas del dashboard para el año actual.
     * Retorna un array con: total de órdenes, órdenes pendientes, órdenes aprobadas,
     * órdenes completadas y monto total de todas las órdenes del año.
     *
     * @return Array de objetos con las estadísticas:
     *         [0] = totalOrdenes (Long)
     *         [1] = pendientes (Long)
     *         [2] = aprobadas (Long)
     *         [3] = completadas (Long)
     *         [4] = totalMonto (BigDecimal)
     */
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