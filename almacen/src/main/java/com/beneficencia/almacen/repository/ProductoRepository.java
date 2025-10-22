package com.beneficencia.almacen.repository;

import com.beneficencia.almacen.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para operaciones CRUD de la entidad Producto
 *
 * @author Equipo de Desarrollo
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca productos por categoría
     *
     * @param categoria Categoría de productos
     * @return Lista de productos en la categoría
     */
    List<Producto> findByCategoria(String categoria);

    /**
     * Busca productos con stock bajo (cantidad <= stock mínimo)
     *
     * @return Lista de productos con stock bajo
     */
    @Query("SELECT p FROM Producto p WHERE p.cantidad <= p.stockMinimo")
    List<Producto> findProductosConStockBajo();

    /**
     * Verifica si existe un producto con el código especificado
     *
     * @param codigo Código del producto
     * @return true si existe, false en caso contrario
     */
    boolean existsByCodigo(String codigo);

    /**
     * Cuenta productos con stock bajo (cantidad <= stock mínimo)
     *
     * @return Número de productos con stock bajo
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.cantidad <= p.stockMinimo")
    Long countByCantidadLessThanEqualStockMinimo();

    /**
     * Busca productos que no están en la lista de IDs proporcionada
     *
     * @param ids Lista de IDs a excluir
     * @return Lista de productos que no están en los IDs proporcionados
     */
    @Query("SELECT p FROM Producto p WHERE p.id NOT IN :ids")
    List<Producto> findByIdNotIn(List<Long> ids);
}