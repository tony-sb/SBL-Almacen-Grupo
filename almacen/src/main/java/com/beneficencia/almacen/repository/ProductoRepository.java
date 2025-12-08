package com.beneficencia.almacen.repository;

import com.beneficencia.almacen.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD y consultas personalizadas de la entidad Producto.
 * Proporciona métodos para gestionar productos del inventario con diversas opciones
 * de filtrado, búsqueda y consultas específicas para el control de stock.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    //
    // Método para buscar productos por prefijo de código
    List<Producto> findByCodigoStartingWithOrderByIdDesc(String prefijo);

    // Método para verificar si existe un código (case insensitive)
    boolean existsByCodigoIgnoreCase(String codigo);

    /**
     * Busca productos por categoría específica.
     * Útil para filtrar productos por tipo o agrupación.
     *
     * @param categoria Categoría de productos a buscar
     * @return Lista de productos que pertenecen a la categoría especificada
     */
    List<Producto> findByCategoria(String categoria);

    /**
     * Busca productos con stock bajo (cantidad <= stock mínimo).
     * Identifica productos que necesitan reabastecimiento urgente.
     *
     * @return Lista de productos con stock igual o por debajo del mínimo establecido
     */
    @Query("SELECT p FROM Producto p WHERE p.cantidad <= p.stockMinimo")
    List<Producto> findProductosConStockBajo();

    /**
     * Verifica si existe un producto con el código especificado.
     * Utilizado para validar duplicados durante la creación de productos.
     *
     * @param codigo Código del producto a verificar
     * @return true si existe un producto con el código, false en caso contrario
     */
    boolean existsByCodigo(String codigo);

    /**
     * Cuenta el número de productos con stock bajo (cantidad <= stock mínimo).
     * Proporciona un conteo rápido para alertas y dashboards.
     *
     * @return Número total de productos con stock bajo
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.cantidad <= p.stockMinimo")
    Long countByCantidadLessThanEqualStockMinimo();

    /**
     * Busca productos que no están en la lista de IDs proporcionada.
     * Útil para excluir productos específicos en consultas.
     *
     * @param ids Lista de IDs de productos a excluir
     * @return Lista de productos que no están en los IDs proporcionados
     */
    @Query("SELECT p FROM Producto p WHERE p.id NOT IN :ids")
    List<Producto> findByIdNotIn(List<Long> ids);

    // MÉTODOS NUEVOS PARA ORDEN DE SALIDAS

    /**
     * Busca un producto por su código único.
     * Utilizado en procesos de órdenes de salida para validar productos.
     *
     * @param codigo Código del producto a buscar
     * @return Optional con el producto encontrado o vacío si no existe
     */
    Optional<Producto> findByCodigo(String codigo);

    /**
     * Busca productos por nombre o código (búsqueda case insensitive).
     * Realiza una búsqueda parcial en nombre y código del producto.
     * Convierte tanto el término de búsqueda como los campos a minúsculas
     * para hacer la búsqueda insensible a mayúsculas/minúsculas.
     *
     * @param termino Término de búsqueda para nombre o código
     * @param busqueda Parámetro adicional para compatibilidad (no utilizado en la consulta)
     * @return Lista de productos que coinciden con el término en nombre o código
     */
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Producto> findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(@Param("termino") String termino, String busqueda);
}