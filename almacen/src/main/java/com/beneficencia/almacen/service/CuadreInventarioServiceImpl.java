package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.CuadreInventario;
import com.beneficencia.almacen.model.Producto;
import com.beneficencia.almacen.repository.CuadreInventarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CuadreInventarioServiceImpl implements CuadreInventarioService {

    @Autowired
    private CuadreInventarioRepository cuadreRepository;

    @Autowired
    private ProductoService productoService;

    @Override
    public CuadreInventario guardarCuadre(CuadreInventario cuadre) {
        return cuadreRepository.save(cuadre);
    }

    @Override
    public List<CuadreInventario> obtenerTodosCuadresOrdenados() {
        // Obtiene todos ordenados por fecha descendente
        return cuadreRepository.findAllByOrderByFechaRegistroDesc();
    }

    @Override
    public Optional<CuadreInventario> obtenerCuadrePorId(Long id) {
        return cuadreRepository.findById(id);
    }

    @Override
    public void confirmarCuadre(Long id) {
        CuadreInventario cuadre = cuadreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cuadre no encontrado"));

        Producto producto = cuadre.getProducto();

        if ("reingresar".equals(cuadre.getAccion())) {
            // Sumar cantidad al inventario
            int nuevaCantidad = (producto.getCantidad() != null ? producto.getCantidad() : 0) + cuadre.getCantidad();
            producto.setCantidad(nuevaCantidad);
            productoService.actualizarProducto(producto);

            cuadre.setEstado("APROBADO");
        } else if ("descartar".equals(cuadre.getAccion())) {
            // Restar cantidad del inventario
            int nuevaCantidad = Math.max(0, (producto.getCantidad() != null ? producto.getCantidad() : 0) - cuadre.getCantidad());
            producto.setCantidad(nuevaCantidad);
            productoService.actualizarProducto(producto);

            cuadre.setEstado("APROBADO");
        }

        cuadre.setFechaConfirmacion(LocalDateTime.now());
        cuadreRepository.save(cuadre);
    }

    @Override
    public void descartarCuadre(Long id) {
        CuadreInventario cuadre = cuadreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cuadre no encontrado"));

        cuadre.setEstado("RECHAZADO");
        cuadre.setFechaConfirmacion(LocalDateTime.now());
        cuadreRepository.save(cuadre);
    }
}