package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.model.Producto;
import com.beneficencia.almacen.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/inventario")
@CrossOrigin(origins = "*")
public class ProductoApiController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerInventarioCompleto() {
        try {
            List<Producto> productos = productoService.obtenerTodosProductos();
            Long productosStockBajo = productoService.contarProductosConStockBajo();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("productos", productos);
            response.put("totalProductos", productos.size());
            response.put("productosStockBajo", productosStockBajo);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(crearErrorResponse("Error al obtener inventario: " + e.getMessage()));
        }
    }

    @GetMapping("/alertas")
    public ResponseEntity<Map<String, Object>> obtenerAlertasStockBajo() {
        try {
            List<Producto> productosStockBajo = productoService.obtenerProductosConStockBajo();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("productos", productosStockBajo);
            response.put("totalAlertas", productosStockBajo.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(crearErrorResponse("Error al obtener alertas: " + e.getMessage()));
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<Map<String, Object>> buscarProductos(@RequestParam String q) {
        try {
            List<Producto> productos = productoService.buscarProductosPorTermino(q);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("productos", productos);
            response.put("totalResultados", productos.size());
            response.put("terminoBusqueda", q);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(crearErrorResponse("Error en búsqueda: " + e.getMessage()));
        }
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<Map<String, Object>> obtenerProductosPorCategoria(@PathVariable String categoria) {
        try {
            List<Producto> productos = productoService.obtenerProductosPorCategoria(categoria);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("productos", productos);
            response.put("categoria", categoria);
            response.put("totalProductos", productos.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(crearErrorResponse("Error al obtener categoría: " + e.getMessage()));
        }
    }

    @GetMapping("/categorias")
    public ResponseEntity<Map<String, Object>> obtenerCategorias() {
        try {
            List<String> categorias = productoService.obtenerTodasLasCategorias();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("categorias", categorias);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(crearErrorResponse("Error al obtener categorías: " + e.getMessage()));
        }
    }

    @PostMapping("/abastecer")
    public ResponseEntity<Map<String, Object>> solicitarAbastecimiento(@RequestBody Map<String, Object> request) {
        try {
            Long productoId = Long.valueOf(request.get("productoId").toString());
            Integer cantidad = Integer.valueOf(request.get("cantidad").toString());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Solicitud de abastecimiento recibida");
            response.put("productoId", productoId);
            response.put("cantidadSolicitada", cantidad);
            response.put("estado", "PENDIENTE");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(crearErrorResponse("Error en solicitud de abastecimiento: " + e.getMessage()));
        }
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        try {
            Map<String, Object> estadisticas = productoService.obtenerEstadisticasInventario();
            estadisticas.put("success", true);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(crearErrorResponse("Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    @GetMapping("/grupos")
    public ResponseEntity<Map<String, Object>> obtenerGruposUnicos() {
        try {
            List<Producto> productos = productoService.obtenerTodosProductos();

            List<String> gruposUnicos = productos.stream()
                    .map(Producto::getCategoria)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("grupos", gruposUnicos);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(crearErrorResponse("Error al obtener grupos: " + e.getMessage()));
        }
    }

    private Map<String, Object> crearErrorResponse(String mensaje) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", mensaje);
        return errorResponse;
    }
}