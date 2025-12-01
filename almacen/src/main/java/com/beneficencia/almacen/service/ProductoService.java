package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.Producto;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para la gestión de productos en el inventario del almacén.
 * Define la interfaz de operaciones disponibles para productos incluyendo
 * operaciones CRUD, búsquedas, filtros y análisis de inventario.
 */
public interface ProductoService {

    /**
     * Obtiene todos los productos del inventario.
     * Incluye todos los productos registrados sin filtros aplicados.
     *
     * @return Lista completa de productos del inventario
     */
    List<Producto> obtenerTodosProductos();

    /**
     * Busca un producto por su ID único.
     *
     * @param id ID del producto a buscar
     * @return Optional con el producto encontrado o vacío si no existe
     */
    Optional<Producto> obtenerProductoPorId(Long id);

    /**
     * Guarda un nuevo producto en el inventario.
     * Realiza validaciones de duplicados y datos requeridos.
     *
     * @param producto Producto a guardar
     * @return Producto guardado con ID generado
     */
    Producto guardarProducto(Producto producto);

    /**
     * Actualiza un producto existente en el inventario.
     * Mantiene el ID original y actualiza los demás campos.
     *
     * @param producto Producto con los datos actualizados
     * @return Producto actualizado
     */
    Producto actualizarProducto(Producto producto);

    /**
     * Elimina un producto del inventario por su ID.
     * Realiza validaciones de existencia antes de proceder.
     *
     * @param id ID del producto a eliminar
     */
    void eliminarProducto(Long id);

    /**
     * Obtiene productos filtrados por categoría específica.
     *
     * @param categoria Categoría de productos a filtrar
     * @return Lista de productos que pertenecen a la categoría especificada
     */
    List<Producto> obtenerProductosPorCategoria(String categoria);

    /**
     * Obtiene productos con stock bajo (cantidad <= stock mínimo).
     * Identifica productos que necesitan reabastecimiento urgente.
     *
     * @return Lista de productos con stock igual o por debajo del mínimo establecido
     */
    List<Producto> obtenerProductosConStockBajo();

    /**
     * Verifica si existe un producto con el código especificado.
     * Utilizado para validaciones de duplicados durante la creación de productos.
     *
     * @param codigo Código del producto a verificar
     * @return true si existe un producto con el código, false en caso contrario
     */
    boolean existeProductoPorCodigo(String codigo);

    // MÉTODOS NUEVOS PARA ORDEN DE SALIDAS

    /**
     * Busca productos por término en código o nombre (búsqueda case insensitive).
     * Realiza una búsqueda parcial que incluye tanto código como nombre del producto.
     *
     * @param termino Término de búsqueda para código o nombre
     * @return Lista de productos que coinciden con el término en código o nombre
     */
    List<Producto> buscarProductosPorTermino(String termino);

    /**
     * Busca un producto por su código único.
     * Utilizado en procesos de órdenes de salida para validar productos.
     *
     * @param codigo Código del producto a buscar
     * @return Optional con el producto encontrado o vacío si no existe
     */
    Optional<Producto> obtenerProductoPorCodigo(String codigo);

    // MÉTODOS NUEVOS PARA EL INVENTARIO CON ALERTAS

    /**
     * Cuenta el número de productos con stock bajo.
     * Proporciona un conteo rápido para alertas y dashboards.
     *
     * @return Número total de productos con stock bajo
     */
    Long contarProductosConStockBajo();

    /**
     * Obtiene todas las categorías únicas existentes en el inventario.
     * Útil para filtros, agrupaciones y formularios de selección.
     *
     * @return Lista de categorías únicas ordenadas alfabéticamente
     */
    List<String> obtenerTodasLasCategorias();

    /**
     * Verifica si un producto específico tiene stock bajo.
     * Evalúa si la cantidad actual es menor o igual al stock mínimo.
     *
     * @param producto Producto a verificar
     * @return true si el producto tiene stock bajo, false en caso contrario
     */
    boolean tieneStockBajo(Producto producto);

    /**
     * Obtiene estadísticas detalladas del inventario.
     * Incluye totales por categoría, productos con stock bajo y otras métricas.
     *
     * @return Mapa con las siguientes estadísticas:
     *         - "totalProductos": Número total de productos
     *         - "productosStockBajo": Número de productos con stock bajo
     *         - "totalCategorias": Número de categorías únicas
     *         - "estadisticasPorCategoria": Conteo de productos por categoría
     */
    Map<String, Object> obtenerEstadisticasInventario();
}