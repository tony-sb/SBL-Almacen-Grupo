package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.model.Producto;
import com.beneficencia.almacen.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controlador para la gestión de productos
 *
 * @author Equipo de Desarrollo
 */
@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    /**
     * Muestra la lista de todos los productos
     *
     * @param model Modelo para pasar datos a la vista
     * @return Nombre de la vista JSP
     */
    @GetMapping
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoService.obtenerTodosProductos());
        return "productos/lista";
    }

    /**
     * Muestra el formulario para crear un nuevo producto
     *
     * @param model Modelo para pasar datos a la vista
     * @return Nombre de la vista JSP
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoProducto(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", new String[]{"Medicamentos", "Insumos Médicos", "Limpieza", "Alimentos", "Otros"});
        model.addAttribute("unidades", new String[]{"Unidad", "Caja", "Paquete", "Litro", "Kilogramo"});
        return "productos/formulario";
    }

    /**
     * Procesa el formulario para guardar un nuevo producto
     *
     * @param producto Producto a guardar
     * @param redirectAttributes Atributos para redirección
     * @return Redirección a la lista de productos
     */
    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto, RedirectAttributes redirectAttributes) {
        try {
            productoService.guardarProducto(producto);
            redirectAttributes.addFlashAttribute("success", "Producto guardado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar el producto: " + e.getMessage());
        }
        return "redirect:/productos";
    }

    /**
     * Muestra el formulario para editar un producto existente
     *
     * @param id ID del producto a editar
     * @param model Modelo para pasar datos a la vista
     * @param redirectAttributes Atributos para redirección
     * @return Nombre de la vista JSP
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarProducto(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Producto> producto = productoService.obtenerProductoPorId(id);
        if (producto.isPresent()) {
            model.addAttribute("producto", producto.get());
            model.addAttribute("categorias", new String[]{"Medicamentos", "Insumos Médicos", "Limpieza", "Alimentos", "Otros"});
            model.addAttribute("unidades", new String[]{"Unidad", "Caja", "Paquete", "Litro", "Kilogramo"});
            return "productos/formulario";
        } else {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
            return "redirect:/productos";
        }
    }

    /**
     * Elimina un producto
     *
     * @param id ID del producto a eliminar
     * @param redirectAttributes Atributos para redirección
     * @return Redirección a la lista de productos
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productoService.eliminarProducto(id);
            redirectAttributes.addFlashAttribute("success", "Producto eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el producto: " + e.getMessage());
        }
        return "redirect:/productos";
    }
}