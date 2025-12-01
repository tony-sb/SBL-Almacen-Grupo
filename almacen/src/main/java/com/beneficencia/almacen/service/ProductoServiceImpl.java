package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.Producto;
import com.beneficencia.almacen.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del servicio de productos para la gestión del inventario del almacén.
 * Proporciona la lógica de negocio para las operaciones CRUD, búsquedas, filtros
 * y análisis estadísticos de los productos.
 */
@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Producto> obtenerTodosProductos() {
        return productoRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Producto> obtenerProductoPorId(Long id) {
        return productoRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Producto actualizarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void eliminarProducto(Long id) {
        productoRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Producto> obtenerProductosPorCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Producto> obtenerProductosConStockBajo() {
        return productoRepository.findProductosConStockBajo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existeProductoPorCodigo(String codigo) {
        return productoRepository.existsByCodigo(codigo);
    }

    // MÉTODOS NUEVOS PARA ORDEN DE SALIDAS

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Producto> buscarProductosPorTermino(String termino) {
        // Usando el método del repositorio si existe, sino implementación manual
        try {
            return productoRepository.findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(termino, termino);
        } catch (Exception e) {
            // Fallback a implementación manual si el método del repositorio no existe
            List<Producto> todosProductos = productoRepository.findAll();
            return todosProductos.stream()
                    .filter(producto ->
                            (producto.getCodigo() != null && producto.getCodigo().toLowerCase().contains(termino.toLowerCase())) ||
                                    (producto.getNombre() != null && producto.getNombre().toLowerCase().contains(termino.toLowerCase())))
                    .toList();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Producto> obtenerProductoPorCodigo(String codigo) {
        // Usando el método del repositorio si existe, sino implementación manual
        try {
            return productoRepository.findByCodigo(codigo);
        } catch (Exception e) {
            // Fallback a implementación manual si el método del repositorio no existe
            return productoRepository.findAll().stream()
                    .filter(producto -> producto.getCodigo() != null && producto.getCodigo().equalsIgnoreCase(codigo))
                    .findFirst();
        }
    }

    // MÉTODOS NUEVOS PARA EL INVENTARIO CON ALERTAS

    /**
     * {@inheritDoc}
     */
    @Override
    public Long contarProductosConStockBajo() {
        try {
            return productoRepository.countByCantidadLessThanEqualStockMinimo();
        } catch (Exception e) {
            // Fallback a implementación manual
            return productoRepository.findAll().stream()
                    .filter(this::tieneStockBajo)
                    .count();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> obtenerTodasLasCategorias() {
        return productoRepository.findAll().stream()
                .map(Producto::getCategoria)
                .distinct()
                .sorted()
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean tieneStockBajo(Producto producto) {
        if (producto.getCantidad() == null) return true;
        if (producto.getStockMinimo() == null) return producto.getCantidad() <= 5;
        return producto.getCantidad() <= producto.getStockMinimo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> obtenerEstadisticasInventario() {
        List<Producto> todosProductos = productoRepository.findAll();
        List<Producto> productosStockBajo = obtenerProductosConStockBajo();
        List<String> categorias = obtenerTodasLasCategorias();

        // Calcular total de items en inventario
        Integer totalItems = todosProductos.stream()
                .mapToInt(producto -> producto.getCantidad() != null ? producto.getCantidad() : 0)
                .sum();

        // Calcular valor total del inventario
        Double valorTotal = todosProductos.stream()
                .mapToDouble(producto ->
                        producto.getPrecioUnitario() != null && producto.getCantidad() != null ?
                                producto.getPrecioUnitario().doubleValue() * producto.getCantidad() : 0
                )
                .sum();

        // Calcular estadísticas por categoría
        Map<String, Long> productosPorCategoria = todosProductos.stream()
                .collect(HashMap::new,
                        (map, producto) -> map.merge(producto.getCategoria(), 1L, Long::sum),
                        HashMap::putAll);

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalProductos", todosProductos.size());
        estadisticas.put("totalItems", totalItems);
        estadisticas.put("productosStockBajo", productosStockBajo.size());
        estadisticas.put("totalCategorias", categorias.size());
        estadisticas.put("valorTotalInventario", valorTotal);
        estadisticas.put("productosPorCategoria", productosPorCategoria);

        return estadisticas;
    }
}