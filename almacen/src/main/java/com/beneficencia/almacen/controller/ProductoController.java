package com.beneficencia.almacen.controller;

import com.beneficencia.almacen.model.Producto;
import com.beneficencia.almacen.service.ProductoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    private final List<String> CATEGORIAS = Arrays.asList(
            "Medicamentos",
            "Insumos Médicos",
            "Limpieza",
            "Alimentos",
            "Material Oficina",
            "Otros"
    );
    private final List<String> UNIDADES = Arrays.asList(
            "Unidad",
            "Caja",
            "Paquete",
            "Litro",
            "Kilogramo",
            "Gramo",
            "Mililitro",
            "Frasco",
            "Tableta",
            "Cápsula",
            "Par",
            "Rollo"
    );

    @GetMapping
    public String listarProductos(
            @RequestParam(value = "q", required = false) String terminoBusqueda,
            @RequestParam(value = "categoria", required = false) String categoria,
            Model model) {

        List<Producto> productos;

        if (terminoBusqueda != null && !terminoBusqueda.trim().isEmpty()) {
            productos = productoService.buscarProductosPorTermino(terminoBusqueda);
            model.addAttribute("terminoBusqueda", terminoBusqueda);
        } else if (categoria != null && !categoria.trim().isEmpty()) {
            productos = productoService.obtenerProductosPorCategoria(categoria);
            model.addAttribute("categoriaSeleccionada", categoria);
        } else {
            productos = productoService.obtenerTodosProductos();
        }

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", CATEGORIAS);
        model.addAttribute("unidades", UNIDADES);
        model.addAttribute("totalProductos", productos.size());

        return "productos/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoProducto(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", CATEGORIAS);
        model.addAttribute("unidades", UNIDADES);
        model.addAttribute("fechaHoy", LocalDate.now().plusDays(30));
        model.addAttribute("modo", "nuevo");
        return "productos/formulario";
    }

    @PostMapping("/guardar")
    public String guardarProducto(
            @ModelAttribute Producto producto,
            @RequestParam(value = "tieneVencimiento", required = false) Boolean tieneVencimiento,
            HttpServletRequest request,  // Agregar esto para debug
            RedirectAttributes redirectAttributes) {

        System.out.println("=== DEBUG GUARDAR PRODUCTO ===");
        System.out.println("Producto recibido - Fecha vencimiento: " + producto.getFechaVencimiento());
        System.out.println("Checkbox 'tieneVencimiento': " + tieneVencimiento);
        System.out.println("Producto código: " + producto.getCodigo());
        System.out.println("Producto nombre: " + producto.getNombre());

        System.out.println("Parámetros de la request:");
        request.getParameterMap().forEach((key, value) ->
                System.out.println(key + " = " + Arrays.toString(value))
        );

        try {
            if (tieneVencimiento == null || !tieneVencimiento) {
                producto.setFechaVencimiento(null);
                System.out.println("Checkbox NO marcado - Fecha establecida como null");
            } else {
                System.out.println("Checkbox SÍ marcado - Manteniendo fecha: " + producto.getFechaVencimiento());
            }

            productoService.guardarProducto(producto);
            redirectAttributes.addFlashAttribute("success",
                    "Producto " + producto.getCodigo() + " guardado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al guardar el producto: " + e.getMessage());
        }
        return "redirect:/productos";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarProducto(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Optional<Producto> producto = productoService.obtenerProductoPorId(id);
        if (producto.isPresent()) {
            model.addAttribute("producto", producto.get());
            model.addAttribute("categorias", CATEGORIAS);
            model.addAttribute("unidades", UNIDADES);
            model.addAttribute("tieneVencimiento", producto.get().getFechaVencimiento() != null);
            model.addAttribute("modo", "editar");
            return "productos/formulario";
        } else {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
            return "redirect:/productos";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarProducto(
            @PathVariable Long id,
            @ModelAttribute Producto producto,
            @RequestParam(value = "tieneVencimiento", required = false) Boolean tieneVencimiento,
            RedirectAttributes redirectAttributes) {

        try {
            Producto productoExistente = productoService.obtenerProductoPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

            if (tieneVencimiento == null || !tieneVencimiento) {
                producto.setFechaVencimiento(null);
            }

            producto.setId(id);
            productoService.actualizarProducto(producto);

            redirectAttributes.addFlashAttribute("success",
                    "Producto " + producto.getCodigo() + " actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al actualizar el producto: " + e.getMessage());
        }
        return "redirect:/productos";
    }

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

    @GetMapping("/detalle/{id}")
    public String verDetalleProducto(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Producto> producto = productoService.obtenerProductoPorId(id);
        if (producto.isPresent()) {
            model.addAttribute("producto", producto.get());
            return "productos/detalle";
        } else {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
            return "redirect:/productos";
        }
    }
}