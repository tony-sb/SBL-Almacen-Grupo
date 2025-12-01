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

/**
 * Controlador REST para operaciones de inventario con el frontend.
 * Proporciona endpoints API para gestionar productos, categorías, alertas de stock
 * y estadísticas del inventario. Diseñado para ser consumido por aplicaciones frontend.
 */
@RestController
@RequestMapping("/api/inventario")
@CrossOrigin(origins = "*")
public class ProductoApiController {

    @Autowired
    private ProductoService productoService;

    /**
     * Obtiene el inventario completo con información de alertas y estadísticas.
     * Incluye todos los productos, total de productos y conteo de productos con stock bajo.
     *
     * @return ResponseEntity con mapa conteniendo los datos del inventario
     */
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

    /**
     * Obtiene solo los productos con stock bajo para mostrar alertas.
     * Utilizado para notificaciones y dashboard de alertas.
     *
     * @return ResponseEntity con lista de productos con stock bajo
     */
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

    /**
     * Busca productos por término en nombre o código.
     * Realiza búsqueda case-insensitive en campos de nombre y código del producto.
     *
     * @param q Término de búsqueda
     * @return ResponseEntity con productos que coinciden con el término de búsqueda
     */
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

    /**
     * Obtiene productos filtrados por categoría específica.
     *
     * @param categoria Categoría por la cual filtrar los productos
     * @return ResponseEntity con productos de la categoría especificada
     */
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

    /**
     * Obtiene todas las categorías disponibles en el inventario.
     * Retorna una lista única de categorías para filtros y agrupaciones.
     *
     * @return ResponseEntity con lista de categorías únicas
     */
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

    /**
     * Endpoint para simular solicitud de abastecimiento de producto.
     * En una implementación real, esto generaría una orden de abastecimiento.
     *
     * @param request Mapa con productoId y cantidad a abastecer
     * @return ResponseEntity confirmando la solicitud de abastecimiento
     */
    @PostMapping("/abastecer")
    public ResponseEntity<Map<String, Object>> solicitarAbastecimiento(@RequestBody Map<String, Object> request) {
        try {
            Long productoId = Long.valueOf(request.get("productoId").toString());
            Integer cantidad = Integer.valueOf(request.get("cantidad").toString());

            // Aquí iría la lógica real de abastecimiento
            // Por ahora simulamos la respuesta

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

    /**
     * Obtiene estadísticas detalladas del inventario.
     * Incluye totales por categoría, productos con stock bajo, y otras métricas.
     *
     * @return ResponseEntity con mapa de estadísticas del inventario
     */
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

    /**
     * Obtiene grupos únicos de productos basados en la categoría.
     * Similar a obtener categorías pero con un enfoque diferente en la agrupación.
     *
     * @return ResponseEntity con lista de grupos únicos
     */
    @GetMapping("/grupos")
    public ResponseEntity<Map<String, Object>> obtenerGruposUnicos() {
        try {
            List<Producto> productos = productoService.obtenerTodosProductos();

            // Obtener grupos únicos de la categoría
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

    /**
     * Crea una respuesta de error estandarizada para los endpoints.
     *
     * @param mensaje Mensaje de error descriptivo
     * @return Mapa con la estructura de error estandarizada
     */
    private Map<String, Object> crearErrorResponse(String mensaje) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", mensaje);
        return errorResponse;
    }
}