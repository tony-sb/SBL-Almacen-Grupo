package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.Producto;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de productos en el inventario
 * Define la interfaz de operaciones disponibles para productos
 *
 * @author Equipo de Desarrollo
 */
public interface ProductoService {

    /**
     * Obtiene todos los productos del inventario
     *
     * @return Lista de productos
     */
    List<Producto> obtenerTodosProductos();

    /**
     * Busca un producto por su ID
     *
     * @param id ID del producto
     * @return Optional con el producto encontrado
     */
    Optional<Producto> obtenerProductoPorId(Long id);

    /**
     * Guarda un nuevo producto en el inventario
     *
     * @param producto Producto a guardar
     * @return Producto guardado
     */
    Producto guardarProducto(Producto producto);

    /**
     * Actualiza un producto existente
     *
     * @param producto Producto con los datos actualizados
     * @return Producto actualizado
     */
    Producto actualizarProducto(Producto producto);

    /**
     * Elimina un producto por su ID
     *
     * @param id ID del producto a eliminar
     */
    void eliminarProducto(Long id);

    /**
     * Obtiene productos por categoría
     *
     * @param categoria Categoría de productos
     * @return Lista de productos en la categoría
     */
    List<Producto> obtenerProductosPorCategoria(String categoria);

    /**
     * Obtiene productos con stock bajo
     *
     * @return Lista de productos con stock bajo
     */
    List<Producto> obtenerProductosConStockBajo();

    /**
     * Verifica si existe un producto con el código especificado
     *
     * @param codigo Código del producto
     * @return true si existe, false en caso contrario
     */
    boolean existeProductoPorCodigo(String codigo);
}