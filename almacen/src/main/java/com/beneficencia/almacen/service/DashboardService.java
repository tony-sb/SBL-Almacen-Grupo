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

@Service
public class DashboardService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MovimientoRecienteRepository movimientoRecienteRepository;

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

        // 4. RETORNAR datos para el HTML
        dashboardData.put("movimientosRecientes", movimientosRecientes);
        dashboardData.put("productosSinMovimientos", productosSinMovimientos);

        return dashboardData;
    }
}