package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.Producto;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductoService {

    List<Producto> obtenerTodosProductos();

    Optional<Producto> obtenerProductoPorId(Long id);

    Producto guardarProducto(Producto producto);

    Producto actualizarProducto(Producto producto);

    void eliminarProducto(Long id);

    List<Producto> obtenerProductosPorCategoria(String categoria);

    List<Producto> obtenerProductosConStockBajo();

    boolean existeProductoPorCodigo(String codigo);

    List<Producto> buscarProductosPorTermino(String termino);

    Optional<Producto> obtenerProductoPorCodigo(String codigo);

    Long contarProductosConStockBajo();

    List<String> obtenerTodasLasCategorias();

    boolean tieneStockBajo(Producto producto);

    Map<String, Object> obtenerEstadisticasInventario();
}