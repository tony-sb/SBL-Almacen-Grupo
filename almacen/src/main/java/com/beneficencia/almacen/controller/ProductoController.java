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
 * Controlador para la gestión de productos del almacén.
 * Maneja las operaciones CRUD (Crear, Leer, Actualizar, Eliminar) de productos
 * a través de interfaces web utilizando el patrón MVC.
 */
@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    /**
     * Muestra la lista de todos los productos del inventario.
     * Recupera todos los productos existentes y los pasa a la vista para su visualización.
     *
     * @param model Modelo para pasar datos a la vista
     * @return Nombre de la vista JSP 'productos/lista'
     */
    @GetMapping
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoService.obtenerTodosProductos());
        return "productos/lista";
    }

    /**
     * Muestra el formulario para crear un nuevo producto.
     * Prepara el modelo con un producto vacío y las opciones predefinidas
     * para categorías y unidades de medida.
     *
     * @param model Modelo para pasar datos a la vista
     * @return Nombre de la vista JSP 'productos/formulario'
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoProducto(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", new String[]{"Medicamentos", "Insumos Médicos", "Limpieza", "Alimentos", "Otros"});
        model.addAttribute("unidades", new String[]{"Unidad", "Caja", "Paquete", "Litro", "Kilogramo"});
        return "productos/formulario";
    }

    /**
     * Procesa el formulario para guardar un nuevo producto.
     * Recibe los datos del formulario, valida y persiste el producto en la base de datos.
     * Maneja excepciones y proporciona retroalimentación al usuario mediante mensajes flash.
     *
     * @param producto Objeto Producto con los datos del formulario
     * @param redirectAttributes Atributos para mensajes flash en redirección
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
     * Muestra el formulario para editar un producto existente.
     * Busca el producto por ID y carga sus datos en el formulario para edición.
     * Si el producto no existe, redirige con un mensaje de error.
     *
     * @param id ID del producto a editar
     * @param model Modelo para pasar datos a la vista
     * @param redirectAttributes Atributos para mensajes flash en redirección
     * @return Nombre de la vista JSP 'productos/formulario' o redirección en caso de error
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
     * Elimina un producto del inventario.
     * Busca el producto por ID y procede con su eliminación si existe.
     * Maneja excepciones y proporciona retroalimentación al usuario.
     *
     * @param id ID del producto a eliminar
     * @param redirectAttributes Atributos para mensajes flash en redirección
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