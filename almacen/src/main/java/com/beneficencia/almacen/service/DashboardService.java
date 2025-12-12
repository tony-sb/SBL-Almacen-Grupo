package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.OrdenSalida;
import com.beneficencia.almacen.model.OrdenSalidaItem;
import com.beneficencia.almacen.model.Producto;
import com.beneficencia.almacen.repository.OrdenSalidaItemRepository;
import com.beneficencia.almacen.repository.OrdenSalidaRepository;
import com.beneficencia.almacen.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

        @Autowired
        private OrdenSalidaItemRepository ordenSalidaItemRepository;

        @Autowired
        private OrdenSalidaRepository ordenSalidaRepository;

        @Autowired
        private ProductoRepository productoRepository;

        public Map<String, Object> getDashboardData() {
            Map<String, Object> dashboardData = new HashMap<>();

            LocalDate fechaInicio = LocalDate.now().minusDays(30);
            LocalDate fechaFin = LocalDate.now();

            List<OrdenSalida> ordenesRecientes = ordenSalidaRepository
                    .findByFechaSalidaBetween(fechaInicio, fechaFin);

            List<Map<String, Object>> movimientosRecientes = new ArrayList<>();

            for (OrdenSalida orden : ordenesRecientes) {
                // Obtener items de la orden
                List<OrdenSalidaItem> items = ordenSalidaItemRepository
                        .findItemsConProductosPorOrdenId(orden.getId());

                for (OrdenSalidaItem item : items) {
                    Map<String, Object> movimiento = new HashMap<>();
                    movimiento.put("nombreProducto", item.getProducto().getNombre());
                    movimiento.put("fechaSalida", orden.getFechaSalida());
                    movimiento.put("cantidad", item.getCantidad());
                    movimiento.put("dniBeneficiario", orden.getDniUsuario());
                    movimiento.put("numeroOrden", orden.getNumeroOrden());
                    movimientosRecientes.add(movimiento);
                }
            }

            LocalDate fechaInicioSinMovimientos = LocalDate.now().minusDays(90);
            List<Producto> todosProductos = productoRepository.findAll();
            List<Producto> productosSinMovimientos = new ArrayList<>();

            for (Producto producto : todosProductos) {
                List<OrdenSalidaItem> itemsRecientes = ordenSalidaItemRepository
                        .findByProductoId(producto.getId()).stream()
                        .filter(item -> {
                            OrdenSalida orden = item.getOrdenSalida();
                            return orden != null &&
                                    !orden.getFechaSalida().isBefore(fechaInicioSinMovimientos);
                        })
                        .collect(Collectors.toList());

                if (itemsRecientes.isEmpty()) {
                    productosSinMovimientos.add(producto);
                }
            }

            List<Producto> productosStockBajo = productoRepository.findProductosConStockBajo();

            dashboardData.put("movimientosRecientes", movimientosRecientes);
            dashboardData.put("productosSinMovimientos", productosSinMovimientos);
            dashboardData.put("productosStockBajo", productosStockBajo);
            dashboardData.put("cantidadSinMovimientos", productosSinMovimientos.size());
            dashboardData.put("cantidadStockBajo", productosStockBajo.size());

            return dashboardData;
        }
    }
