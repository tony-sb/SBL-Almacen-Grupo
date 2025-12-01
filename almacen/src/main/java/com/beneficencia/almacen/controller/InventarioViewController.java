package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para la vista de inventario con alertas.
 * Gestiona la visualización del inventario y las notificaciones de stock bajo.
 */
@Controller
public class InventarioViewController {

    @Autowired
    private ProductoService productoService;

    /**
     * Muestra la página de inventario con alertas de stock bajo y categorías disponibles.
     * Procesa las solicitudes GET a la ruta /inventario y prepara los datos necesarios
     * para la vista, incluyendo el conteo de productos con stock bajo y la lista de categorías.
     * Implementa manejo de excepciones para garantizar que la vista siempre reciba datos válidos.
     *
     * @param model Modelo para pasar datos a la vista
     * @return Nombre de la vista 'inventario' que será renderizada
     */
    @GetMapping("/inventario")
    public String mostrarInventario(Model model) {
        try {
            // Agregar datos básicos para la vista
            Long productosStockBajo = productoService.contarProductosConStockBajo();
            model.addAttribute("totalProductosStockBajo", productosStockBajo);
            model.addAttribute("categorias", productoService.obtenerTodasLasCategorias());
        } catch (Exception e) {
            // En caso de error, establecer valores por defecto
            model.addAttribute("totalProductosStockBajo", 0L);
            model.addAttribute("categorias", java.util.List.of());
        }
        return "inventario";
    }
}