package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InventarioViewController {

    @Autowired
    private ProductoService productoService;

    @GetMapping("/inventario")
    public String mostrarInventario(Model model) {
        try {
            Long productosStockBajo = productoService.contarProductosConStockBajo();
            model.addAttribute("totalProductosStockBajo", productosStockBajo);
            model.addAttribute("categorias", productoService.obtenerTodasLasCategorias());
        } catch (Exception e) {
            model.addAttribute("totalProductosStockBajo", 0L);
            model.addAttribute("categorias", java.util.List.of());
        }
        return "inventario";
    }
}