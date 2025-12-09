package com.beneficencia.almacen.service;

import com.beneficencia.almacen.model.MovimientoReciente;
import com.beneficencia.almacen.model.OrdenSalida;
import com.beneficencia.almacen.model.OrdenSalidaItem;
import com.beneficencia.almacen.model.Producto;
import com.beneficencia.almacen.repository.MovimientoRecienteRepository;
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

/**
 * Servicio para la gestión de datos del dashboard del sistema.
 * Proporciona métodos para obtener y procesar la información que se muestra
 * en el panel principal de control del almacén.
 */
@Service
public class DashboardService {

        @Autowired
        private OrdenSalidaItemRepository ordenSalidaItemRepository;

        @Autowired
        private OrdenSalidaRepository ordenSalidaRepository;

        @Autowired
        private ProductoRepository productoRepository;

        /**
         * Obtiene y procesa todos los datos necesarios para el dashboard del sistema.
         */
        public Map<String, Object> getDashboardData() {
            Map<String, Object> dashboardData = new HashMap<>();

            // 1. OBTENER MOVIMIENTOS RECIENTES (últimos 30 días)
            LocalDate fechaInicio = LocalDate.now().minusDays(30);
            LocalDate fechaFin = LocalDate.now();

            // Obtener órdenes de salida recientes
            List<OrdenSalida> ordenesRecientes = ordenSalidaRepository
                    .findByFechaSalidaBetween(fechaInicio, fechaFin);

            // Crear lista de movimientos recientes para el dashboard
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

            // 2. OBTENER PRODUCTOS SIN MOVIMIENTOS (últimos 90 días)
            LocalDate fechaInicioSinMovimientos = LocalDate.now().minusDays(90);
            List<Producto> todosProductos = productoRepository.findAll();
            List<Producto> productosSinMovimientos = new ArrayList<>();

            for (Producto producto : todosProductos) {
                // Verificar si el producto tiene órdenes de salida recientes
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

            // 3. OBTENER PRODUCTOS CON STOCK BAJO
            List<Producto> productosStockBajo = productoRepository.findProductosConStockBajo();

            // 4. RETORNAR datos para el HTML
            dashboardData.put("movimientosRecientes", movimientosRecientes);
            dashboardData.put("productosSinMovimientos", productosSinMovimientos);
            dashboardData.put("productosStockBajo", productosStockBajo);
            dashboardData.put("cantidadSinMovimientos", productosSinMovimientos.size());
            dashboardData.put("cantidadStockBajo", productosStockBajo.size());

            return dashboardData;
        }
    }
