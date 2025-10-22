package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.Producto;
import com.beneficencia.almacen.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de productos
 *
 * @author Equipo de Desarrollo
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
}