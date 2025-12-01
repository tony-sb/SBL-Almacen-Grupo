package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.MovimientoReciente;
import com.beneficencia.almacen.model.Producto;
import com.beneficencia.almacen.repository.MovimientoRecienteRepository;
import com.beneficencia.almacen.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de datos del dashboard del sistema.
 * Proporciona métodos para obtener y procesar la información que se muestra
 * en el panel principal de control del almacén.
 */
@Service
public class DashboardService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MovimientoRecienteRepository movimientoRecienteRepository;

    /**
     * Obtiene y procesa todos los datos necesarios para el dashboard del sistema.
     * Recopila información sobre movimientos recientes, productos sin movimientos
     * y productos con stock bajo para mostrar en el panel de control principal.
     *
     * @return Mapa con los datos del dashboard organizados en las siguientes claves:
     *         - "movimientosRecientes": Lista de movimientos ordenados por fecha descendente
     *         - "productosSinMovimientos": Lista de productos sin movimientos recientes
     *         - "productosStockBajo": Lista de productos con stock bajo
     */
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();

        // 1. OBTENER MOVIMIENTOS RECIENTES desde BD MySQL
        List<MovimientoReciente> movimientosRecientes = movimientoRecienteRepository.findAllByOrderByFechaSalidaDesc();

        // 2. OBTENER TODOS LOS PRODUCTOS desde BD MySQL
        List<Producto> todosProductos = productoRepository.findAll();

        // 3. FILTRAR productos que NO tienen movimientos recientes
        List<Long> idsProductosConMovimientos = movimientosRecientes.stream()
                .map(movimiento -> movimiento.getProducto().getId())
                .collect(Collectors.toList());

        List<Producto> productosSinMovimientos = todosProductos.stream()
                .filter(producto -> !idsProductosConMovimientos.contains(producto.getId()))
                .collect(Collectors.toList());

        // 4. OBTENER PRODUCTOS CON STOCK BAJO
        List<Producto> productosStockBajo = productoRepository.findProductosConStockBajo();

        // 5. RETORNAR datos para el HTML
        dashboardData.put("movimientosRecientes", movimientosRecientes);
        dashboardData.put("productosSinMovimientos", productosSinMovimientos);
        dashboardData.put("productosStockBajo", productosStockBajo);

        return dashboardData;
    }
}